package group.g203.justalittlefit.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
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
import group.g203.justalittlefit.database.DbAsyncTask;
import group.g203.justalittlefit.database.DbConstants;
import group.g203.justalittlefit.database.DbFunctionObject;
import group.g203.justalittlefit.database.DbTaskResult;
import group.g203.justalittlefit.listener.SelectExerciseDialogListener;
import group.g203.justalittlefit.model.Exercise;
import group.g203.justalittlefit.model.Workout;
import group.g203.justalittlefit.util.BusFactory;
import group.g203.justalittlefit.util.Constants;

/**
 * Dialog that allows users to select an {@link group.g203.justalittlefit.model.Exercise} when
 * adding a {@link group.g203.justalittlefit.model.Set} to a workout from the
 * {@link group.g203.justalittlefit.activity.TodayActivity}.
 */
public class SelectExerciseDialog extends AppBaseDialog {

    private SelectExerciseDialogListener listener;
    private String selectedExercise;
    @InjectView(R.id.rgExercises)
    RadioGroup exerciseRadioGroup;
    boolean busRegistered;

    public static SelectExerciseDialog newInstance(Workout workout) {
        SelectExerciseDialog dialog = new SelectExerciseDialog();

        Bundle args = new Bundle();
        args.putParcelable(Constants.WORKOUT, workout);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.select_exercise_dialog_view, null);
        ButterKnife.inject(this, view);
        registerBus();

        Bundle args = getArguments();
        Workout workout;

        if (args != null) {
            workout = args.getParcelable(Constants.WORKOUT);
        } else {
            workout = null;
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
        } else if (event.getResult() instanceof Workout) {
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
        exerciseRadioGroup.removeAllViews();
        for (final Exercise exercise : exercises) {
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

    @Override
    public void onResume() {
        super.onResume();
        if (!busRegistered) {
            registerBus();
        }
    }

    private void registerBus() {
        if (!busRegistered) {
            BusFactory.getSelectDialogBus().register(this);
            busRegistered = true;
        }
    }

    private void unregisterBus() {
        if (busRegistered) {
            BusFactory.getSelectDialogBus().unregister(this);
            busRegistered = false;
        }
    }

    @Override
    public void onDestroy() {
        unregisterBus();
        super.onDestroy();
    }
}
