package com.dhrumil.udemy.review.filter.main;

import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dhrumil.udemy.review.filter.kafka.KafkaUtil;
import com.dhrumil.udemy.review.filter.mongodb.MongodbClient;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.typesafe.config.ConfigFactory;

public class ReviewFilterMain {

  private static final Logger logger = LoggerFactory.getLogger(ReviewFilterMain.class);

  public static final String UPVOTE = "upvote";
  public static final String DOWNVOTE = "downvote";
  public static final String IS_REPORTED = "isReported";
  public static final String REPORTCOUNT = "reportCount";
  public static final String COURSE_ID = "id";
  public static final String REVIEW_ID = "id";
  public static final String COURSE = "course";
  public static final String COURSE_TITLE = "title";
  public static final String COURSE_URL = "url";

  private AppConfig appConfig;
  private static JsonParser parser;
  private KafkaStreams filterStream;
  private MongodbClient dbClient;
  private KafkaUtil kafkaUtil;

  public static void main(String args[]) {
    ReviewFilterMain reviewFilter = new ReviewFilterMain();
    reviewFilter.init();
    reviewFilter.start();
  }

  public void init() {
    appConfig = new AppConfig(ConfigFactory.load());
    logger.info("[{}] app has been initialized", appConfig.getServiceName());
    addShutDownHook();
    parser = new JsonParser();
    dbClient = MongodbClient.getInstance();
    dbClient.getOrCreateReviewCollection();
    kafkaUtil = new KafkaUtil(appConfig);
    kafkaUtil.checkKafkaTopic(appConfig.getPublishValidReview());
    kafkaUtil.checkKafkaTopic(appConfig.getPublishFraudReview());
  }

  public void start() {
    logger.info("[{}] app has been started", appConfig.getServiceName());
    Properties streamConfig = getKafkaStremConfiguration();
    logger.info("Created Stream configuration with properties : [{}]", streamConfig);
    Topology filterTopology = createTopology(streamConfig);
    filterStream = new KafkaStreams(filterTopology, streamConfig);
    filterStream.cleanUp();
    filterStream.start();
    logger.info("Started filtering reviews");
  }

  private Properties getKafkaStremConfiguration() {
    Properties config = new Properties();
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, appConfig.getBootstrapServers());
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, appConfig.getApplicationID());
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    config.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);
    config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    return config;
  }

  @SuppressWarnings("unchecked")
  private Topology createTopology(Properties config) {
    StreamsBuilder builder = new StreamsBuilder();
    KStream<String, String> reviewStream = builder.stream(appConfig.getTopicToConsume());

    KStream<String, String> banches[] =
        reviewStream.branch((key, review) -> isvalideReview(review), (key, review) -> true);

    KStream<String, String> validReview = banches[0];
    KStream<String, String> fraudReview = banches[1];

    validReview.peek((key, review) -> dbClient.insertIntoReviewCollection(review))
        .to(appConfig.getPublishValidReview());
    fraudReview.to(appConfig.getPublishFraudReview());


    return builder.build();
  }

  private boolean isvalideReview(String review) {
    JsonObject reviewJson = null;
    try {
      reviewJson = parseToJson(review);
    } catch (JsonSyntaxException e) {
      logger.error(
          "Exception while parsing to json data errorMessage:[{}], errorStackTrace:[{}], errorCause:[{}]",
          e.getMessage(), e.getStackTrace(), e.getCause());
    }

    boolean isNotEffective =
        !(reviewJson.get(DOWNVOTE).getAsInt() == reviewJson.get(UPVOTE).getAsInt())
            && reviewJson.get(DOWNVOTE).getAsInt() > reviewJson.get(UPVOTE).getAsInt();

    boolean reported = (reviewJson.get(IS_REPORTED).getAsBoolean() == true)
        && reviewJson.get(REPORTCOUNT).getAsInt() > 0;

    if (isNotEffective || reported) {
      logger.warn(
          "Invalid review with field upvote:[{}], downvote:[{}] , reportCount:[{}] publishing to kafka topic :[{}]",
          reviewJson.get(UPVOTE), reviewJson.get(DOWNVOTE), reviewJson.get(REPORTCOUNT),
          appConfig.getPublishFraudReview());
      return false;
    }
    logger.info("Found valid review, publishing to kafka topic:[{}]",
        appConfig.getPublishValidReview());
    return true;
  }

  public static JsonObject parseToJson(String data) throws JsonSyntaxException {
    return parser.parse(data).getAsJsonObject();
  }

  private void addShutDownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> filterStream.close()));
  }
}
