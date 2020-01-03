package com.dhrumil.udemy.review.filter.main;

import com.typesafe.config.Config;

public class AppConfig {

  private final String serviceName;
  private final String bootstrapServers;
  private final String topicToConsume;
  private final String publishValidReview;
  private final String publishFraudReview;
  private final String applicationID;
  
  public AppConfig(Config config) {
    super();
    this.serviceName = config.getString("app.name");
    this.bootstrapServers = config.getString("app.kafka.bootstrap.servers");
    this.topicToConsume = config.getString("app.kafka.topic.consume");
    this.publishValidReview = config.getString("app.kafka.topic.valid.review");
    this.publishFraudReview = config.getString("app.kafka.topic.fraud.review");
    this.applicationID = config.getString("app.kafka.application.id");
  }

  public String getServiceName() {
    return serviceName;
  }

  public String getBootstrapServers() {
    return bootstrapServers;
  }

  public String getTopicToConsume() {
    return topicToConsume;
  }

  public String getPublishValidReview() {
    return publishValidReview;
  }

  public String getPublishFraudReview() {
    return publishFraudReview;
  }

  public String getApplicationID() {
    return applicationID;
  }
}
