package ecap.studio.group.justalittlefit.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.util.Constants;
import ecap.studio.group.justalittlefit.util.Utils;

/**
 * Info dialog class.
 */
public class InformationDialog extends AppBaseDialog {
    private static final String INFO_TITLE = "Information";

    public static InformationDialog newInstance(String dialogType) {
        InformationDialog dialog = new InformationDialog();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DIALOG_TYPE, dialogType);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        setTitle(INFO_TITLE);
        String dialogType = Utils.ensureValidString(
                getArguments().getString(Constants.DIALOG_TYPE));
        switch(dialogType) {
            case Constants.EXERCISES_NORM_CASE:
                setDialogMessage(getString(R.string.deleteBySwipe_exercise) + Constants.NEWLINE
                        + Constants.NEWLINE + getString(R.string.dragSort_exercise));
                break;
            case Constants.SETS_NORM_CASE:
                break;
            case Constants.WORKOUTS_NORM_CASE:
                setDialogMessage(getString(R.string.deleteBySwipe) + Constants.NEWLINE
                        + Constants.NEWLINE + getString(R.string.dragSort));
                break;
        }
        setPositiveButton(getString(R.string.got_it), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // onClick will close dialog
            }
        });
        return this.confirmDialog;
    }
}
