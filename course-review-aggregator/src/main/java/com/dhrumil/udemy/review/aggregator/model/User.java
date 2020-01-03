package com.dhrumil.udemy.review.aggregator.model;

public class User {

  public static final String TITLE = "title";
  public static final String NAME = "name";
  public static final String DISPLAY_NAME = "display_name";

  private String title;
  private String name;
  private String displayName;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public String toString() {
    return "User [title=" + title + ", name=" + name + ", displayName=" + displayName + "]";
  }
}
