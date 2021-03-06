package group.g203.justalittlefit.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import group.g203.justalittlefit.R;
import group.g203.justalittlefit.listener.AddWorkoutDialogListener;
import group.g203.justalittlefit.util.Constants;
import group.g203.justalittlefit.util.Utils;

/**
 * Dialog to display when adding a {@link group.g203.justalittlefit.model.Workout}.
 */
public class AddWorkoutDialog extends AppBaseDialog {
    private AddWorkoutDialogListener listener;
    @Bind(R.id.etField)
    EditText addWorkoutText;

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_workout_exercise_dialog_view, null);
        ButterKnife.bind(this, view);
        builder.setTitle(getString(R.string.addWorkoutDialog_Title));
        builder.setView(view);
        builder.setPositiveButton(getString(R.string.addWorkoutDialog_add), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Will be overridden
            }
        });
        builder.setNegativeButton(getString(R.string.addWorkoutDialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // onClick will close dialog
            }
        });
        final AlertDialog createWorkoutDialog = builder.create();
        createWorkoutDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = createWorkoutDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (Utils.ensureValidString(addWorkoutText.getText().toString()).isEmpty()) {
                            Utils.displayLongToast(
                                    getActivity(),
                                    getString(R.string.addWorkout_requireName));
                        } else {
                            listener.onAddWorkoutClick(AddWorkoutDialog.this);
                            createWorkoutDialog.dismiss();
                        }
                    }
                });
            }
        });
        return createWorkoutDialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (AddWorkoutDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + Constants.SPACE +
                    getString(R.string.noImplementsAddWorkoutDialogListener));
        }
    }

    public EditText getAddWorkoutText() {
        return addWorkoutText;
    }
}