package group.g203.justalittlefit.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;

import java.util.ArrayList;

import group.g203.justalittlefit.R;
import group.g203.justalittlefit.model.Exercise;
import group.g203.justalittlefit.model.Workout;
import group.g203.justalittlefit.util.Constants;

public class AddExerciseOrSetDialog extends DialogFragment {

    Workout workout;
    ArrayList<Exercise> deletedExercises;

    public static AddExerciseOrSetDialog newInstance(Workout workout,
                                                     ArrayList<Exercise> exercises) {
        AddExerciseOrSetDialog dialog = new AddExerciseOrSetDialog();

        Bundle args = new Bundle();
        args.putParcelable(Constants.WORKOUT, workout);
        args.putParcelableArrayList(Constants.EXERCISES, exercises);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(getActivity(), R.style.AppCompatAlertDialogStyle));

        Bundle args = getArguments();
        if (args != null && args.containsKey(Constants.WORKOUT)) {
            workout = args.getParcelable(Constants.WORKOUT);
            deletedExercises = args.getParcelableArrayList(Constants.EXERCISES);
        } else {
            workout = null;
            deletedExercises = null;
        }

        builder.setTitle(getString(R.string.addExerciseOrSetDialog_Title));
        builder.setPositiveButton(getString(R.string.addExerciseOrSetDialog_addSet), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               displayDialog(true);
            }
        });
        builder.setNeutralButton(getString( R.string.addExerciseOrSetDialog_addExercise), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                displayDialog(false);
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // onClick will close dialog
            }
        });
        final AlertDialog alertDialog = builder.create();
        return alertDialog;
    }

    private void displayDialog(boolean displaySetDialog) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        if (displaySetDialog) {
            SelectExerciseDialog dialog = SelectExerciseDialog.newInstance(workout, deletedExercises);
            dialog.show(fm, getString(R.string.selectExerciseDialogTag));
        } else {
            AddExerciseDialog dialog = new AddExerciseDialog();
            dialog.show(fm, getString(R.string.addExerciseDialogTag));
        }
    }
}
