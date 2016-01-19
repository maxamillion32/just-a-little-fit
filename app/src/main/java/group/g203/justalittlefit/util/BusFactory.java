package group.g203.justalittlefit.util;

import com.squareup.otto.Bus;

/**
 * A class that creates Otto event buses for the application
 */
public class BusFactory {

    private static final Bus ASSIGN_BUS = new Bus();
    private static final Bus ASSIGN_DIALOG_BUS = new Bus();
    private static final Bus CREATE_EDIT_EXERCISE_BUS = new Bus();
    private static final Bus CREATE_EDIT_SET_BUS = new Bus();
    private static final Bus CREATE_EDIT_WORKOUT_BUS = new Bus();
    private static final Bus PEEK_LAUNCHER_BUS = new Bus();
    private static final Bus SELECT_DIALOG_BUS = new Bus();
    private static final Bus VIEW_BUS = new Bus();

    public static Bus getAssignBus() {
        return ASSIGN_BUS;
    }

    public static Bus getAssignDialogBus() {
        return ASSIGN_DIALOG_BUS;
    }

    public static Bus getCreateEditExerciseBus() {
        return CREATE_EDIT_EXERCISE_BUS;
    }

    public static Bus getCreateEditSetBus() {
        return CREATE_EDIT_SET_BUS;
    }

    public static Bus getCreateEditWorkoutBus() {
        return CREATE_EDIT_WORKOUT_BUS;
    }

    public static Bus getPeekLauncherBus() {
        return PEEK_LAUNCHER_BUS;
    }

    public static Bus getSelectDialogBus() {
        return SELECT_DIALOG_BUS;
    }

    public static Bus getViewBus() {
        return VIEW_BUS;
    }
}
