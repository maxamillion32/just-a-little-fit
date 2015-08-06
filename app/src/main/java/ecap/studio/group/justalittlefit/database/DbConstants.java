package ecap.studio.group.justalittlefit.database;

/**
 * Constants class for database logic.
 */
public class DbConstants {

    /** An int representation for retrieving all
     * {@link ecap.studio.group.justalittlefit.model.Workout} objects from the db
     * that have no date assigned to them*/
    public static final int GET_ALL_UNASSIGNED_WORKOUTS = 1;

    /** An int representation for retrieving a
     * {@link ecap.studio.group.justalittlefit.model.Workout} object by id from the db */
    public static final int GET_SPECIFIC_WORKOUT = 2;

    /** An int representation for deleting {@link ecap.studio.group.justalittlefit.model.Workout} objects from the db */
    public static final int DELETE_WORKOUTS = 3;

    /** An int representation for for inserting an {@link ecap.studio.group.justalittlefit.model.Workout} object to the db */
    public static final int INSERT_WORKOUT = 4;

    /** An int representation for for updating a list of {@link ecap.studio.group.justalittlefit.model.Workout} objects in the db */
    public static final int UPDATE_WORKOUTS = 5;

    /** An int representation for for assigning a list of {@link ecap.studio.group.justalittlefit.model.Workout} objects to dates in the db */
    public static final int ASSIGN_WORKOUTS = 6;

    /** An int representation for deleting a {@link ecap.studio.group.justalittlefit.model.Workout} object from the db */
    public static final int DELETE_WORKOUT = 7;

    /** The column name for the id field of {@link ecap.studio.group.justalittlefit.model.Workout} */
    public static final String WORKOUT_ID_COLUMN_NAME = "workout_id";

    /** The column name for the id field of {@link ecap.studio.group.justalittlefit.model.Exercise} */
    public static final String EXERCISE_ID_COLUMN_NAME = "exercise_id";

    /** The column name for the id field of {@link ecap.studio.group.justalittlefit.model.Set} */
    public static final String SET_ID_COLUMN_NAME = "set_id";

    /** The column name for the id field of {@link ecap.studio.group.justalittlefit.model.SuperSet} */
    public static final String SUPERSET_ID_COLUMN_NAME = "superset_id";

    /** The column name for the orderNumber field of a table */
    public static final String ORDER_NUMBER_COLUMN_NAME = "order_num";

    /** The column name for the date field of {@link ecap.studio.group.justalittlefit.model.Workout} */
    public static final String WORKOUT_DATE_COLUMN_NAME = "workout_dt";

    /** The column name for the name field of a table */
    public static final String NAME_COLUMN_NAME = "name";

    /** The column name for the isComplete field of a table */
    public static final String IS_COMPLETE_COLUMN_NAME = "isComplete";

    /** The column name for the isSelected field of a table */
    public static final String IS_SELECTED_COLUMN_NAME = "isSelected";

    /** The column name for the {@link ecap.studio.group.justalittlefit.model.SuperSet} field of
     * {@link ecap.studio.group.justalittlefit.model.Exercise} */
    public static final String SUPERSET_SWITCH_COLUMN_NAME = "superset_sw";

    /** The column name for the reps field of {@link ecap.studio.group.justalittlefit.model.Set} */
    public static final String REPS_COLUMN_NAME = "rep_count";

    /** The column name for the reps field of {@link ecap.studio.group.justalittlefit.model.Set} */
    public static final String SET_VALUE_COLUMN_NAME = "set_val";

    /** The column name for the weightTypeCode field of {@link ecap.studio.group.justalittlefit.model.Set} */
    public static final String WEIGHT_CODE_COLUMN_NAME = "wt_code";

    /** The column name for the exerciseTypeCode field of {@link ecap.studio.group.justalittlefit.model.Set} */
    public static final String TYPE_CODE_COLUMN_NAME = "type_code";

    /** The column name for the {@link ecap.studio.group.justalittlefit.model.Workout} foreign collection of
     *  {@link ecap.studio.group.justalittlefit.model.Exercise} */
    public static final String EXERCISES = "exercises";

    /** The column name for the {@link ecap.studio.group.justalittlefit.model.Exercise}
     * and/or {@link ecap.studio.group.justalittlefit.model.SuperSet} foreign collection of
     *  {@link ecap.studio.group.justalittlefit.model.Set} */
    public static final String SETS = "sets";

    /** The column name for the {@link ecap.studio.group.justalittlefit.model.Exercise} foreign collection of
     *  {@link ecap.studio.group.justalittlefit.model.SuperSet} */
    public static final String SUPERSETS = "superSets";

    public static final String SQL_ERR_PREFIX = "An SQLException occurred: ";
}

