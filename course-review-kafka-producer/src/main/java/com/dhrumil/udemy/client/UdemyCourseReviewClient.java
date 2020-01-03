package com.dhrumil.udemy.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dhrumil.udemy.model.Course;
import com.dhrumil.udemy.model.CourseResponse;
import com.dhrumil.udemy.model.Review;
import com.dhrumil.udemy.model.ReviewResponse;
import com.dhrumil.udemy.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

public class UdemyCourseReviewClient {

  private static final Logger logger = LoggerFactory.getLogger(UdemyCourseReviewClient.class);
  private static final String TOKEN = "********";
  private String courseID;
  private final Integer pageSize;
  private final String reviewRestAPI =
      "https://www.udemy.com/api-2.0/courses/%s/reviews/?page=%s&page_size=%s";
  private JsonParser parser;
  private Integer numOfPage;
  private CourseResponse courseDetail;
  private JsonObject courseDetailJson;

  public UdemyCourseReviewClient(String courseID, Integer pageSize, CourseResponse courseDetail) {
    this.courseID = courseID;
    this.pageSize = pageSize;
    parser = new JsonParser();
    this.numOfPage = null;
    this.courseDetail = courseDetail;
    this.courseDetailJson = parser.parse(new Gson().toJson(this.courseDetail)).getAsJsonObject();

  }

  private void init() {
    Integer count = reviewApi(1, 1).getCount();
    logger.info("[{}] review to fetch form udemy rest api", count);
    if (count % pageSize == 0) {
      numOfPage = count / pageSize;
    } else {
      numOfPage = count / pageSize + 1;
    }
    logger.info("Need to fetch [{}] pages with [{}] pagesize", numOfPage, this.pageSize);
  }

  public List<Review> getNextReviews() {
    if (numOfPage == null) {
      init();
    }
    if (numOfPage >= 1) {
      List<Review> list = reviewApi(pageSize, numOfPage).getReviews();
      numOfPage--;
      return list;
    }
    return Collections.emptyList();
  }

  private ReviewResponse reviewApi(Integer pagesize, Integer page) {
    String URL = String.format(reviewRestAPI, courseID, page, pagesize);

    Client restClient = Client.create();
    WebResource resource = restClient.resource(URL);

    resource.accept(MediaType.APPLICATION_JSON).header("Authorization", TOKEN)
        .header("Content-Type", "application/json;charset=utf-8");

    ClientResponse res = null;
    try {
      logger.info("Sending request to course-review udemy rest end-point");
      res = resource.get(ClientResponse.class);
    } catch (UniformInterfaceException | ClientHandlerException e1) {
      logger.error("Error occured in request to fetch course review from udemy rest end-point");
    } finally {
      restClient.destroy();
    }

    if (res.getStatus() == 200) {
      logger.info("Successfully fetched reviews from udemy rest point with status:[{}]",
          res.getStatus());
      String reviewStr = res.getEntity(String.class);
      JsonObject reviews = null;
      try {
        reviews = parseToJson(reviewStr);
      } catch (JsonSyntaxException e) {
        logger.error(
            "Error while parsing data errorMessage:[{}], errorCause:[{}],errorStackTrace:[{}]",
            e.getMessage(), e.getCause(), e.getStackTrace());
      }
      ReviewResponse reviewResponse =
          new ReviewResponse(checkJsonHasANDNotNull(reviews, ReviewResponse.NEXT),
              checkJsonHasANDNotNull(reviews, ReviewResponse.PREVIOUS),
              getReviewList(reviews.has(ReviewResponse.RESULT)
                  ? reviews.get(ReviewResponse.RESULT).getAsJsonArray()
                  : null),
              Integer.valueOf(checkJsonHasANDNotNull(reviews, ReviewResponse.COUNT)));
      logger.info("Successfully fetched [{}] from udemy rest end-point",
          reviewResponse.getReviews().size());
      return reviewResponse;
    }

    return null;
  }

  private JsonObject parseToJson(String str) throws JsonSyntaxException {
    return parser.parse(str).getAsJsonObject();
  }

  private String checkJsonHasANDNotNull(JsonObject jsondata, String field) {
    return jsondata.has(field)
        ? (!jsondata.get(field).isJsonNull() ? jsondata.get(field).getAsString() : "")
        : "";
  }

  private List<Review> getReviewList(JsonArray reviews) {
    if (reviews == null || reviews.size() == 0) {
      return null;
    }
    List<Review> reviewList = new ArrayList<Review>();

    for (JsonElement reviewElement : reviews) {
      JsonObject reviewJson = reviewElement.getAsJsonObject();
      Review review = getReviewList(reviewJson);
      reviewList.add(review);
    }
    return reviewList;
  }

  private Review getReviewList(JsonObject reviewJson) {
    Review review = new Review();
    review.setId(Integer.valueOf(checkJsonHasANDNotNull(reviewJson, Review.ID)));
    review.setContent(checkJsonHasANDNotNull(reviewJson, Review.CONTENT));
    review.setRating(Double.valueOf(checkJsonHasANDNotNull(reviewJson, Review.RATING)));
    review.setCreated(checkJsonHasANDNotNull(reviewJson, Review.CREATED));
    review.setModified(checkJsonHasANDNotNull(reviewJson, Review.MODIFIED));
    review.setUserModified(checkJsonHasANDNotNull(reviewJson, Review.USER_MODIFIED));
    review.setUser(getUserDetail(
        reviewJson.has(Review.USER) ? reviewJson.get(Review.USER).getAsJsonObject() : null));
    review.setCourse(getCourseDetail(
        reviewJson.has(Review.COURSE) ? reviewJson.get(Review.COURSE).getAsJsonObject() : null));

    return review;
  }

  private Course getCourseDetail(JsonObject courseData) {
    Course course = new Course();
    if (courseData == null || courseData.size() == 0) {
      logger.warn("Course infomation might be null or empty.");
      courseData = courseDetailJson;
    }

    course.setId(Integer.valueOf(checkJsonHasANDNotNull(courseData, Course.ID)));
    course.setTitle(checkJsonHasANDNotNull(courseData, Course.TITLE));
    course.setUrl(checkJsonHasANDNotNull(courseData, Course.URL));
    return course;
  }

  private User getUserDetail(JsonObject userData) {
    if (userData == null || userData.size() == 0) {
      logger.warn("User infomation might be null or empty.");
      return null;
    }
    User user = new User();
    user.setTitle(checkJsonHasANDNotNull(userData, User.TITLE));
    user.setName(checkJsonHasANDNotNull(userData, User.NAME));
    user.setDisplayName(checkJsonHasANDNotNull(userData, User.DISPLAY_NAME));
    return user;
  }
}
