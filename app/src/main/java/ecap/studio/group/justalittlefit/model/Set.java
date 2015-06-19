package ecap.studio.group.justalittlefit.model;

import com.j256.ormlite.field.DatabaseField;

import ecap.studio.group.justalittlefit.database.DbConstants;

/**
 * Class that represents a single set within an {@link Exercise)
 */
public class Set implements Comparable<Set> {

    /** The display name of the Set
     * (will not be displayed unless Set is part of a {@link SuperSet}) */
    @DatabaseField(index = true, columnName = DbConstants.NAME_COLUMN_NAME, canBeNull = false)
    private String name;

    /** The id of the Set object */
    @DatabaseField(generatedId = true, columnName = DbConstants.SET_ID_COLUMN_NAME)
    private int setId;

    /** Whether or not this Set is shown as completed on UI */
    @DatabaseField(canBeNull = false, columnName = DbConstants.IS_COMPLETE_COLUMN_NAME)
    private boolean isComplete;

    /** The {@link Exercise) that this Set is a part of */
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = DbConstants.EXERCISE_ID_COLUMN_NAME)
    private Exercise exercise;

    /** The {@link SuperSet) that this Set is a part of */
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = DbConstants.SUPERSET_ID_COLUMN_NAME)
    private SuperSet superSet;

    /** The number of reps in the Set */
    @DatabaseField(canBeNull = false, columnName = DbConstants.REPS_COLUMN_NAME)
    private int reps;

    /** The measured work of this Set
     * (either timed value or rep count)
     */
    @DatabaseField(columnName = DbConstants.SET_VALUE_COLUMN_NAME)
    private long value;

    /** Either lbs, kgs, or none */
    @DatabaseField(canBeNull = false, columnName = DbConstants.WEIGHT_CODE_COLUMN_NAME)
    private String weightTypeCode;

    /** Either reps, loggedTime, or timer based set */
    @DatabaseField(canBeNull = false, columnName = DbConstants.TYPE_CODE_COLUMN_NAME)
    private String exerciseTypeCode;

    /** The int that determines the display order of the Set object within the app */
    @DatabaseField(canBeNull = false, columnName = DbConstants.ORDER_NUMBER_COLUMN_NAME)
    private int orderNumber;

    /** Whether or not this Set is shown as selected on UI */
    @DatabaseField(canBeNull = false, columnName = DbConstants.IS_SELECTED_COLUMN_NAME)
    private boolean isSelected;

    public Set() {}

    public Set(String name, int reps, long value, String weightTypeCode, String exerciseTypeCode, int orderNumber) {
        this.name = name;
        this.reps = reps;
        this.value = value;
        this.weightTypeCode = weightTypeCode;
        this.exerciseTypeCode = exerciseTypeCode;
        this.orderNumber = orderNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public SuperSet getSuperSet() {
        return superSet;
    }

    public void setSuperSet(SuperSet superSet) {
        this.superSet = superSet;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
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

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    @Override
    public int compareTo(Set setObj) {
        return this.orderNumber - setObj.orderNumber;
    }
}
