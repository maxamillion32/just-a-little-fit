package group.g203.justalittlefit.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TableRow;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import group.g203.justalittlefit.R;
import group.g203.justalittlefit.listener.DeleteWorkoutsFromViewDialogListener;
import group.g203.justalittlefit.model.Workout;
import group.g203.justalittlefit.util.Constants;
import group.g203.justalittlefit.util.Utils;

/**
 * Created by s536564 on 12/8/2015.
 */
public class DeleteWorkoutsFromViewDialog extends AppBaseDialog implements CompoundButton.OnCheckedChangeListener {
    List<Workout> selectedWorkouts;
    List<Workout> workouts;
    DeleteWorkoutsFromViewDialogListener listener;
    @InjectView(R.id.workoutContainer)
    LinearLayout workoutContainer;

    public static DeleteWorkoutsFromViewDialog getInstance(ArrayList<Workout> workouts) {
        DeleteWorkoutsFromViewDialog dialog = new DeleteWorkoutsFromViewDialog();

        Bundle args = new Bundle();
        args.putParcelableArrayList(Constants.WORKOUT_LIST, workouts);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        Bundle args = getArguments();
        selectedWorkouts = new ArrayList<>();
        if (args != null && args.containsKey(Constants.WORKOUT_LIST)) {
            workouts = args.getParcelableArrayList(Constants.WORKOUT_LIST);
        } else {
            workouts = null;
        }

        AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(
                new ContextThemeWrapper(getActivity(), R.style.AppCompatAlertDialogStyle));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.assign_workout_dialog_view, null);
        ButterKnife.inject(this, view);
        builder.setTitle(getString(R.string.deleteWorkoutsFromViewDialogTitle));
        builder.setView(view);
        builder.setPositiveButton(getString(R.string.delete_item), new DialogInterface.OnClickListener() {
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
        createWorkoutCheckBoxes(workouts);
        final AlertDialog deleteWorkoutsDialog = builder.create();
        deleteWorkoutsDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = deleteWorkoutsDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (Utils.collectionIsNullOrEmpty(selectedWorkouts)) {
                            Utils.displayLongToast(getActivity(), getString(R.string.enforceDeleteSelection));
                        } else {
                            listener.onDeleteWorkoutsClick(DeleteWorkoutsFromViewDialog.this);
                            deleteWorkoutsDialog.dismiss();
                        }
                    }
                });
            }
        });
        return deleteWorkoutsDialog;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            selectedWorkouts.add((Workout) buttonView.getTag());
        } else {
            selectedWorkouts.remove((Workout) buttonView.getTag());
        }
    }

    private void createWorkoutCheckBoxes(List<Workout> workouts) {
        int count = 0;
        for (Workout workout : workouts) {
            TableRow row = new TableRow(getActivity());
            row.setId(count);
            count++;
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            CheckBox checkBox = new CheckBox(getActivity());
            checkBox.setId(workout.getWorkoutId());
            checkBox.setText(workout.getName());
            checkBox.setTag(workout);
            checkBox.setOnCheckedChangeListener(this);
            row.addView(checkBox);
            workoutContainer.addView(row);
        }
    }

    public List<Workout> getSelectedWorkouts() {
        return selectedWorkouts;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (DeleteWorkoutsFromViewDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + Constants.SPACE +
                    getString(R.string.noImplementsDeleteWorkoutsFromViewDialogListener));
        }
    }
}
