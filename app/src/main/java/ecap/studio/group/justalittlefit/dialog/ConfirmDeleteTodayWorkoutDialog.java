package ecap.studio.group.justalittlefit.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.listener.ConfirmDeleteTodayWorkoutListener;
import ecap.studio.group.justalittlefit.util.Constants;

public class ConfirmDeleteTodayWorkoutDialog extends AppBaseDialog {
    private static final String DELETE_TODAY_WORKOUT_TITLE = "Delete This Workout for today?";
    ConfirmDeleteTodayWorkoutListener listener;

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        setTitle(DELETE_TODAY_WORKOUT_TITLE);
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
