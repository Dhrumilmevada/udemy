package com.dhrumil.udemy.client;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dhrumil.udemy.model.CourseResponse;
import com.dhrumil.udemy.model.InstructorDetail;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

public class UdemyCourseDetailClient {

  private static final Logger logger = LoggerFactory.getLogger(UdemyCourseDetailClient.class);
  private static final String TOKEN = "******";

  private final String courseRestAPI = "https://www.udemy.com/api-2.0/courses/%s";
  private JsonParser parser;

  public UdemyCourseDetailClient() {
    super();
    parser = new JsonParser();
  }

  public CourseResponse getCourseDetail(int courseID) {
    String URL = String.format(courseRestAPI, courseID);

    Client restClient = Client.create();

    WebResource resourses = restClient.resource(URL);

    resourses.accept(MediaType.APPLICATION_JSON)
        .header("Content-Type", "application/json;charset=utf-8").header("Authorization", TOKEN);

    ClientResponse res = null;
    try {
      logger.info("Sending request to course detail udemy rest end-point");
      res = resourses.get(ClientResponse.class);
    } catch (ClientHandlerException | UniformInterfaceException e) {
      logger.error("Error occured in request to fetch course detail from udemy rest end-point");
    } finally {
      restClient.destroy();
    }

    if (res.getStatus() == 200) {
      logger.info("Fetched course detail from udemy rest end-point with status:[{}]",
          res.getStatus());
      String responseStr = res.getEntity(String.class);
      JsonObject courseDetail = null;
      try {
        courseDetail = parseToJson(responseStr);
      } catch (Exception e) {
        logger.error("Invalid json data [{}]", responseStr);
      }
      CourseResponse courseResponse = new CourseResponse(
          Long.valueOf(checkJsonHasANDNotNull(courseDetail, CourseResponse.ID)).longValue(),
          checkJsonHasANDNotNull(courseDetail, CourseResponse.TITLE),
          checkJsonHasANDNotNull(courseDetail, CourseResponse.URL),
          Boolean.valueOf(checkJsonHasANDNotNull(courseDetail, CourseResponse.IS_PAID)),
          Double.valueOf(checkJsonHasANDNotNull(courseDetail.has(CourseResponse.PRICE_DETAIL)
              ? courseDetail.get(CourseResponse.PRICE_DETAIL).getAsJsonObject()
              : new JsonObject(), CourseResponse.PRICE)),
          checkJsonHasANDNotNull(courseDetail.has(CourseResponse.PRICE_DETAIL)
              ? courseDetail.get(CourseResponse.PRICE_DETAIL).getAsJsonObject()
              : new JsonObject(), CourseResponse.CURRENCY),
          getInstructorList(courseDetail.has(CourseResponse.INSTRUCTORS)
              ? courseDetail.get(CourseResponse.INSTRUCTORS).getAsJsonArray()
              : null),
          Boolean.valueOf(checkJsonHasANDNotNull(courseDetail, CourseResponse.IS_PRACTISE_COURSE)),
          checkJsonHasANDNotNull(courseDetail, CourseResponse.PUBLISHED_TITLE));
      logger.info("Fetched course detail [{}]", courseResponse.toString());
      return courseResponse;
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

  private List<InstructorDetail> getInstructorList(JsonArray instructors) {
    int instructorCount = instructors.size();
    List<InstructorDetail> instructorList = new ArrayList<InstructorDetail>();

    if (instructorCount <= 0) {
      logger.info("Invalid instructor detail with instructor count [{}]", instructors.size());
      return null;
    }

    for (int i = 0; i < instructorCount; i++) {
      JsonObject instructorJson = instructors.get(i).getAsJsonObject();
      InstructorDetail instructor = new InstructorDetail();
      instructor.setTitle(checkJsonHasANDNotNull(instructorJson, InstructorDetail.TITLE));
      instructor.setName(checkJsonHasANDNotNull(instructorJson, InstructorDetail.NAME));
      instructor
          .setDisplayName(checkJsonHasANDNotNull(instructorJson, InstructorDetail.DISPLAY_NAME));
      instructor.setJobTitle(checkJsonHasANDNotNull(instructorJson, InstructorDetail.JOB_TITLE));
      instructor.setUrl(checkJsonHasANDNotNull(instructorJson, InstructorDetail.URL));
      instructorList.add(instructor);
    }
    return instructorList;
  }
}
