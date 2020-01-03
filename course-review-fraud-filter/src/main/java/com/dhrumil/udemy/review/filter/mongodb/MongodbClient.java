package com.dhrumil.udemy.review.filter.mongodb;

import java.util.Map;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dhrumil.udemy.review.filter.main.ReviewFilterMain;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class MongodbClient {

  private static final Logger logger = LoggerFactory.getLogger(MongodbClient.class);
  private static final String DATABASE_NAME = "udemy";
  private static final String REVIEW_COLLECTION = "review";

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

  public void getOrCreateReviewCollection() {
    boolean hasCollection = collectionExists(REVIEW_COLLECTION);

    if (!hasCollection) {
      udemyDB.createCollection(REVIEW_COLLECTION);
      logger.info("Created [{}] collection in database [{}]", REVIEW_COLLECTION, DATABASE_NAME);
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
  public void insertIntoReviewCollection(String data) {
    MongoCollection<Document> collection = udemyDB.getCollection(REVIEW_COLLECTION);
    JsonObject dataJson = null;
    try {
      dataJson = ReviewFilterMain.parseToJson(data);
    } catch (Exception e) {
      logger.error(
          "Exception while parsing to json data errorMessage:[{}], errorStackTrace:[{}], errorCause:[{}]",
          e.getMessage(), e.getStackTrace(), e.getCause());
    }

    String collectionKey = getCollectionKey(dataJson);
    boolean documentExists = isCourseDocumentExists(collection, collectionKey);

    if (!documentExists) {
      Gson gson = new Gson();
      Map<String, Object> dataMap = gson.fromJson(data, Map.class);
      dataMap.put("_id", collectionKey);
      logger.info("Inserting data [{}] in [{}] collection in [{}] database", data,
          REVIEW_COLLECTION, DATABASE_NAME);
      Document doc = new Document(dataMap);
      collection.insertOne(new Document(doc));
    } else {
      logger.info("Data with id [{}] already exists in [{}] collection", collectionKey,
          REVIEW_COLLECTION);
    }
  }

  private boolean isCourseDocumentExists(MongoCollection<Document> collection,
      String collectionKey) {

    BasicDBObject query = new BasicDBObject();
    query.put("_id", collectionKey);
    Document document = collection.find(query).first();
    if (document == null) {
      logger.info("Document with _id:[{}] is not exist in collection [{}]", collectionKey,
          REVIEW_COLLECTION);
      return false;
    } else {
      return true;
    }
  }

  private String getCollectionKey(JsonObject dataJson) {
    String courseID = dataJson.get(ReviewFilterMain.COURSE).getAsJsonObject()
        .get(ReviewFilterMain.COURSE_ID).getAsString();
    String reviewID = dataJson.get(ReviewFilterMain.REVIEW_ID).getAsString();

    String collectionID = courseID + ":" + reviewID;
    return collectionID;
  }

}
