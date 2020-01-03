package com.dhrumil.udemy.model;

public class Course {

  public static final String ID = "id";
  public static final String TITLE = "title";
  public static final String URL = "url";

  private Integer id;
  private String title;
  private String url;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
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

  @Override
  public String toString() {
    return "Course [id=" + id + ", title=" + title + ", url=" + url + "]";
  }
}
