package com.dhrumil.udemy.model;

import java.util.List;

public class CourseResponse {

  public static final String ID = "id";
  public static final String TITLE = "title";
  public static final String URL = "url";
  public static final String IS_PAID = "is_paid";
  public static final String PRICE_DETAIL = "price_detail";
  public static final String PRICE = "amount";
  public static final String CURRENCY = "currency";
  public static final String INSTRUCTORS = "visible_instructors";
  public static final String IS_PRACTISE_COURSE = "is_practice_test_course";
  public static final String PUBLISHED_TITLE = "published_title";


  private long id;
  private String title;
  private String url;
  private boolean isPaid;
  private double price;
  private String currency;
  private List<InstructorDetail> instructors;
  private boolean isPractiseTestCourse;
  private String publishedTitle;



  public CourseResponse(long id, String title, String url, boolean isPaid, Double price,
      String currency, List<InstructorDetail> instructor, boolean isPractiseTestCourse,
      String publishedTitle) {
    super();
    this.id = id;
    this.title = title;
    this.url = url;
    this.isPaid = isPaid;
    this.price = price;
    this.currency = currency;
    this.instructors = instructor;
    this.isPractiseTestCourse = isPractiseTestCourse;
    this.publishedTitle = publishedTitle;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public boolean isPaid() {
    return isPaid;
  }

  public void setPaid(boolean isPaid) {
    this.isPaid = isPaid;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    this.price = price;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public List<InstructorDetail> getInstructor() {
    return instructors;
  }

  public void setInstructor(List<InstructorDetail> instructors) {
    this.instructors = instructors;
  }

  public boolean isPractiseTestCourse() {
    return isPractiseTestCourse;
  }

  public void setPractiseTestCourse(boolean isPractiseTestCourse) {
    this.isPractiseTestCourse = isPractiseTestCourse;
  }

  public String getPublishedTitle() {
    return publishedTitle;
  }

  public void setPublishedTitle(String publishedTitle) {
    this.publishedTitle = publishedTitle;
  }

  @Override
  public String toString() {
    return "CourseResponse [id=" + id + ", title=" + title + ", url=" + url + ", isPaid=" + isPaid
        + ", price=" + price + ", currency=" + currency + ", instructor=" + instructors
        + ", isPractiseTestCourse=" + isPractiseTestCourse + ", publishedTitle=" + publishedTitle
        + "]";
  }
}
