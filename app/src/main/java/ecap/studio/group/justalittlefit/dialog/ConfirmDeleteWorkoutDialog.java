package ecap.studio.group.justalittlefit.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.listener.ConfirmWorkoutDeletionListener;
import ecap.studio.group.justalittlefit.util.Constants;

/**
 * Dialog that will confirm deletion of a workout(s).
 */
public class ConfirmDeleteWorkoutDialog extends AppBaseDialog {
    private static final String DELETE_ALL_WORKOUTS = "Delete All Workouts?";
    ConfirmWorkoutDeletionListener listener;

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        final boolean deleteAllTitle = getArguments().getBoolean(getString(R.string.confirmDeleteWorkoutDialog_selectAll_bool));
        final String deleteOptString = (deleteAllTitle) ? getString(R.string.delete_all) :
                getString(R.string.delete_item);
        setTitleTextView(DELETE_ALL_WORKOUTS);
        setPositiveButton(deleteOptString, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (deleteAllTitle) {
                    listener.onDeleteAllWorkoutsClick(ConfirmDeleteWorkoutDialog.this);
                } else {
                    listener.onDeleteWorkoutClick(ConfirmDeleteWorkoutDialog.this);
                }
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
            listener = (ConfirmWorkoutDeletionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + Constants.SPACE +
                    getString(R.string.noImplementsConfirmDeletionListener));
        }
    }
}
