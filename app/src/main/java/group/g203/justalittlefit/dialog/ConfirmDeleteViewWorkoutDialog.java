package group.g203.justalittlefit.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import group.g203.justalittlefit.R;
import group.g203.justalittlefit.listener.ConfirmDeleteViewWorkoutListener;
import group.g203.justalittlefit.util.Constants;

/**
 * Dialog that confirms if the user wants to delete the {@link group.g203.justalittlefit.model.Workout}
 * associated to a date.
 */
public class ConfirmDeleteViewWorkoutDialog extends AppBaseDialog {
    private static final String DELETE_WORKOUT_TITLE = "Delete This Workout?";
    ConfirmDeleteViewWorkoutListener listener;

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        Bundle args = getArguments();
        setTitle(DELETE_WORKOUT_TITLE);
        setPositiveButton(getString(R.string.deleteViewWorkout), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onDeleteViewWorkoutClick(ConfirmDeleteViewWorkoutDialog.this);
            }
        });
        setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // onClick will close dialog
            }
        });
        return this.alertDialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (ConfirmDeleteViewWorkoutListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + Constants.SPACE +
                    getString(R.string.noImplementsConfirmDeleteViewWorkoutListener));
        }
    }
}
