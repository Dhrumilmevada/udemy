package com.dhrumil.udemy.model;

public class Review {

  public static final String ID = "id";
  public static final String CONTENT = "content";
  public static final String RATING = "rating";
  public static final String CREATED = "created";
  public static final String MODIFIED = "modified";
  public static final String USER_MODIFIED = "user_modified";
  public static final String USER = "user";
  public static final String COURSE = "course";

  private Integer id;
  private String content;
  private Double rating;
  private String created;
  private String modified;
  private String userModified;
  private User user;
  private Course course;
  private Integer upvote = new Integer(0);
  private Integer downvote = new Integer(0);
  private boolean isReported;
  private Integer reportCount = new Integer(0);

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Double getRating() {
    return rating;
  }

  public void setRating(Double rating) {
    this.rating = rating;
  }

  public String getCreated() {
    return created;
  }

  public void setCreated(String created) {
    this.created = created;
  }

  public String getModified() {
    return modified;
  }

  public void setModified(String modified) {
    this.modified = modified;
  }

  public String getUserModified() {
    return userModified;
  }

  public void setUserModified(String userModified) {
    this.userModified = userModified;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Course getCourse() {
    return course;
  }

  public void setCourse(Course course) {
    this.course = course;
  }

  public Integer getUpvote() {
    return upvote;
  }

  public void setUpvote(Integer upvote) {
    if (this.upvote == null) {
      this.upvote = new Integer(upvote);
    } else {
      this.upvote = this.upvote + upvote;
    }
  }

  public Integer getDownvote() {
    return downvote;
  }

  public void setDownvote(Integer downvote) {
    if (this.downvote == null) {
      this.downvote = new Integer(downvote);
    } else {
      this.downvote = this.downvote + downvote;
    }
  }

  public boolean isReported() {
    return isReported;
  }

  public void setReported(boolean isReported) {
    this.isReported = isReported;
  }

  public Integer getReportCount() {
    return reportCount;
  }

  public void setReportCount(Integer reportCount) {
    if (this.reportCount == null) {
      this.reportCount = new Integer(reportCount);
    } else {
      this.reportCount = this.reportCount + reportCount;
    }
  }

  @Override
  public String toString() {
    return "Review [id=" + id + ", content=" + content + ", rating=" + rating + ", created="
        + created + ", modified=" + modified + ", user_modified=" + userModified + ", user=" + user
        + ", course=" + course + ", upvote=" + upvote + ", downvote=" + downvote + ", isReported="
        + isReported + ", reportCount=" + reportCount + "]";
  }
}
