package ecap.studio.group.justalittlefit.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.listener.ConfirmSetsDeletionListener;
import ecap.studio.group.justalittlefit.util.Constants;

/**
 * Dialog that will confirm deletion of all Sets in view.
 */
public class ConfirmDeleteSetsDialog extends AppBaseDialog {
    private static final String DELETE_ALL_SETS = "Delete All Sets?";
    ConfirmSetsDeletionListener listener;

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        setTitle(DELETE_ALL_SETS);
        setPositiveButton(getString(R.string.delete_all), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onDeleteAllSetsClick(ConfirmDeleteSetsDialog.this);
            }
        });
        setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // onClick will close dialog
            }
        });
        return this.confirmDialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (ConfirmSetsDeletionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + Constants.SPACE +
                    getString(R.string.noImplementsConfirmSetDeletionListener));
        }
    }

}