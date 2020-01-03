package com.dhrumil.udemy.model;

public class InstructorDetail {

  public static final String TITLE = "title";
  public static final String NAME = "name";
  public static final String DISPLAY_NAME = "display_name";
  public static final String JOB_TITLE = "job_title";
  public static final String URL = "url";

  private String title;
  private String name;
  private String displayName;
  private String jobTitle;
  private String url;

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

  public String getJobTitle() {
    return jobTitle;
  }

  public void setJobTitle(String jobTitle) {
    this.jobTitle = jobTitle;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  @Override
  public String toString() {
    return "InstructorDetail [title=" + title + ", name=" + name + ", displayName=" + displayName
        + ", jobTitle=" + jobTitle + ", url=" + url + "]";
  }
}
