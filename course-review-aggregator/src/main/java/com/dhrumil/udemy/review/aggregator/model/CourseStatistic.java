package com.dhrumil.udemy.review.aggregator.model;

public class CourseStatistic {

  public static final String ID = "courseID";
  public static final String REVIEW_COUNT = "totalReview";
  public static final String AVERAGE = "averageRating";
  public static final String REVIEW_SUM = "sumOfRating";
  public static final String ZERO_STAR = "countZeroStar";
  public static final String ONE_STAR = "countOneStar";
  public static final String TWO_STAR = "countTwoStar";
  public static final String THREE_STAR = "countThreeStar";
  public static final String FOUR_STAR = "countFourStar";
  public static final String FIVE_STAR = "countFiveStar";

  private long courseID;
  private String courseTitle;
  private int totalReview;
  private double averageRating;
  private double sumOfRating;
  private int countOneStar;
  private int countZeroStar;
  private int countTwoStar;
  private int countThreeStar;
  private int countFourStar;
  private int countFiveStar;

  public CourseStatistic() {
    this.courseID = 0L;
    this.courseTitle = null;
    this.totalReview = 0;
    this.averageRating = 0L;
    this.sumOfRating = 0L;
    this.countZeroStar = 0;
    this.countOneStar = 0;
    this.countTwoStar = 0;
    this.countThreeStar = 0;
    this.countFourStar = 0;
    this.countFiveStar = 0;
  }

  public CourseStatistic(CourseStatistic oldStat) {
    this.courseID = oldStat.getCourseID();
    this.courseTitle = oldStat.getCourseTitle();
    this.totalReview = oldStat.getTotalReview();
    this.averageRating = oldStat.getAverageRating();
    this.sumOfRating = oldStat.getSumOfRating();
    this.countZeroStar = oldStat.getCountZeroStar();
    this.countOneStar = oldStat.getCountOneStar();
    this.countTwoStar = oldStat.getCountTwoStar();
    this.countThreeStar = oldStat.getCountThreeStar();
    this.countFourStar = oldStat.getCountFourStar();
    this.countFiveStar = oldStat.getCountFiveStar();
  }

  public long getCourseID() {
    return courseID;
  }

  public void setCourseID(long courseID) {
    this.courseID = courseID;
  }

  public String getCourseTitle() {
    return courseTitle;
  }

  public void setCourseTitle(String courseTitle) {
    this.courseTitle = courseTitle;
  }

  public int getTotalReview() {
    return totalReview;
  }

  public void setTotalReview(int totalReview) {
    this.totalReview = totalReview;
  }

  public double getAverageRating() {
    return averageRating;
  }

  public void setAverageRating(double averageRating) {
    this.averageRating = averageRating;
  }

  public double getSumOfRating() {
    return sumOfRating;
  }

  public void setSumOfRating(double sumOfRating) {
    this.sumOfRating = sumOfRating;
  }

  public int getCountOneStar() {
    return countOneStar;
  }

  public void setCountOneStar(int countOneStar) {
    this.countOneStar = countOneStar;
  }

  public int getCountZeroStar() {
    return countZeroStar;
  }

  public void setCountZeroStar(int countZeroStar) {
    this.countZeroStar = countZeroStar;
  }

  public int getCountTwoStar() {
    return countTwoStar;
  }

  public void setCountTwoStar(int countTwoStar) {
    this.countTwoStar = countTwoStar;
  }

  public int getCountThreeStar() {
    return countThreeStar;
  }

  public void setCountThreeStar(int countThreeStar) {
    this.countThreeStar = countThreeStar;
  }

  public int getCountFourStar() {
    return countFourStar;
  }

  public void setCountFourStar(int countFourStar) {
    this.countFourStar = countFourStar;
  }

  public int getCountFiveStar() {
    return countFiveStar;
  }

  public void setCountFiveStar(int countFiveStar) {
    this.countFiveStar = countFiveStar;
  }

  @Override
  public String toString() {
    return "CourseStatistic [courseID=" + courseID + ", courseTitle=" + courseTitle
        + ", totalReview=" + totalReview + ", averageRating=" + averageRating + ", sumOfRating="
        + sumOfRating + ", countOneStar=" + countOneStar + ", countZeroStar=" + countZeroStar
        + ", countTwoStar=" + countTwoStar + ", countThreeStar=" + countThreeStar
        + ", countFourStar=" + countFourStar + ", countFiveStar=" + countFiveStar + "]";
  }
}
