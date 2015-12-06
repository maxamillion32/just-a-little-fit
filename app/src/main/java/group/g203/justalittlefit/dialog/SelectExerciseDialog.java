package group.g203.justalittlefit.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableRow;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import group.g203.justalittlefit.R;
import group.g203.justalittlefit.bus.SelectDialogBus;
import group.g203.justalittlefit.database.DbAsyncTask;
import group.g203.justalittlefit.database.DbConstants;
import group.g203.justalittlefit.database.DbFunctionObject;
import group.g203.justalittlefit.database.DbTaskResult;
import group.g203.justalittlefit.listener.SelectExerciseDialogListener;
import group.g203.justalittlefit.model.Exercise;
import group.g203.justalittlefit.model.Workout;
import group.g203.justalittlefit.util.Constants;

public class SelectExerciseDialog extends DialogFragment {

    private SelectExerciseDialogListener listener;
    private String selectedExercise;
    @InjectView(R.id.rgExercises)
    RadioGroup exerciseRadioGroup;
    boolean busRegistered;
    ArrayList<Exercise> deletedExercises;

    public static SelectExerciseDialog newInstance(Workout workout, ArrayList<Exercise> exercises) {
        SelectExerciseDialog dialog = new SelectExerciseDialog();

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
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.select_exercise_dialog_view, null);
        ButterKnife.inject(this, view);
        registerBus();

        Bundle args = getArguments();
        Workout workout;

        if (args != null) {
            workout = args.getParcelable(Constants.WORKOUT);
            deletedExercises = args.getParcelableArrayList(Constants.EXERCISES);
        } else {
            workout = null;
            deletedExercises = null;
        }

        DbFunctionObject getFullWorkoutDfo = new DbFunctionObject(workout, DbConstants.GET_FULL_WORKOUT);
        new DbAsyncTask(Constants.SELECT_DIALOG).execute(getFullWorkoutDfo);
        builder.setTitle(getString(R.string.selectExerciseDialog_Title));
        builder.setView(view);
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // onClick will close dialog
            }
        });
        final AlertDialog selectExerciseDialog = builder.create();
        return selectExerciseDialog;
    }

    @Subscribe
    public void onAsyncTaskResult(DbTaskResult event) {
        if (event == null || event.getResult() == null) {
            Snackbar.make(getActivity().findViewById(R.id.fab),
                    getString(R.string.workout_list_error), Snackbar.LENGTH_LONG)
                    .show();
        } else {
            Workout workout = (Workout) event.getResult();
            if (workout == null) {
                Snackbar.make(getActivity().findViewById(R.id.fab),
                        getString(R.string.workout_list_error), Snackbar.LENGTH_LONG)
                        .show();
            } else {
                createExerciseRadioGroup(workout);
            }
        }
    }

    private void createExerciseRadioGroup(Workout workout) {
        int count = 0;
        List<Exercise> exercises = new ArrayList<>(workout.getExercises());
        List<String> deletedExerciseNameList = getDeletedExerciseStringList(deletedExercises);
        for (final Exercise exercise : exercises)
        {
            if (!deletedExerciseNameList.contains(exercise.getName().trim())) {
                TableRow row = new TableRow(getActivity());
                row.setId(count);
                count++;
                row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                RadioButton radioButton = new RadioButton(getActivity());
                radioButton.setId(exercise.getExerciseId());
                radioButton.setText(exercise.getName());
                radioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentManager fm = getActivity().getSupportFragmentManager();
                        AddSetDialog dialog = AddSetDialog.newInstance(exercise);
                        dialog.show(fm, getString(R.string.addSetDialogTag));
                        getDialog().dismiss();
                    }
                });
                row.addView(radioButton);
                exerciseRadioGroup.addView(row);
            }
        }
    }

    public List<String> getDeletedExerciseStringList(ArrayList<Exercise> exercises) {
        List<String> deletedExerciseNameList = new ArrayList<>();
        for (Exercise exercise : exercises) {
            deletedExerciseNameList.add(exercise.getName().trim());
        }
        return deletedExerciseNameList;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!busRegistered) {
            registerBus();
        }
    }

    private void registerBus() {
        if (!busRegistered) {
            SelectDialogBus.getInstance().register(this);
            busRegistered = true;
        }
    }

    private void unregisterBus() {
        if (busRegistered) {
            SelectDialogBus.getInstance().unregister(this);
            busRegistered = false;
        }
    }

    @Override
    public void onDestroy() {
        unregisterBus();
        super.onDestroy();
    }
}
