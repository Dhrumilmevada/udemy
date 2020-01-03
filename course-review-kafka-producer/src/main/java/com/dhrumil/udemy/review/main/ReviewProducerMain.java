package com.dhrumil.udemy.review.main;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dhrumil.udemy.model.Review;
import com.dhrumil.udemy.runnable.KafkaPublisherThread;
import com.dhrumil.udemy.runnable.ReviewConsumerThread;
import com.typesafe.config.ConfigFactory;

public class ReviewProducerMain {

  private static final Logger logger = LoggerFactory.getLogger(ReviewProducerMain.class);
  public static final String appName = "course-review-kafka-producer";
  private String courseID;

  private ArrayBlockingQueue<Review> reviewQueue;
  private ExecutorService executor;
  private CountDownLatch latch;
  private ReviewConsumerThread reviewConsumer;
  private KafkaPublisherThread reviewpublisher;

  private AppConfig appConfig;

  public static void main(String[] args) {
    ReviewProducerMain app = new ReviewProducerMain();
    app.init();
    app.start();
  }

  public void start() {
    logger.info("Stared [{}] application.", appName);
    executor.submit(reviewConsumer);
    executor.submit(reviewpublisher);
    try {
      latch.await();
    } catch (InterruptedException e) {
      logger.error(
          "Exception in Thread scheduling errorMessage:[{}] , errorStackTrace:[{}] , errorCause:[{}]",
          e.getMessage(), e.getStackTrace(), e.getCause());
    } finally {
      shutdown();
    }
  }

  public void init() {
    logger.info("[{}] has been initialized.", appName);
    addShutDownHook();
    appConfig = new AppConfig(ConfigFactory.load());
    courseID = appConfig.getCourseID();
    executor = Executors.newFixedThreadPool(2);
    latch = new CountDownLatch(2);
    reviewQueue = new ArrayBlockingQueue<>(appConfig.getReviewQueueCapacity());
    reviewConsumer = new ReviewConsumerThread(appConfig, reviewQueue, latch, courseID);
    reviewpublisher = new KafkaPublisherThread(appConfig, reviewQueue, latch);
  }

  private void addShutDownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      if (!executor.isShutdown()) {
        logger.info("Shutdown hook initiated....");
        shutdown();
      }
    }));
  }

  private void shutdown() {
    if (!executor.isShutdown()) {
      executor.shutdown();
      logger.info("Shuting down thread Scheduler");
    }
  }
}
