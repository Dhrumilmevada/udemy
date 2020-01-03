package com.dhrumil.udemy.review.main;

import com.typesafe.config.Config;

public class AppConfig {

  private final Integer reviewQueueCapacity;
  private final Integer pageSize;
  private final String courseID;
  private final String bootstrapServers;
  private final String kafakaTopic;

  public AppConfig(Config config) {
    super();
    this.reviewQueueCapacity = config.getInt("app.queue.capacity");
    this.pageSize = config.getInt("app.udemy.page.size");
    this.courseID = config.getString("app.course.id");
    this.bootstrapServers = config.getString("app.kafka.bootstrap.servers");
    this.kafakaTopic = config.getString("app.kafka.topic.name");
  }

  public Integer getReviewQueueCapacity() {
    return reviewQueueCapacity;
  }

  public Integer getPageSize() {
    return pageSize;
  }

  public String getCourseID() {
    return courseID;
  }

  public String getBootstrapServers() {
    return bootstrapServers;
  }

  public String getKafakaTopic() {
    return kafakaTopic;
  }



}
