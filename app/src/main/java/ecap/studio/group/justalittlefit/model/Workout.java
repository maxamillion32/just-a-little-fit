package ecap.studio.group.justalittlefit.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import org.joda.time.DateTime;

import ecap.studio.group.justalittlefit.database.DbConstants;

/**
 * Class that represents a workout within the application.
 */
public class Workout implements Comparable<Workout>, Parcelable {

    /** The display name of the Workout object */
    @DatabaseField(index = true, columnName = DbConstants.NAME_COLUMN_NAME, canBeNull = false)
    private String name;

    /** The id of the Workout object */
    @DatabaseField(generatedId = true, columnName = DbConstants.WORKOUT_ID_COLUMN_NAME)
    private int workoutId;

    /** The int that determines the display order of the Workout object within the app */
    @DatabaseField(canBeNull = false, columnName = DbConstants.ORDER_NUMBER_COLUMN_NAME)
    private int orderNumber;

    /** The date the Workout will or has occurred on */
    @DatabaseField(dataType = DataType.DATE_TIME, columnName = DbConstants.WORKOUT_DATE_COLUMN_NAME)
    private DateTime workoutDate;

    /** The collection of {@link Exercise} that are associated to this Workout in the database */
    @ForeignCollectionField(columnName = DbConstants.EXERCISES, orderColumnName = DbConstants.ORDER_NUMBER_COLUMN_NAME)
    private ForeignCollection<Exercise> exercises;

    public Workout() {}

    public Workout(String name) {
        this.name = name;
    }

    public Workout(String name, DateTime workoutDate) {
        this.name = name;
        this.workoutDate = workoutDate;
    }

    public Workout(String name, int orderNumber, DateTime workoutDate) {
        this.name = name;
        this.orderNumber = orderNumber;
        this.workoutDate = workoutDate;
    }

    public Workout(String name, int orderNumber) {
        this.name = name;
        this.orderNumber = orderNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(int workoutId) {
        this.workoutId = workoutId;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public DateTime getWorkoutDate() {
        return workoutDate;
    }

    public void setWorkoutDate(DateTime workoutDate) {
        this.workoutDate = workoutDate;
    }

    public ForeignCollection<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(ForeignCollection<Exercise> exercises) {
        this.exercises = exercises;
    }

    @Override
    public int compareTo(Workout workout) {
        return this.orderNumber - workout.orderNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Workout workout = (Workout) o;

        if (workoutId != workout.workoutId) return false;
        if (!name.equals(workout.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + workoutId;
        return result;
    }

    protected Workout(Parcel in) {
        name = in.readString();
        workoutId = in.readInt();
        orderNumber = in.readInt();
        workoutDate = (DateTime) in.readValue(DateTime.class.getClassLoader());
        exercises = (ForeignCollection) in.readValue(ForeignCollection.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(workoutId);
        dest.writeInt(orderNumber);
        dest.writeValue(workoutDate);
        dest.writeValue(exercises);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Workout> CREATOR = new Parcelable.Creator<Workout>() {
        @Override
        public Workout createFromParcel(Parcel in) {
            return new Workout(in);
        }

        @Override
        public Workout[] newArray(int size) {
            return new Workout[size];
        }
    };
}