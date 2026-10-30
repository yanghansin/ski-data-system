package io.swagger.client.model;

import java.util.Objects;
import com.google.gson.annotations.SerializedName;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * LiftRide
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2025-02-11T10:30:20.214898800-08:00[America/Los_Angeles]")
public class LiftRide {

  @SerializedName("time")
  private Integer time = null;

  @SerializedName("liftID")
  private Integer liftID = null;

  @SerializedName("skierID")
  private Integer skierID = null;

  @SerializedName("resortID")
  private Integer resortID = null;

  @SerializedName("seasonID")
  private String seasonID = null;

  @SerializedName("dayID")
  private String dayID = null;

  public LiftRide time(Integer time) {
    this.time = time;
    return this;
  }

  /**
   * Get time
   * @return time
   **/
  @Schema(example = "217", description = "")
  public Integer getTime() {
    return time;
  }

  public void setTime(Integer time) {
    this.time = time;
  }

  public LiftRide liftID(Integer liftID) {
    this.liftID = liftID;
    return this;
  }

  /**
   * Get liftID
   * @return liftID
   **/
  @Schema(example = "21", description = "")
  public Integer getLiftID() {
    return liftID;
  }

  public void setLiftID(Integer liftID) {
    this.liftID = liftID;
  }

  public LiftRide skierID(Integer skierID) {
    this.skierID = skierID;
    return this;
  }

  /**
   * Get skierID
   * @return skierID
   **/
  @Schema(example = "123", description = "")
  public Integer getSkierID() {
    return skierID;
  }

  public void setSkierID(Integer skierID) {
    this.skierID = skierID;
  }

  public LiftRide resortID(Integer resortID) {
    this.resortID = resortID;
    return this;
  }

  /**
   * Get resortID
   * @return resortID
   **/
  @Schema(example = "12", description = "")
  public Integer getResortID() {
    return resortID;
  }

  public void setResortID(Integer resortID) {
    this.resortID = resortID;
  }

  public LiftRide seasonID(String seasonID) {
    this.seasonID = seasonID;
    return this;
  }

  /**
   * Get seasonID
   * @return seasonID
   **/
  @Schema(example = "2025", description = "")
  public String getSeasonID() {
    return seasonID;
  }

  public void setSeasonID(String seasonID) {
    this.seasonID = seasonID;
  }

  public LiftRide dayID(String dayID) {
    this.dayID = dayID;
    return this;
  }

  /**
   * Get dayID
   * @return dayID
   **/
  @Schema(example = "1", description = "")
  public String getDayID() {
    return dayID;
  }

  public void setDayID(String dayID) {
    this.dayID = dayID;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LiftRide liftRide = (LiftRide) o;
    return Objects.equals(this.time, liftRide.time) &&
            Objects.equals(this.liftID, liftRide.liftID) &&
            Objects.equals(this.skierID, liftRide.skierID) &&
            Objects.equals(this.resortID, liftRide.resortID) &&
            Objects.equals(this.seasonID, liftRide.seasonID) &&
            Objects.equals(this.dayID, liftRide.dayID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(time, liftID, skierID, resortID, seasonID, dayID);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LiftRide {\n");
    sb.append("    time: ").append(toIndentedString(time)).append("\n");
    sb.append("    liftID: ").append(toIndentedString(liftID)).append("\n");
    sb.append("    skierID: ").append(toIndentedString(skierID)).append("\n");
    sb.append("    resortID: ").append(toIndentedString(resortID)).append("\n");
    sb.append("    seasonID: ").append(toIndentedString(seasonID)).append("\n");
    sb.append("    dayID: ").append(toIndentedString(dayID)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}