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
import android.widget.LinearLayout;
import android.widget.RadioButton;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.listener.AddSetDialogListener;
import ecap.studio.group.justalittlefit.model.Exercise;
import ecap.studio.group.justalittlefit.model.Workout;
import ecap.studio.group.justalittlefit.util.Constants;
import ecap.studio.group.justalittlefit.util.Utils;

/**
 * Dialog to display when adding a Set.
 */
public class AddSetDialog extends DialogFragment {
    private AddSetDialogListener listener;
    @InjectView(R.id.rbWeightType)
    RadioButton rbWeightedSet;
    @InjectView(R.id.rbLoggedTimeType)
    RadioButton rbTimedSet;
    @InjectView(R.id.rbLbs)
    RadioButton rbLbs;
    @InjectView(R.id.rbKgs)
    RadioButton rbKgs;
    @InjectView(R.id.etHours)
    EditText tvHours;
    @InjectView(R.id.etMins)
    EditText tvMins;
    @InjectView(R.id.etSeconds)
    EditText tvSeconds;
    @InjectView(R.id.llWeightedRepsOptions)
    LinearLayout llWeightedSetView;
    @InjectView(R.id.llTimedOptions)
    LinearLayout llTimedSetView;
    @InjectView(R.id.etRepCount)
    EditText etRepCount;
    @InjectView(R.id.etWeightAmount)
    EditText etWeightAmount;
    @InjectView(R.id.etTimedRepCount)
    EditText etTimedRepCount;
    Exercise exercise;

    public static AddSetDialog newInstance(Exercise exercise) {
        AddSetDialog dialog = new AddSetDialog();

        Bundle args = new Bundle();
        args.putParcelable(Constants.EXERCISE, exercise);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(getActivity(), R.style.AppCompatAlertDialogStyle));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_set_dialog_view, null);
        ButterKnife.inject(this, view);

        Bundle args = getArguments();
        if (args != null && args.containsKey(Constants.EXERCISE)) {
            exercise = args.getParcelable(Constants.EXERCISE);
        } else {
            exercise = null;
        }

        builder.setTitle(getString(R.string.addSetDialog_Title));
        builder.setView(view);
        builder.setPositiveButton(getString(R.string.addSetDialog_add), new DialogInterface.OnClickListener() {
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
        final AlertDialog createSetDialog = builder.create();
        createSetDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = createSetDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (getRbWeightedSet().isChecked()) {
                            String errMsg = Constants.EMPTY_STRING;
                            if (getEtRepCount().getText().toString().trim().isEmpty()) {
                                errMsg += "Please enter in a rep count greater than 0";
                            }
                            if (getEtWeightAmount().getText().toString().trim().isEmpty()) {
                                String weightErr = "Please enter in a weight amount greater than 0";
                                if (errMsg.trim().isEmpty()) {
                                    errMsg += weightErr;
                                } else {
                                    errMsg += "\n" + weightErr;
                                }
                            }
                            handleDismissOrErrDisplay(errMsg, createSetDialog);
                        } else if (getRbTimedSet().isChecked()) {
                            String errMsg = Constants.EMPTY_STRING;
                            if (getEtRepCount().getText().toString().trim().isEmpty()) {
                                errMsg += "Please enter in a rep count greater than 0";
                            }
                            if (getEtHours().getText().toString().trim().isEmpty() &&
                                    getEtMins().getText().toString().trim().isEmpty() &&
                                    getEtSeconds().getText().toString().trim().isEmpty()) {
                                String timedErr = "Please enter in at least one value for hours, minutes, or seconds";
                                if (errMsg.trim().isEmpty()) {
                                    errMsg += timedErr;
                                } else {
                                    errMsg += "\n" + timedErr;
                                }
                                handleDismissOrErrDisplay(errMsg, createSetDialog);
                            } else {
                                listener.onAddSetClick(AddSetDialog.this);
                                createSetDialog.dismiss();
                            }
                        } else {
                            listener.onAddSetClick(AddSetDialog.this);
                            createSetDialog.dismiss();
                        }
                    }
                });
            }
        });
        return createSetDialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (AddSetDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + Constants.SPACE +
                    getString(R.string.noImplementsAddSetDialogListener));
        }
    }

    private void handleDismissOrErrDisplay(String errMsg, AlertDialog dialog) {
        if (errMsg.trim() == Constants.EMPTY_STRING) {
            listener.onAddSetClick(AddSetDialog.this);
            dialog.dismiss();
        } else {
            Utils.displayLongToast(
                    getActivity(), errMsg);
        }
    }

    @OnClick({R.id.rbWeightType, R.id.rbLoggedTimeType})
    void onTypeClick(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.rbWeightType:
                if (checked)
                    llTimedSetView.setVisibility(View.GONE);
                    llWeightedSetView.setVisibility(View.VISIBLE);
                    break;
            case R.id.rbLoggedTimeType:
                if (checked)
                    llTimedSetView.setVisibility(View.VISIBLE);
                llWeightedSetView.setVisibility(View.GONE);
                    break;
        }
    }

    public RadioButton getRbLbs() {
        return rbLbs;
    }

    public RadioButton getRbKgs() {
        return rbKgs;
    }

    public EditText getEtHours() {
        return tvHours;
    }

    public EditText getEtMins() {
        return tvMins;
    }

    public EditText getEtSeconds() {
        return tvSeconds;
    }

    public EditText getEtRepCount() {
        return etRepCount;
    }

    public EditText getEtWeightAmount() {
        return etWeightAmount;
    }

    public RadioButton getRbWeightedSet() {
        return rbWeightedSet;
    }

    public RadioButton getRbTimedSet() {
        return rbTimedSet;
    }

    public EditText getEtTimedRepCount() {
        return etTimedRepCount;
    }

    public Exercise getExercise() {
        return exercise;
    }
}
