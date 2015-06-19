package ecap.studio.group.justalittlefit.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import ecap.studio.group.justalittlefit.database.DbConstants;

/**
 * Class that represents a single SuperSet within an {@link Exercise)
 */
public class SuperSet implements Comparable<SuperSet> {

    /** The id of the SuperSet object */
    @DatabaseField(generatedId = true, columnName = DbConstants.SUPERSET_ID_COLUMN_NAME)
    private int superSetId;

    /** The int that determines the display order of the SuperSet object within the app */
    @DatabaseField(canBeNull = false, columnName = DbConstants.ORDER_NUMBER_COLUMN_NAME)
    private int orderNumber;

    /** Whether or not this SuperSet is shown as completed on UI */
    @DatabaseField(canBeNull = false, columnName = DbConstants.IS_COMPLETE_COLUMN_NAME)
    private boolean isComplete;

    @ForeignCollectionField(columnName = DbConstants.SETS)
    private ForeignCollection<Set> sets;

    /** The {@link Exercise) that this SuperSet is a part of */
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = DbConstants.EXERCISE_ID_COLUMN_NAME)
    private Exercise exercise;

    /** Whether or not this SuperSet is shown as selected on UI */
    @DatabaseField(canBeNull = false, columnName = DbConstants.IS_SELECTED_COLUMN_NAME)
    private boolean isSelected;

    public SuperSet() {}

    public SuperSet(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public int getSuperSetId() {
        return superSetId;
    }

    public void setSuperSetId(int superSetId) {
        this.superSetId = superSetId;
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

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public void setSets(ForeignCollection<Set> sets) {
        this.sets = sets;
    }

    @Override
    public int compareTo(SuperSet superSet) {
        return this.orderNumber - superSet.orderNumber;
    }
}
