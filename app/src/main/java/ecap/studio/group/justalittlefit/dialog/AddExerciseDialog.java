package ecap.studio.group.justalittlefit.dialog;

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
import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.listener.AddExerciseDialogListener;
import ecap.studio.group.justalittlefit.util.Constants;
import ecap.studio.group.justalittlefit.util.Utils;

/**
 * Dialog to display when adding an exercise.
 */
public class AddExerciseDialog extends DialogFragment {
    private AddExerciseDialogListener listener;
    @InjectView(R.id.etField)
    EditText addExerciseText;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(getActivity(), R.style.AppCompatAlertDialogStyle));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_workout_exercise_dialog_view, null);
        ButterKnife.inject(this, view);
        builder.setTitle(getString(R.string.addExerciseDialog_Title));
        builder.setView(view);
        builder.setPositiveButton(getString(R.string.addExerciseDialog_add), new DialogInterface.OnClickListener() {
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
        final AlertDialog createWorkoutDialog = builder.create();
        createWorkoutDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = createWorkoutDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (addExerciseText.getText().toString().trim().isEmpty()) {
                            Utils.displayLongToast(
                                    getActivity(),
                                    getString(R.string.addExercise_requireName));
                        } else {
                            listener.onAddExerciseClick(AddExerciseDialog.this);
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
            listener = (AddExerciseDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + Constants.SPACE +
                    getString(R.string.noImplementsAddExerciseDialogListener));
        }
    }

    public EditText getAddExerciseText() {
        return addExerciseText;
    }
}
