package com.dhrumil.udemy.review.aggregator.main;

import com.typesafe.config.Config;

public class AppConfig {

  private final String serviceName;
  private final String bootstrapServer;
  private final String topicToRead;
  private final String topicToWrite;
  private final String applicationId;

  public AppConfig(Config config) {

    serviceName = config.getString("app.name");
    bootstrapServer = config.getString("app.kafka.bootstrap.servers");
    topicToRead = config.getString("app.kafka.topic.consume");
    topicToWrite = config.getString("app.kafka.topic.long.term.stat");
    applicationId = config.getString("app.kafka.application.id");
  }

  public String getServiceName() {
    return serviceName;
  }

  public String getBootstrapServer() {
    return bootstrapServer;
  }

  public String getTopicToRead() {
    return topicToRead;
  }

  public String getTopicToWrite() {
    return topicToWrite;
  }

  public String getApplicationId() {
    return applicationId;
  }
}
