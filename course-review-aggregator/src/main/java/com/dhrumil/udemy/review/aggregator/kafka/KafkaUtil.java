package com.dhrumil.udemy.review.aggregator.kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dhrumil.udemy.review.aggregator.main.AppConfig;

public class KafkaUtil {

  private static final Logger logger = LoggerFactory.getLogger(KafkaUtil.class);
  private AdminClient kafkaClient;

  public KafkaUtil(AppConfig appConfig) {
    super();
    Properties kafkaConfig = new Properties();
    kafkaConfig.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, appConfig.getBootstrapServer());
    this.kafkaClient = AdminClient.create(kafkaConfig);

  }

  public void checkKafkaTopic(String topicName) {
    try {
      if (!this.isKafkaTopicExists(topicName)) {
        logger.warn("[{}] topic does not exists in kafka cluster", topicName);
        boolean iscreated = this.createKafkaTopic(topicName);
        if (iscreated) {
          logger.info("[{}] topic successfully created in kafka cluster", topicName);
        }
      } else {
        logger.info("[{}] topic already exists in kafka cluster", topicName);
      }
    } catch (InterruptedException | ExecutionException e) {
      logger.error(
          "Error while creating kafka topic errorMessage:[{}],errorStackTrace:[{}],errorCause:[{}]",
          e.getMessage(), e.getStackTrace(), e.getCause());
    }
  }

  private boolean createKafkaTopic(String topicName) {
    List<NewTopic> topicList = new ArrayList<NewTopic>();
    Integer partition = 3;
    Short replication = 1;
    NewTopic newTopic = new NewTopic(topicName, partition, replication);
    topicList.add(newTopic);
    CreateTopicsResult topicCreationResult = kafkaClient.createTopics(topicList);
    return topicCreationResult.values().get(topicName).isDone();
  }

  private boolean isKafkaTopicExists(String topicName)
      throws InterruptedException, ExecutionException {
    ListTopicsResult topics = kafkaClient.listTopics();

    return topics.names().get().contains(topicName);
  }


}
