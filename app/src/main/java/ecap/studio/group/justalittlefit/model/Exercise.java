package ecap.studio.group.justalittlefit.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import ecap.studio.group.justalittlefit.database.DbConstants;

/**
 * Class that represents a single exercise within a {@link Workout)
 */
public class Exercise implements Comparable<Exercise> {

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

    /** Whether or not this Exercise displays {@link Set} or {@link SuperSet} */
    @DatabaseField(canBeNull = false, columnName = DbConstants.SUPERSET_SWITCH_COLUMN_NAME)
    private boolean superSetSwitch;

    /** Whether or not this Exercise is shown as completed on UI */
    @DatabaseField(canBeNull = false, columnName = DbConstants.IS_COMPLETE_COLUMN_NAME)
    private boolean isComplete;

    /** Whether or not this Exercise is shown as selected on UI */
    @DatabaseField(canBeNull = false, columnName = DbConstants.IS_SELECTED_COLUMN_NAME)
    private boolean isSelected;

    /** The collection of {@link Set} that are associated to this Exercise in the database */
    @ForeignCollectionField(columnName = DbConstants.SETS)
    ForeignCollection<Set> sets;

    /** The collection of {@link SuperSet} that are associated to this Exercise in the database */
    @ForeignCollectionField(columnName = DbConstants.SUPERSETS)
    ForeignCollection<SuperSet> superSets;

    public Exercise() {}

    public Exercise(String name, boolean superSetSwitch, int orderNumber) {
        this.name = name;
        this.superSetSwitch = superSetSwitch;
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

    public boolean isSuperSetSwitch() {
        return superSetSwitch;
    }

    public void setSuperSetSwitch(boolean superSetSwitch) {
        this.superSetSwitch = superSetSwitch;
    }

    public ForeignCollection<Set> getSets() {
        return sets;
    }

    public ForeignCollection<SuperSet> getSuperSets() {
        return superSets;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
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

    public void setSuperSets(ForeignCollection<SuperSet> superSets) {
        this.superSets = superSets;
    }

    @Override
    public int compareTo(Exercise exercise) {
        return this.orderNumber - exercise.orderNumber;
    }
}

