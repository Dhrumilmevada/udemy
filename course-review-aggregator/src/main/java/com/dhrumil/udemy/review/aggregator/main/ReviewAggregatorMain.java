package com.dhrumil.udemy.review.aggregator.main;

import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dhrumil.udemy.review.aggregator.kafka.KafkaUtil;
import com.dhrumil.udemy.review.aggregator.model.CourseStatistic;
import com.dhrumil.udemy.review.aggregator.model.Review;
import com.dhrumil.udemy.review.aggregator.mongodb.MongodbClient;
import com.google.gson.Gson;
import com.typesafe.config.ConfigFactory;

public class ReviewAggregatorMain {

  private static final Logger logger = LoggerFactory.getLogger(ReviewAggregatorMain.class);

  private AppConfig appconfig;
  private Gson gson;
  private KafkaStreams stream;
  private KafkaUtil kafkaUtils;
  private MongodbClient dbClient;

  public static void main(String[] args) {
    ReviewAggregatorMain main = new ReviewAggregatorMain();
    main.init();
    main.start();
  }

  public void init() {
    appconfig = new AppConfig(ConfigFactory.load());
    logger.info("[{}] app has been initialized", appconfig.getServiceName());

    logger.info("[{}] app has been initialized", appconfig.getServiceName());
    addShutDownHook();
    gson = new Gson();
    kafkaUtils = new KafkaUtil(appconfig);
    kafkaUtils.checkKafkaTopic(appconfig.getTopicToWrite());
    dbClient = MongodbClient.getInstance();
    dbClient.getOrCreateReviewStatCollection();
  }

  public void start() {
    logger.info("[{}] app has been started", appconfig.getServiceName());
    Properties streamConfig = getKafkaStremConfiguration();
    logger.info("Created Stream configuration with properties : [{}]", streamConfig);
    Topology aggregatorTopology = createTopology(streamConfig);
    stream = new KafkaStreams(aggregatorTopology, streamConfig);
    stream.cleanUp();
    stream.start();
  }


  private Properties getKafkaStremConfiguration() {
    Properties config = new Properties();
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, appconfig.getBootstrapServer());
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, appconfig.getApplicationId());
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    config.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);
    config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    return config;
  }

  private Topology createTopology(Properties config) {

    StreamsBuilder builder = new StreamsBuilder();

    KStream<String, String> validReview = builder.stream(appconfig.getTopicToRead());
    KStream<String, String> validReviewCourseIdAskey =
        validReview.selectKey((key, review) -> getReview(review).getCourse().getId().toString());

    KTable<String, String> longTermStat =
        validReviewCourseIdAskey.groupByKey().<String>aggregate(this::initCourseStat,
            (key, review, oldStat) -> this.reviewAggregator(key, review, oldStat));

    longTermStat.toStream().peek((key, value) -> dbClient.insertIntoReviewStatCollection(value))
        .to(appconfig.getTopicToWrite());
    return builder.build();
  }

  private String initCourseStat() {
    gson = new Gson();
    CourseStatistic courseStat = new CourseStatistic();
    String courseStatStr = gson.toJson(courseStat);
    return courseStatStr;
  }

  private String reviewAggregator(String CourseId, String reviewStr, String oldStatStr) {
    gson = new Gson();
    CourseStatistic oldStat = gson.fromJson(oldStatStr, CourseStatistic.class);
    CourseStatistic newStat = new CourseStatistic(oldStat);
    Review review = this.getReview(reviewStr);

    newStat.setCourseID(review.getCourse().getId());
    newStat.setCourseTitle(review.getCourse().getTitle());

    newStat.setTotalReview(newStat.getTotalReview() + 1);
    newStat.setSumOfRating(newStat.getSumOfRating() + review.getRating());
    newStat.setAverageRating(newStat.getSumOfRating() / newStat.getTotalReview());

    String rating = review.getRating().toString();

    switch (rating) {
      case "0.0":
      case "0.5":
        newStat.setCountZeroStar(newStat.getCountZeroStar() + 1);
        break;
      case "1.0":
      case "1.5":
        newStat.setCountOneStar(newStat.getCountOneStar() + 1);
        break;
      case "2.0":
      case "2.5":
        newStat.setCountTwoStar(newStat.getCountTwoStar() + 1);
        break;
      case "3.0":
      case "3.5":
        newStat.setCountThreeStar(newStat.getCountThreeStar() + 1);
        break;
      case "4.0":
      case "4.5":
        newStat.setCountFourStar(newStat.getCountFourStar() + 1);
        break;
      case "5.0":
        newStat.setCountFiveStar(newStat.getCountFiveStar() + 1);
        break;
    }
    return gson.toJson(newStat);
  }

  private Review getReview(String reviewStr) {
    gson = new Gson();
    Review review = gson.fromJson(reviewStr, Review.class);
    return review;
  }

  private void addShutDownHook() {
    logger.info("Shutdown hook initiated...");
    Runtime.getRuntime().addShutdownHook(new Thread(() -> stream.close()));
  }
}
