package group.g203.justalittlefit.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import group.g203.justalittlefit.database.DbConstants;

/**
 * Class that represents a single exercise within a {@link Workout)
 */
public class Exercise implements Comparable<Exercise>, Parcelable {

    /** The {@link Workout) that this Exercise is a part of */
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = DbConstants.WORKOUT_ID_COLUMN_NAME)
    private Workout workout;

    /** The display name of this Exercise */
    @DatabaseField(index = true, columnName = DbConstants.NAME_COLUMN_NAME, canBeNull = false)
    private String name;

    /** The id of the Exercise object */
    @DatabaseField(generatedId = true, columnName = DbConstants.EXERCISE_ID_COLUMN_NAME)
    private int exerciseId;

    /** The int that determines the display order of the Exercise object within the app */
    @DatabaseField(canBeNull = false, columnName = DbConstants.ORDER_NUMBER_COLUMN_NAME)
    private int orderNumber;

    /** Whether or not this Exercise is shown as completed on UI */
    @DatabaseField(canBeNull = false, columnName = DbConstants.IS_COMPLETE_COLUMN_NAME)
    private boolean isComplete;

    /** The collection of {@link Set} that are associated to this Exercise in the database */
    @ForeignCollectionField(columnName = DbConstants.SETS, orderColumnName = DbConstants.ORDER_NUMBER_COLUMN_NAME)
    ForeignCollection<Set> sets;

    public Exercise() {}

    public Exercise(String name, int orderNumber) {
        this.name = name;
        this.orderNumber = orderNumber;
    }

    public Exercise(Workout workout, String name, int orderNumber) {
        this.workout = workout;
        this.name = name;
        this.orderNumber = orderNumber;
    }

    public Workout getWorkout() {
        return workout;
    }

    public void setWorkout(Workout workout) {
        this.workout = workout;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public ForeignCollection<Set> getSets() {
        return sets;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean isComplete) {
        this.isComplete = isComplete;
    }

    public void setSets(ForeignCollection<Set> sets) {
        this.sets = sets;
    }

    @Override
    public int compareTo(Exercise exercise) {
        return this.orderNumber - exercise.orderNumber;
    }

    protected Exercise(Parcel in) {
        workout = (Workout) in.readValue(Workout.class.getClassLoader());
        name = in.readString();
        exerciseId = in.readInt();
        orderNumber = in.readInt();
        isComplete = in.readByte() != 0x00;
        sets = (ForeignCollection) in.readValue(ForeignCollection.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(workout);
        dest.writeString(name);
        dest.writeInt(exerciseId);
        dest.writeInt(orderNumber);
        dest.writeByte((byte) (isComplete ? 0x01 : 0x00));
        dest.writeValue(sets);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Exercise> CREATOR = new Parcelable.Creator<Exercise>() {
        @Override
        public Exercise createFromParcel(Parcel in) {
            return new Exercise(in);
        }

        @Override
        public Exercise[] newArray(int size) {
            return new Exercise[size];
        }
    };

}