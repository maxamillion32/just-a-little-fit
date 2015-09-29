package ecap.studio.group.justalittlefit.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;

import ecap.studio.group.justalittlefit.database.DbConstants;
import ecap.studio.group.justalittlefit.util.Constants;

/**
 * Class that represents a single set within an {@link Exercise)
 */
public class Set implements Comparable<Set>, Parcelable {

    /** The id of the Set object */
    @DatabaseField(generatedId = true, columnName = DbConstants.SET_ID_COLUMN_NAME)
    private int setId;

    /** Whether or not this Set is shown as completed on UI */
    @DatabaseField(canBeNull = false, columnName = DbConstants.IS_COMPLETE_COLUMN_NAME)
    private boolean isComplete;

    /** The {@link Exercise) that this Set is a part of */
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = DbConstants.EXERCISE_ID_COLUMN_NAME)
    private Exercise exercise;

    /** The number of reps in the Set */
    @DatabaseField(canBeNull = false, columnName = DbConstants.REPS_COLUMN_NAME)
    private int reps;

    /** The measured weight */
    @DatabaseField(columnName = DbConstants.WEIGHT_AMOUNT_COLUMN_NAME)
    private Integer weight;

    /** The measured work of this Set in hours */
    @DatabaseField(columnName = DbConstants.HOURS_COLUMN_NAME)
    private Integer hours;

    /** The measured work of this Set in minutes */
    @DatabaseField(columnName = DbConstants.MINUTES_COLUMN_NAME)
    private Integer minutes;

    /** The measured work of this Set in seconds */
    @DatabaseField(columnName = DbConstants.SECONDS_COLUMN_NAME)
    private Integer seconds;

    /** Either lbs, kgs, or none */
    @DatabaseField(columnName = DbConstants.WEIGHT_CODE_COLUMN_NAME)
    private String weightTypeCode;

    /** Either reps or loggedTime based set */
    @DatabaseField(canBeNull = false, columnName = DbConstants.TYPE_CODE_COLUMN_NAME)
    private String exerciseTypeCode;

    /** The int that determines the display order of the Set object within the app */
    @DatabaseField(canBeNull = false, columnName = DbConstants.ORDER_NUMBER_COLUMN_NAME)
    private int orderNumber;

    public Set() {}

    public Set(int reps, String weightTypeCode, String exerciseTypeCode, int orderNumber,
               Integer weight) {
        this.reps = reps;
        this.weightTypeCode = weightTypeCode;
        this.exerciseTypeCode = exerciseTypeCode;
        this.orderNumber = orderNumber;
        this.weight = weight;
    }

    public Set(int reps, String exerciseTypeCode, int orderNumber, Integer hours,
               Integer minutes, Integer seconds) {
        this.reps = reps;
        this.exerciseTypeCode = exerciseTypeCode;
        this.orderNumber = orderNumber;
        this.minutes = minutes;
        this.seconds = seconds;
        this.hours = hours;
    }

    public Set(int reps, String weightTypeCode, String exerciseTypeCode, Integer weight) {
        this.reps = reps;
        this.weightTypeCode = weightTypeCode;
        this.exerciseTypeCode = exerciseTypeCode;
        this.weight = weight;
    }

    public Set(int reps, String exerciseTypeCode, Integer hours,
               Integer minutes, Integer seconds) {
        this.reps = reps;
        this.exerciseTypeCode = exerciseTypeCode;
        this.minutes = minutes;
        this.seconds = seconds;
        this.hours = hours;
    }

    public Set(boolean isComplete, int reps, Integer weight, Integer hours, Integer minutes, Integer seconds, String weightTypeCode, String exerciseTypeCode, int orderNumber) {
        this.isComplete = isComplete;
        this.reps = reps;
        this.weight = weight;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.weightTypeCode = weightTypeCode;
        this.exerciseTypeCode = exerciseTypeCode;
        this.orderNumber = orderNumber;
    }

    @Override
    public String toString() {
        if (exerciseTypeCode == Constants.WEIGHTS) {
            return reps + " rep(s) of " + weight + Constants.SPACE + weightTypeCode.toLowerCase();
        } else {
            return reps + " rep(s) timed at " + forceTwoDigitTime(hours) + Constants.COLON
                    + forceTwoDigitTime(minutes) + Constants.COLON + forceTwoDigitTime(seconds);
        }
    }

    private String forceTwoDigitTime(int timeVal) {
        String timeString = String.valueOf(timeVal);
        if (timeString.length() == 1) {
            return "0" + timeString;
        } else {
            return timeString;
        }
    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public String getWeightTypeCode() {
        return weightTypeCode;
    }

    public void setWeightTypeCode(String weightTypeCode) {
        this.weightTypeCode = weightTypeCode;
    }

    public String getExerciseTypeCode() {
        return exerciseTypeCode;
    }

    public void setExerciseTypeCode(String exerciseTypeCode) {
        this.exerciseTypeCode = exerciseTypeCode;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public int getSetId() {
        return setId;
    }

    public void setSetId(int setId) {
        this.setId = setId;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean isComplete) {
        this.isComplete = isComplete;
    }

    public Integer getHours() {
        return hours;
    }

    public void setHours(Integer hours) {
        this.hours = hours;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    public Integer getSeconds() {
        return seconds;
    }

    public void setSeconds(Integer seconds) {
        this.seconds = seconds;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Override
    public int compareTo(Set setObj) {
        return this.orderNumber - setObj.orderNumber;
    }

    protected Set(Parcel in) {
        setId = in.readInt();
        isComplete = in.readByte() != 0x00;
        exercise = (Exercise) in.readValue(Exercise.class.getClassLoader());
        reps = in.readInt();
        weight = in.readByte() == 0x00 ? null : in.readInt();
        hours = in.readByte() == 0x00 ? null : in.readInt();
        minutes = in.readByte() == 0x00 ? null : in.readInt();
        seconds = in.readByte() == 0x00 ? null : in.readInt();
        weightTypeCode = in.readString();
        exerciseTypeCode = in.readString();
        orderNumber = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(setId);
        dest.writeByte((byte) (isComplete ? 0x01 : 0x00));
        dest.writeValue(exercise);
        dest.writeInt(reps);
        if (weight == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(weight);
        }
        if (hours == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(hours);
        }
        if (minutes == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(minutes);
        }
        if (seconds == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(seconds);
        }
        dest.writeString(weightTypeCode);
        dest.writeString(exerciseTypeCode);
        dest.writeInt(orderNumber);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Set> CREATOR = new Parcelable.Creator<Set>() {
        @Override
        public Set createFromParcel(Parcel in) {
            return new Set(in);
        }

        @Override
        public Set[] newArray(int size) {
            return new Set[size];
        }
    };
}