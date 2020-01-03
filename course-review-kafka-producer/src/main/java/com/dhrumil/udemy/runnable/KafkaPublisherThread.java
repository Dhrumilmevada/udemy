package com.dhrumil.udemy.runnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dhrumil.udemy.model.Review;
import com.dhrumil.udemy.review.main.AppConfig;
import com.google.gson.Gson;

public class KafkaPublisherThread implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(KafkaPublisherThread.class);

  private String bootstrapServers;
  private AppConfig appConfig;
  private ArrayBlockingQueue<Review> reviewQueue;
  private CountDownLatch latch;
  private AdminClient kafkaClient;
  private KafkaProducer<String, String> kafkaProducer;
  private static Integer totalReviewCount = 0;


  public KafkaPublisherThread(AppConfig appConfig, ArrayBlockingQueue<Review> reviewQueue,
      CountDownLatch latch) {
    super();
    this.appConfig = appConfig;
    bootstrapServers = appConfig.getBootstrapServers();
    this.reviewQueue = reviewQueue;
    this.latch = latch;
    Properties kafkaConfig = new Properties();
    kafkaConfig.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

    kafkaClient = AdminClient.create(kafkaConfig);
    this.checkKafkaTopic(appConfig.getKafakaTopic());

    this.kafkaProducer = createKafkaProducer();
  }

  class KafkaCallback implements Callback {
    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {
      if (exception != null) {
        logger.error(
            "Error while publishing data to kafka errorMessage:[{}] , errorCause:[{}] ,errorStackTrace:[{}]",
            exception.getMessage(), exception.getCause(), exception.getStackTrace());
      } else {
        logger.info("Publish data to kafka topic [{}] with partition:[{}] and offset:[{}]",
            metadata.topic(), metadata.partition(), metadata.offset());
      }
    }
  }

  private KafkaProducer<String, String> createKafkaProducer() {
    Properties configs = new Properties();
    configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, appConfig.getBootstrapServers());
    configs.put(ProducerConfig.ACKS_CONFIG, "all");
    configs.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
    configs.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");
    configs.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
    configs.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
    configs.put(ProducerConfig.LINGER_MS_CONFIG, "50");
    configs.put(ProducerConfig.BATCH_SIZE_CONFIG, String.valueOf(64 * 1024));
    configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    logger.info("Kafka producer is created with properties : [{}]", configs);

    return new KafkaProducer<>(configs);
  }

  @Override
  public void run() {
    try {
      while (true) {
        if (reviewQueue.size() == 0) {
          logger.warn("Review queue is empty. Waiting to pull data from queue");
          Thread.sleep(500);
          continue;
        }
        Review review = reviewQueue.poll();
        if (review == null) {
          Thread.sleep(500);
          logger.warn("invalid review : [{}]", review);
        } else {
          kafkaProducer.send(new ProducerRecord<String, String>(appConfig.getKafakaTopic(),
              review.getId().toString(), new Gson().toJson(review)), new KafkaCallback());
          logger.info("Published review with count [{}] : [{}] to kafka topic", ++totalReviewCount,
              review);
        }
      }
    } catch (InterruptedException e) {
      logger.error(
          "Error occured with publishing review to kafka topic errorMessage:[{}] , errorCause:[{}] , errorStackStrace:[{}]",
          e.getMessage(), e.getCause(), e.getStackTrace());
    } finally {
      this.close();
    }

  }

  private void checkKafkaTopic(String topicName) {
    try {
      if (!this.isKafkaTopicExists(topicName)) {
        logger.warn("[{}] topic does not exists in kafka cluster", topicName);
        boolean iscreated = this.createKafkaTopic(topicName);
        if (iscreated) {
          logger.info("[{}] topic successfully created in kafka cluster", topicName);
        }
      }
      logger.info("[{}] topic has been created in kafka cluster", topicName);
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

  private void close() {
    latch.countDown();
    logger.info("Decreasing countDownLach count to [{}]", latch.getCount());
  }

}
