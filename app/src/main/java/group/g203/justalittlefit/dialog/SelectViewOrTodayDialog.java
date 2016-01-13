package group.g203.justalittlefit.dialog;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;

import group.g203.justalittlefit.R;
import group.g203.justalittlefit.listener.SelectViewOrTodayDialogListener;
import group.g203.justalittlefit.util.Constants;

/**
 * Dialog that displays when user selects today's date when in
 * {@link group.g203.justalittlefit.activity.ChooseWorkoutDate} activity.
 */
public class SelectViewOrTodayDialog extends DialogFragment {
    private SelectViewOrTodayDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(getActivity(), R.style.AppCompatAlertDialogStyle));

        builder.setTitle(getString(R.string.selectViewOrTodayDialog_Title));
        builder.setPositiveButton(getString(R.string.selectTodayOption), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onSelectTodayDialog(SelectViewOrTodayDialog.this);
            }
        });
        builder.setNeutralButton(getString(R.string.selectViewOption), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onSelectViewDialog(SelectViewOrTodayDialog.this);
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // onClick will close dialog
            }
        });
        builder.setMessage((getString(R.string.selectInfo) + Constants.NEWLINE
                + Constants.NEWLINE + getString(R.string.selectViewInfo)
                + Constants.NEWLINE + Constants.NEWLINE + getString(R.string.selectTodayInfo)));
        final AlertDialog alertDialog = builder.create();
        return alertDialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (SelectViewOrTodayDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + Constants.SPACE +
                    getString(R.string.noImplementsAddExerciseDialogListener));
        }
    }

}
