package group.g203.justalittlefit.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import group.g203.justalittlefit.R;
import group.g203.justalittlefit.listener.ConfirmExercisesDeletionListener;
import group.g203.justalittlefit.util.Constants;

/**
 * Dialog that will confirm deletion of all exercises in view.
 */
public class ConfirmDeleteExercisesDialog extends AppBaseDialog {
    private static final String DELETE_ALL_EXERCISES = "Delete All Exercises?";
    ConfirmExercisesDeletionListener listener;

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        setTitle(DELETE_ALL_EXERCISES);
        setPositiveButton(getString(R.string.delete_all), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onDeleteAllExercisesClick(ConfirmDeleteExercisesDialog.this);
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
            listener = (ConfirmExercisesDeletionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + Constants.SPACE +
                    getString(R.string.noImplementsConfirmExerciseDeletionListener));
        }
    }
}