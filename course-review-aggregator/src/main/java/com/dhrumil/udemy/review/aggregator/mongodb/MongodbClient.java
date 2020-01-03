package com.dhrumil.udemy.review.aggregator.mongodb;

import java.util.Map;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dhrumil.udemy.review.aggregator.model.CourseStatistic;
import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class MongodbClient {

  private static final Logger logger = LoggerFactory.getLogger(MongodbClient.class);
  private static final String DATABASE_NAME = "udemy";
  private static final String REVIEW_AGGREGATION = "courseLongtermStat";

  private MongoClient dbClient;
  private MongoDatabase udemyDB;
  private static MongodbClient dbobject = null;

  private MongodbClient() {
    super();
    dbClient = new MongoClient();
    udemyDB = dbClient.getDatabase(DATABASE_NAME);
    logger.info("Created MongoDB client and initialized database with name:[{}]", DATABASE_NAME);
  }

  public static MongodbClient getInstance() {
    if (dbobject == null) {
      dbobject = new MongodbClient();
    }
    return dbobject;
  }

  public void getOrCreateReviewStatCollection() {
    boolean hasCollection = collectionExists(REVIEW_AGGREGATION);

    if (!hasCollection) {
      udemyDB.createCollection(REVIEW_AGGREGATION);
      logger.info("Created [{}] collection in database [{}]", REVIEW_AGGREGATION, DATABASE_NAME);
    }
  }

  private boolean collectionExists(String collectionName) {
    MongoCursor<String> collectionCursor = udemyDB.listCollectionNames().iterator();
    logger.info("Checking if collection with name [{}] is exists or not", collectionName);
    while (collectionCursor.hasNext()) {
      String collection = collectionCursor.next();
      if (collection.equals(collectionName)) {
        logger.info("Collection [{}] is exists in [{}] in database", collectionName, DATABASE_NAME);
        return true;
      }
    }
    logger.info(
        "Collection [{}] does not exists in [{}] in database , need to create [{}] collection",
        collectionName, DATABASE_NAME, collectionName);
    return false;
  }

  @SuppressWarnings("unchecked")
  public void insertIntoReviewStatCollection(String data) {
    MongoCollection<Document> collection = udemyDB.getCollection(REVIEW_AGGREGATION);
    CourseStatistic courseStatistic = new Gson().fromJson(data, CourseStatistic.class);
    String collectionKey = String.valueOf(courseStatistic.getCourseID());
    boolean documentExists =
        isCourseDocumentExists(collection, collectionKey);

    if (!documentExists) {
      Gson gson = new Gson();
      Map<String, Object> dataMap = gson.fromJson(data, Map.class);
      dataMap.put("_id", collectionKey);
      logger.info("Inserting data [{}] in [{}] collection in [{}] database", data,
          REVIEW_AGGREGATION, DATABASE_NAME);
      Document doc = new Document(dataMap);
      collection.insertOne(new Document(doc));
    } else {
      logger.info("Data with id [{}] already exists in [{}] collection", collectionKey,
          REVIEW_AGGREGATION);

      BasicDBObject query = new BasicDBObject();
      query.put(CourseStatistic.ID, courseStatistic.getCourseID());
      
      BasicDBObject updatedField = new BasicDBObject();
      updatedField.put(CourseStatistic.REVIEW_SUM, courseStatistic.getSumOfRating());
      updatedField.put(CourseStatistic.REVIEW_COUNT, courseStatistic.getTotalReview());
      updatedField.put(CourseStatistic.AVERAGE, courseStatistic.getAverageRating());
      updatedField.put(CourseStatistic.ZERO_STAR, courseStatistic.getCountZeroStar());
      updatedField.put(CourseStatistic.ONE_STAR, courseStatistic.getCountOneStar());
      updatedField.put(CourseStatistic.TWO_STAR, courseStatistic.getCountTwoStar());
      updatedField.put(CourseStatistic.THREE_STAR, courseStatistic.getCountThreeStar());
      updatedField.put(CourseStatistic.FOUR_STAR, courseStatistic.getCountFourStar());
      updatedField.put(CourseStatistic.FIVE_STAR, courseStatistic.getCountFiveStar());
      
      BasicDBObject update = new BasicDBObject();
      update.put("$set", updatedField);

      collection.updateOne(query, update);
      logger.info("Data with id [{}] already exists in [{}] collection", collectionKey,
          REVIEW_AGGREGATION);
      logger.info("Updates averageRating:[{}] field in [{}] collection ",
          courseStatistic.getAverageRating(), REVIEW_AGGREGATION);

    }
  }

  private boolean isCourseDocumentExists(MongoCollection<Document> collection,
      String collectionKey) {

    BasicDBObject query = new BasicDBObject();
    query.put("_id", collectionKey);
    Document document = collection.find(query).first();
    if (document == null) {
      logger.info("Document with _id:[{}] is not exist in collection [{}]", collectionKey,
          REVIEW_AGGREGATION);
      return false;
    } else {
      return true;
    }
  }

}
