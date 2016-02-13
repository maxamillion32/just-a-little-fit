package group.g203.justalittlefit.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TableRow;

import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import group.g203.justalittlefit.R;
import group.g203.justalittlefit.database.DbAsyncTask;
import group.g203.justalittlefit.database.DbConstants;
import group.g203.justalittlefit.database.DbFunctionObject;
import group.g203.justalittlefit.database.DbTaskResult;
import group.g203.justalittlefit.listener.AssignWorkoutDialogListener;
import group.g203.justalittlefit.model.Workout;
import group.g203.justalittlefit.util.BusFactory;
import group.g203.justalittlefit.util.Constants;
import group.g203.justalittlefit.util.Utils;

/**
 * Dialog to display when assigning a {@link group.g203.justalittlefit.model.Workout}.
 */
public class AssignWorkoutDialog extends AppBaseDialog implements CompoundButton.OnCheckedChangeListener {
    private AssignWorkoutDialogListener listener;
    private List<String> selectedWorkoutNames;
    @Bind(R.id.workoutContainer)
    LinearLayout workoutContainer;
    ArrayList<DateTime> dateTimes;
    HashSet<Workout> workoutCollection;
    HashMap<String, String> workoutNameMap;
    boolean busRegistered;

    public static AssignWorkoutDialog newInstance(ArrayList<DateTime> dateTimes) {
        AssignWorkoutDialog dialog = new AssignWorkoutDialog();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.DATE_TIMES, dateTimes);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.assign_workout_dialog_view, null);
        ButterKnife.bind(this, view);
        selectedWorkoutNames = new ArrayList<>();
        workoutNameMap = new HashMap<>();
        registerBus();

        Bundle extras = getArguments();

        if (Utils.isInBundleAndValid(extras, Constants.DATE_TIMES)) {
            dateTimes = (ArrayList<DateTime>) extras.getSerializable(Constants.DATE_TIMES);
            DbFunctionObject getWorkoutsByDatesDfo = new DbFunctionObject(dateTimes, DbConstants.GET_WORKOUTS_BY_DATE);
            new DbAsyncTask(Constants.ASSIGN_DIALOG).execute(getWorkoutsByDatesDfo);
        } else {
            callUnassignedWorkouts();
        }
        builder.setTitle(getString(R.string.assignWorkoutDialogHeaderMsg));
        builder.setView(view);
        builder.setPositiveButton(getString(R.string.assignWorkoutDialog_assign), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Will be overridden
            }
        });

        builder.setNegativeButton(getString(R.string.assignWorkoutDialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // onClick will close dialog
            }
        });

        final AlertDialog assignWorkoutDialog = builder.create();
        assignWorkoutDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = assignWorkoutDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (selectedWorkoutNames.isEmpty()) {
                            Utils.displayLongToast(getActivity(), getString(R.string.enforceWorkoutsForAssignment));
                        } else {
                            listener.onAssignWorkoutClick(AssignWorkoutDialog.this);
                            assignWorkoutDialog.dismiss();
                        }
                    }
                });
            }
        });
        return assignWorkoutDialog;
    }

    void callUnassignedWorkouts() {
        DbFunctionObject getAllWorkoutDfo = new DbFunctionObject(null, DbConstants.GET_ALL_UNASSIGNED_WORKOUTS);
        new DbAsyncTask(Constants.ASSIGN_DIALOG).execute(getAllWorkoutDfo);
    }

    @Subscribe
    public void onAsyncTaskResult(DbTaskResult event) {
        if (event == null || event.getResult() == null) {
            Snackbar.make(getActivity().findViewById(R.id.fab),
                    getString(R.string.workout_list_error), Snackbar.LENGTH_LONG)
                    .show();
        } else if (event.getResult() instanceof HashSet) {
            workoutCollection = (HashSet<Workout>) event.getResult();
            callUnassignedWorkouts();
        } else {
            List<Workout> workouts = (List<Workout>) event.getResult();
            if (workouts == null) {
                Snackbar.make(getActivity().findViewById(R.id.fab),
                        getString(R.string.workout_list_error), Snackbar.LENGTH_LONG)
                        .show();
            } else if (workouts.isEmpty()) {
                displayJumpToCreateEditDialog();
                dismiss();
            } else {
                populateMap(workoutCollection, workouts);
                createWorkoutCheckBoxes(workouts);
            }
        }
    }

    void populateMap(HashSet<Workout> set, List<Workout> list) {

        if (!Utils.collectionIsNullOrEmpty(list)) {
            for (Workout workout : list) {
                String keyValue = Utils.ensureValidString(workout.getName());
                workoutNameMap.put(keyValue, keyValue);
            }
        }

        if (!Utils.collectionIsNullOrEmpty(set)) {
            for (Workout workout : set) {
                if (workout.getWorkoutDate() != null) {
                    String key = Utils.ensureValidString(workout.getName());
                    String dateString;
                    if (workoutNameMap.containsKey(key)) {
                        dateString = Utils.ensureValidString(workoutNameMap.get(key));
                        dateString += Constants.SPACE + Constants.DASH + Constants.SPACE +
                                Utils.returnStandardDateString(workout.getWorkoutDate());
                        workoutNameMap.put(key, dateString);
                    }
                }
            }
        }
    }

    private void displayJumpToCreateEditDialog() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        JumpToCreateEditDialog dialog = new JumpToCreateEditDialog();
        dialog.show(fm, getString(R.string.jumpToCreateEditDialogTag));
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
            BusFactory.getAssignDialogBus().register(this);
            busRegistered = true;
        }
    }

    private void unregisterBus() {
        if (busRegistered) {
            BusFactory.getAssignDialogBus().unregister(this);
            busRegistered = false;
        }
    }

    @Override
    public void onDestroy() {
        unregisterBus();
        super.onDestroy();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (AssignWorkoutDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + Constants.SPACE +
                    getString(R.string.noImplementsAssignWorkoutDialogListener));
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        String workoutName = buttonView.getText().toString();
        if (isChecked) {
            selectedWorkoutNames.add(workoutName);
        } else {
            selectedWorkoutNames.remove(workoutName);
        }
    }

    private void createWorkoutCheckBoxes(List<Workout> workouts) {
        int count = 0;
        for (Workout workout : workouts)
        {
            TableRow row = new TableRow(getActivity());
            row.setId(count);
            count++;
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            CheckBox checkBox = new CheckBox(getActivity());
            checkBox.setId(workout.getWorkoutId());
            checkBox.setText(Utils.ensureValidString(workoutNameMap.get(workout.getName())));
            checkBox.setOnCheckedChangeListener(this);
            row.addView(checkBox);
            workoutContainer.addView(row);
        }
    }

    public List<String> getSelectedWorkoutNames() {
        return selectedWorkoutNames;
    }
}
