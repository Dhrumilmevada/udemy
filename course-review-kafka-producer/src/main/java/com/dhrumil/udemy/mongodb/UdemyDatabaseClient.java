package com.dhrumil.udemy.mongodb;

import java.util.Map;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dhrumil.udemy.model.CourseResponse;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class UdemyDatabaseClient {

  private static final Logger logger = LoggerFactory.getLogger(UdemyDatabaseClient.class);
  private static final String DATABASE_NAME = "udemy";
  private static final String COURSE_COLLECTION = "course";

  private MongoClient dbClient;
  private MongoDatabase udemyDB;
  private static UdemyDatabaseClient dbobject = null;

  private UdemyDatabaseClient() {
    super();
    dbClient = new MongoClient();
    udemyDB = dbClient.getDatabase(DATABASE_NAME);
    logger.info("Created MongoDB client and initialized database with name:[{}]", DATABASE_NAME);

  }

  public static UdemyDatabaseClient getInstance() {
    if (dbobject == null) {
      dbobject = new UdemyDatabaseClient();
    }
    return dbobject;
  }

  public void getOrCreateCourseCollection() {
    boolean hasCollection = collectionExists(COURSE_COLLECTION);

    if (!hasCollection) {
      udemyDB.createCollection(COURSE_COLLECTION);
      logger.info("Created [{}] collection in database [{}]", COURSE_COLLECTION, DATABASE_NAME);
    }
  }

  @SuppressWarnings("unchecked")
  public void insertIntoCourseCollection(CourseResponse data) {
    MongoCollection<Document> collection = udemyDB.getCollection(COURSE_COLLECTION);

    boolean documentExists = isCourseDocumentExists(collection, String.valueOf(data.getId()));

    if (!documentExists) {
      Gson gson = new Gson();
      String dataStr = gson.toJson(data);
      Map<String, Object> dataMap = gson.fromJson(dataStr, Map.class);
      dataMap.put("_id", String.valueOf(data.getId()));
      logger.info("Inserting data [{}] in [{}] collection in [{}] database", dataStr,
          COURSE_COLLECTION, DATABASE_NAME);
      Document doc = new Document(dataMap);
      collection.insertOne(new Document(doc));
    } else {
      logger.info("Data with id [{}] already exists in [{}] collection", data.getId(),
          COURSE_COLLECTION);
    }
  }

  private boolean isCourseDocumentExists(MongoCollection<Document> collection, String courseId) {
    MongoCursor<Document> documents = collection.find().iterator();

    while (documents.hasNext()) {
      Document document = documents.next();
      if (courseId.equals(document.get("_id")))
        return true;
    }
    logger.info("Document with _id:[{}] is not exist in collection [{}]", courseId,
        COURSE_COLLECTION);
    return false;
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
}
