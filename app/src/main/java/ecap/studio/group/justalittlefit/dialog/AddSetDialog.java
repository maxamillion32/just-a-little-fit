package ecap.studio.group.justalittlefit.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.listener.AddSetDialogListener;
import ecap.studio.group.justalittlefit.listener.SetEtListener;
import ecap.studio.group.justalittlefit.model.Exercise;
import ecap.studio.group.justalittlefit.model.Set;
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
    EditText etHours;
    @InjectView(R.id.etMins)
    EditText etMins;
    @InjectView(R.id.etSeconds)
    EditText etSeconds;
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
    Set set;

    public static AddSetDialog newInstance(Exercise exercise) {
        AddSetDialog dialog = new AddSetDialog();

        Bundle args = new Bundle();
        args.putParcelable(Constants.EXERCISE, exercise);
        dialog.setArguments(args);

        return dialog;
    }

    public static AddSetDialog newInstance(Set set) {
        AddSetDialog dialog = new AddSetDialog();

        Bundle args = new Bundle();
        args.putParcelable(Constants.SET, set);
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

        if (args != null && args.containsKey(Constants.SET)) {
            set = args.getParcelable(Constants.SET);
        } else {
            set = null;
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
        setEtListeners();
        createSetDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = createSetDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                if (set == null) {
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (getRbWeightedSet().isChecked()) {
                                String errMsg = Constants.EMPTY_STRING;
                                if ((Utils.editableIsZeroOrNullOrEmpty(getEtRepCount().getText()))) {
                                    errMsg += "Please enter in a rep count greater than 0";
                                }
                                if ((Utils.editableIsZeroOrNullOrEmpty(getEtWeightAmount().getText()))) {
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
                                if (Utils.editableIsZeroOrNullOrEmpty(getEtTimedRepCount().getText())) {
                                    errMsg += "Please enter in a rep count greater than 0";
                                }
                                if (Utils.editableIsZeroOrNullOrEmpty(getEtHours().getText()) &&
                                        Utils.editableIsZeroOrNullOrEmpty(getEtMins().getText()) &&
                                        Utils.editableIsZeroOrNullOrEmpty(getEtSeconds().getText())) {
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
                } else {
                    b.setText(getString(R.string.addSetDialog_edit));
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.onEditSetClick(AddSetDialog.this);
                        }
                    });
                }
            }
        });
        displayAndEnableEditingForUi(set, createSetDialog);
        return createSetDialog;
    }

    void displayAndEnableEditingForUi(Set set, AlertDialog dialog) {
        if (set != null) {
            if (set.getExerciseTypeCode().equals(Constants.LOGGED_TIMED)) {
                rbTimedSet.setChecked(true);
                rbWeightedSet.setChecked(false);
                displayTimedView();
                Integer hours = Utils.ensureNonNullInteger(set.getHours());
                Integer mins = Utils.ensureNonNullInteger(set.getMinutes());
                Integer secs = Utils.ensureNonNullInteger(set.getSeconds());

                etHours.setText(Utils.returnTwoDigitString(hours.toString()));
                etMins.setText(Utils.returnTwoDigitString(mins.toString()));
                etSeconds.setText(Utils.returnTwoDigitString(secs.toString()));
                etTimedRepCount.setText(Utils.ensureValidString(set.getReps() + Constants.EMPTY_STRING));
            } else {
                rbTimedSet.setChecked(false);
                rbWeightedSet.setChecked(true);
                displayWeightedView();
                Integer weight = Utils.ensureNonNullInteger(set.getWeight());
                etRepCount.setText(Utils.ensureValidString(set.getReps() + Constants.EMPTY_STRING));
                etWeightAmount.setText(Utils.ensureValidString(weight.toString()));
                if (set.getWeightTypeCode().equals(Constants.LBS)) {
                    rbLbs.setChecked(true);
                    rbKgs.setChecked(false);
                } else {
                    rbLbs.setChecked(false);
                    rbKgs.setChecked(true);
                }
            }
        }
    }

    void setEtListeners() {
        SetEtListener listener = new SetEtListener(etMins);
        SetEtListener listener1 = new SetEtListener(etSeconds);
        etHours.setOnEditorActionListener(listener);
        etMins.setOnEditorActionListener(listener1);
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
                if (checked) {
                    displayWeightedView();
                    break;
                }
            case R.id.rbLoggedTimeType:
                if (checked) {
                    displayTimedView();
                    break;
                }
        }
    }

    void displayWeightedView() {
        llTimedSetView.setVisibility(View.GONE);
        llWeightedSetView.setVisibility(View.VISIBLE);
    }

    void displayTimedView() {
        llTimedSetView.setVisibility(View.VISIBLE);
        llWeightedSetView.setVisibility(View.GONE);
    }

    public RadioButton getRbLbs() {
        return rbLbs;
    }

    public RadioButton getRbKgs() {
        return rbKgs;
    }

    public EditText getEtHours() {
        return etHours;
    }

    public EditText getEtMins() {
        return etMins;
    }

    public EditText getEtSeconds() {
        return etSeconds;
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

    public Set getSet() {
        return set;
    }
}
