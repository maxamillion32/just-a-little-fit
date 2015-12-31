package group.g203.justalittlefit.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import group.g203.justalittlefit.R;
import group.g203.justalittlefit.listener.RenameDialogListener;
import group.g203.justalittlefit.model.Exercise;
import group.g203.justalittlefit.model.Workout;
import group.g203.justalittlefit.util.Constants;
import group.g203.justalittlefit.util.Utils;

/**
 * Dialog to display when renaming a Workout or Exercise.
 */
public class RenameDialog extends DialogFragment {
    private RenameDialogListener listener;
    @InjectView(R.id.etField)
    EditText etRename;
    Workout workout;
    Exercise exercise;

    public static RenameDialog newInstance(Object object) {
        RenameDialog dialog = new RenameDialog();
        Bundle bundle = new Bundle();

        if (object instanceof Workout) {
            Workout workout = (Workout) object;
            bundle.putParcelable(Constants.WORKOUT, workout);
        } else if (object instanceof Exercise){
            Exercise exercise = (Exercise) object;
            bundle.putParcelable(Constants.EXERCISE, exercise);
        }

        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(getActivity(), R.style.AppCompatAlertDialogStyle));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_workout_exercise_dialog_view, null);
        ButterKnife.inject(this, view);

        etRename.setText(Constants.EMPTY_STRING);
        if (getArguments().containsKey(Constants.WORKOUT)) {
            builder.setTitle(getString(R.string.renameDialog_WorkoutTitle));
            workout = getArguments().getParcelable(Constants.WORKOUT);
            etRename.append(Utils.ensureValidString(workout.getName()));
        } else if (getArguments().containsKey(Constants.EXERCISE)) {
            builder.setTitle(getString(R.string.renameDialog_ExerciseTitle));
            exercise = getArguments().getParcelable(Constants.EXERCISE);
            etRename.append(Utils.ensureValidString(exercise.getName()));
        }


        builder.setView(view);
        builder.setPositiveButton(getString(R.string.renameBtnText), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Will be overridden
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // onClick will close dialog
            }
        });
        final AlertDialog renameDialog = builder.create();
        renameDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = renameDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (Utils.ensureValidString(etRename.getText().toString()).isEmpty()) {
                            if (workout == null && exercise != null) {
                                Utils.displayLongToast(
                                        getActivity(),
                                        getString(R.string.addExercise_requireName));
                            } else if (exercise == null && workout != null) {
                                Utils.displayLongToast(
                                        getActivity(),
                                        getString(R.string.addWorkout_requireName));
                            }
                        } else if ((workout == null && exercise != null &&
                                Utils.ensureValidString(exercise.getName()).equals(
                                        Utils.ensureValidString(etRename.getText().toString()))
                        ) || (workout != null && exercise == null &&
                                Utils.ensureValidString(workout.getName()).equals(
                                        Utils.ensureValidString(etRename.getText().toString()))
                        )) {
                            Utils.displayLongToast(
                                    getActivity(),
                                    getString(R.string.noRenameChanges));
                            renameDialog.dismiss();
                        } else {
                            listener.onRenameClick(RenameDialog.this);
                            renameDialog.dismiss();
                        }
                    }
                });
            }
        });
        return renameDialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (RenameDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + Constants.SPACE +
                    getString(R.string.noImplementsAddWorkoutDialogListener));
        }
    }

    public EditText getRenameText() {
        return etRename;
    }

    public Workout getWorkout() {
        return workout;
    }

    public Exercise getExercise() {
        return exercise;
    }
}