package group.g203.justalittlefit.dialog;

import android.app.Activity;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TableRow;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import group.g203.justalittlefit.R;
import group.g203.justalittlefit.bus.AssignDialogBus;
import group.g203.justalittlefit.database.DbAsyncTask;
import group.g203.justalittlefit.database.DbConstants;
import group.g203.justalittlefit.database.DbFunctionObject;
import group.g203.justalittlefit.database.DbTaskResult;
import group.g203.justalittlefit.listener.AssignWorkoutDialogListener;
import group.g203.justalittlefit.model.Workout;
import group.g203.justalittlefit.util.Constants;
import group.g203.justalittlefit.util.Utils;

/**
 * Dialog to display when assigning a {@link group.g203.justalittlefit.model.Workout}.
 */
public class AssignWorkoutDialog extends DialogFragment implements CompoundButton.OnCheckedChangeListener {
    private AssignWorkoutDialogListener listener;
    private List<String> selectedWorkoutNames;
    @InjectView(R.id.workoutContainer)
    LinearLayout workoutContainer;
    boolean busRegistered;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(
                new ContextThemeWrapper(getActivity(), R.style.AppCompatAlertDialogStyle));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.assign_workout_dialog_view, null);
        ButterKnife.inject(this, view);
        selectedWorkoutNames = new ArrayList<>();
        registerBus();
        DbFunctionObject getAllWorkoutDfo = new DbFunctionObject(null, DbConstants.GET_ALL_UNASSIGNED_WORKOUTS);
        new DbAsyncTask(Constants.ASSIGN_DIALOG).execute(getAllWorkoutDfo);
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

    @Subscribe
    public void onAsyncTaskResult(DbTaskResult event) {
        if (event == null || event.getResult() == null) {
            Snackbar.make(getActivity().findViewById(R.id.fab),
                    getString(R.string.workout_list_error), Snackbar.LENGTH_LONG)
                    .show();
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
                createWorkoutCheckBoxes(workouts);
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
            AssignDialogBus.getInstance().register(this);
            busRegistered = true;
        }
    }

    private void unregisterBus() {
        if (busRegistered) {
            AssignDialogBus.getInstance().unregister(this);
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
            checkBox.setText(workout.getName());
            checkBox.setOnCheckedChangeListener(this);
            row.addView(checkBox);
            workoutContainer.addView(row);
        }
    }

    public List<String> getSelectedWorkoutNames() {
        return selectedWorkoutNames;
    }
}
