package group.g203.justalittlefit.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableRow;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import group.g203.justalittlefit.R;
import group.g203.justalittlefit.activity.ViewActivity;
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
    private static AssignWorkoutDialog instance;
    private AssignWorkoutDialogListener listener;
    private List<String> selectedWorkoutNames;
    @Bind(R.id.workoutContainer)
    LinearLayout workoutContainer;
    @Bind(R.id.rgAssignWorkoutOpts)
    RadioGroup rgOpts;
    ArrayList<DateTime> dateTimes;
    HashSet<Workout> workoutCollection;
    List<Workout> unAssignedWorkouts;
    boolean busRegistered;
    AlertDialog thisDialog;

    public static AssignWorkoutDialog newInstance(ArrayList<DateTime> dateTimes) {
        if (instance == null) {
            instance = new AssignWorkoutDialog();
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.DATE_TIMES, dateTimes);
        instance.setArguments(bundle);
        return instance;
    }

    public static AssignWorkoutDialog getInstance() {
        return instance;
    }

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.assign_workout_dialog_view, null);
        ButterKnife.bind(this, view);
        selectedWorkoutNames = new ArrayList<>();
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
        thisDialog = assignWorkoutDialog;
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
            unAssignedWorkouts  = (List<Workout>) event.getResult();
            if (unAssignedWorkouts == null) {
                Snackbar.make(getActivity().findViewById(R.id.fab),
                        getString(R.string.workout_list_error), Snackbar.LENGTH_LONG)
                        .show();
            } else if (unAssignedWorkouts.isEmpty()) {
                displayJumpToCreateEditDialog();
                dismiss();
            } else {
                displayDialogUi();
            }
        }
    }

    void displayOpts() {
        if (!Utils.collectionIsNullOrEmpty(workoutCollection)) {
            rgOpts.setVisibility(View.VISIBLE);
        }
    }

    @OnClick({R.id.rbExistingWorkouts, R.id.rbWorkoutsToAssign})
    void onRbClick(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.rbWorkoutsToAssign:
                if (checked) {
                    displayUnassignedWorkouts(unAssignedWorkouts);
                    break;
                }
            case R.id.rbExistingWorkouts:
                if (checked) {
                    displayExistingWorkouts(workoutCollection);
                    break;
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

    private void displayDialogUi(){
        displayOpts();
        displayUnassignedWorkouts(unAssignedWorkouts);
    }

    private void displayUnassignedWorkouts(List<Workout> workouts) {
        thisDialog.setTitle(getString(R.string.assignWorkoutDialogHeaderMsg));
        thisDialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
        thisDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);

        workoutContainer.removeAllViews();
        int count = 0;
        for (Workout workout : workouts)
        {
            TableRow row = new TableRow(getActivity());
            row.setId(count);
            count++;
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            CheckBox checkBox = new CheckBox(getActivity());
            checkBox.setId(workout.getWorkoutId());
            checkBox.setText(Utils.ensureValidString(workout.getName()));
            checkBox.setOnCheckedChangeListener(this);
            row.addView(checkBox);
            workoutContainer.addView(row);
        }
    }

    private void displayExistingWorkouts(HashSet<Workout> workouts) {
        thisDialog.setTitle(getString(R.string.assignChooseWorkoutDialogHeaderMsg));
        workoutContainer.removeAllViews();
        thisDialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.GONE);
        thisDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);

        for (final Workout workout : workouts)
        {
            if (workout.getWorkoutDate() != null) {
                TextView tv = new TextView(getActivity());
                tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                tv.setPadding(0, Utils.getDp(30, getActivity()), 0, 0);
                tv.setText(Utils.ensureValidString(workout.getName()) + Constants.SPACE + Constants.DASH +
                        Constants.SPACE + Utils.returnStandardDateString(workout.getWorkoutDate()));
                tv.setTextColor(getResources().getColor(R.color.app_blue_gray));
                tv.setTypeface(null, Typeface.BOLD);
                Utils.underlineText(tv);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Context context = getActivity();
                        Intent intent = new Intent(context, ViewActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(Constants.WORKOUT, workout);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        intent.putExtras(bundle);
                        context.startActivity(intent);
                    }
                });
                tv.setGravity(Gravity.CENTER);
                workoutContainer.addView(tv);
            }
        }
    }

    public List<String> getSelectedWorkoutNames() {
        return selectedWorkoutNames;
    }
}
