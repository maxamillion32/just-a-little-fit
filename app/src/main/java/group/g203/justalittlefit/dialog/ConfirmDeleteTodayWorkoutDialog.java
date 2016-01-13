package group.g203.justalittlefit.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import group.g203.justalittlefit.R;
import group.g203.justalittlefit.listener.ConfirmDeleteTodayWorkoutListener;
import group.g203.justalittlefit.util.Constants;

/**
 * Dialog that confirms if the user wants to delete the {@link group.g203.justalittlefit.model.Workout}
 * associated to today's date.
 */
public class ConfirmDeleteTodayWorkoutDialog extends AppBaseDialog {
    private static final String DELETE_TODAY_WORKOUT_TITLE = "Delete This Workout for today?";
    private static final String DELETE_WORKOUT_TITLE = "Delete This Workout?";
    private static final String TITLE = "Title";
    ConfirmDeleteTodayWorkoutListener listener;

    public static ConfirmDeleteTodayWorkoutDialog newDeleteFromViewInstance() {
        ConfirmDeleteTodayWorkoutDialog dialog = new ConfirmDeleteTodayWorkoutDialog();

        Bundle args = new Bundle();
        args.putString(TITLE, DELETE_WORKOUT_TITLE);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        Bundle args = getArguments();
        if (args != null && args.containsKey(TITLE)) {
            setTitle(args.getString(TITLE));
        } else {
            setTitle(DELETE_TODAY_WORKOUT_TITLE);
        }

        setPositiveButton(getString(R.string.deleteTodayWorkout), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onDeleteTodayWorkoutClick(ConfirmDeleteTodayWorkoutDialog.this);
            }
        });
        setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // onClick will close dialog
            }
        });
        return this.confirmDialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (ConfirmDeleteTodayWorkoutListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + Constants.SPACE +
                    getString(R.string.noImplementsConfirmDeleteTodayWorkoutListener));
        }
    }
}
