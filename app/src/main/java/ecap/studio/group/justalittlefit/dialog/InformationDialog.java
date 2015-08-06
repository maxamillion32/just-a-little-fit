package ecap.studio.group.justalittlefit.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.util.Constants;

/**
 * Info dialog class.
 */
public class InformationDialog extends AppBaseDialog {
    private static final String INFO_TITLE = "Information";

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        setTitle(INFO_TITLE);
        setDialogMessage(getString(R.string.deleteBySwipe) + Constants.NEWLINE
                + Constants.NEWLINE + getString(R.string.dragSort));
        setPositiveButton(getString(R.string.got_it), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // onClick will close dialog
            }
        });
        return this.confirmDialog;
    }
}
