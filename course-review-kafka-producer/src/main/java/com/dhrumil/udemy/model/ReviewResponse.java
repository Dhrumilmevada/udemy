package com.dhrumil.udemy.model;

import java.util.List;

public class ReviewResponse {


  public static final String NEXT = "next";
  public static final String PREVIOUS = "previous";
  public static final String COUNT = "count";
  public static final String RESULT = "results";

  private Integer count;
  private String next;
  private String previous;
  List<Review> reviews;

  public ReviewResponse(String next, String previous, List<Review> reviews, Integer count) {
    super();
    this.next = next;
    this.previous = previous;
    this.reviews = reviews;
    this.count = count;
  }

  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public String getNext() {
    return next;
  }

  public void setNext(String next) {
    this.next = next;
  }

  public String getPrevious() {
    return previous;
  }

  public void setPrevious(String previous) {
    this.previous = previous;
  }

  public List<Review> getReviews() {
    return reviews;
  }

  public void setReviews(List<Review> reviews) {
    this.reviews = reviews;
  }

  @Override
  public String toString() {
    return "ReviewResponse [count=" + count + ", next=" + next + ", previous=" + previous
        + ", reviews=" + reviews + "]";
  }
}
