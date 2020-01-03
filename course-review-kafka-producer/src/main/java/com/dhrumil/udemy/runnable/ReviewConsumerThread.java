package com.dhrumil.udemy.runnable;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dhrumil.udemy.client.UdemyCourseDetailClient;
import com.dhrumil.udemy.client.UdemyCourseReviewClient;
import com.dhrumil.udemy.model.CourseResponse;
import com.dhrumil.udemy.model.Review;
import com.dhrumil.udemy.mongodb.UdemyDatabaseClient;
import com.dhrumil.udemy.review.main.AppConfig;

public class ReviewConsumerThread implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(ReviewConsumerThread.class);
  private final int randomReviewUpdateCount;
  private ArrayBlockingQueue<Review> reviewQueue;
  private CountDownLatch latch;
  private UdemyCourseReviewClient udemyClient;
  private AppConfig appConfig;
  private UdemyCourseDetailClient courseDetail;
  private UdemyDatabaseClient dbClient;
  private CourseResponse course;

  public ReviewConsumerThread(AppConfig appConfig, ArrayBlockingQueue<Review> reviewQueue,
      CountDownLatch latch, String courseID) {
    super();
    this.appConfig = appConfig;
    this.reviewQueue = reviewQueue;
    this.latch = latch;
    randomReviewUpdateCount = (int) Math.ceil(appConfig.getPageSize() * 5.0 / 100.0);
    courseDetail = new UdemyCourseDetailClient();
    dbClient = UdemyDatabaseClient.getInstance();
    dbClient.getOrCreateCourseCollection();
    course = courseDetail.getCourseDetail(Integer.valueOf(courseID));
    dbClient.insertIntoCourseCollection(course);
    this.udemyClient = new UdemyCourseReviewClient(courseID, appConfig.getPageSize(), course);
  }

  @Override
  public void run() {
    boolean keepOnRunningConsumer = true;
    boolean isReviewAdded;

    try {
      while (keepOnRunningConsumer) {
        List<Review> reviews = udemyClient.getNextReviews();
        logger.info("Fetched [{}] reviewes using udemy course review rest api", reviews.size());
        if (reviews.size() == 0) {
          try {
            logger.warn("Review queue is empty. Waiting to get review in queue.");
            Thread.sleep(5000);
          } catch (InterruptedException e) {
            logger.error(
                "Error while waiting for queue to fill up errorMessage:[{}],errorStackTrace:[{}],errorMessage:[{}]",
                e.getMessage(), e.getStackTrace(), e.getCause());
          }
        } else {
          if (reviewQueue.size() == appConfig.getReviewQueueCapacity()) {
            while (!(reviewQueue.size() < appConfig.getReviewQueueCapacity())) {
              logger.warn("Review queue of capacity [{}] is full", reviewQueue.size());
              Thread.sleep(2000);
            }
          }

          List<Review> updatedReview = randomReviewUpdate(reviews);

          for (Review review : updatedReview) {
            isReviewAdded = reviewQueue.add(review);
            if (isReviewAdded) {
              // Thread.sleep(500);
              logger.info(
                  "Successfully added review to queue , current queue size : [{}] , queue capacity : [{}]",
                  reviewQueue.size(), appConfig.getReviewQueueCapacity());
            }
          }
        }
      }
    } catch (InterruptedException | IllegalStateException e) {
      logger.error(
          "Error while adding review in queue errorMessage:[{}],errorCause:[{}],errorStackTrace:[{}]",
          e.getMessage(), e.getCause(), e.getStackTrace());
    } finally {
      this.close();
    }
  }

  private List<Review> randomReviewUpdate(List<Review> reviews) {
    int updatedReview = 0;
    Random random = new Random();
    for (updatedReview = 0; updatedReview < randomReviewUpdateCount; updatedReview++) {
      int index = random.nextInt(reviews.size());
      Review review = reviews.get(index);
      review = chooseANDUpdate(review);
      logger.info("Updating review for future analysis for review:[{}] ,count:[{}]", review);
      reviews.set(index, review);
    }
    return reviews;
  }

  private Review chooseANDUpdate(Review review) {
    Random random = new Random();
    int randomNumber = random.nextInt() & Integer.MAX_VALUE;
    int choice = randomNumber % 3;

    switch (choice) {
      case 0:
        review.setUpvote(1);
        return review;
      case 1:
        review.setDownvote(5);
        return review;
      case 2:
        review.setReported(true);
        review.setReportCount(2);
        return review;
      default:
        return review;
    }

  }

  private void close() {
    latch.countDown();
    logger.info("decreasing countDownLach count to [{}]", latch.getCount());
  }

}
