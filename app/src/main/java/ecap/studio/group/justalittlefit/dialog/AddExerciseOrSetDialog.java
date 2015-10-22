package ecap.studio.group.justalittlefit.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;

import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.util.Utils;

public class AddExerciseOrSetDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(getActivity(), R.style.AppCompatAlertDialogStyle));
        builder.setTitle(getString(R.string.addExerciseOrSetDialog_Title));
        builder.setPositiveButton(getString(R.string.addExerciseOrSetDialog_addExercise), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               displayDialog(false);
            }
        });
        builder.setNeutralButton(getString(R.string.addExerciseOrSetDialog_addSet), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                displayDialog(true);
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // onClick will close dialog
            }
        });
        final AlertDialog alertDialog = builder.create();
        return alertDialog;
    }

    private void displayDialog(boolean displaySetDialog) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        if (displaySetDialog) {
            AddSetDialog dialog = new AddSetDialog();
            dialog.show(fm, getString(R.string.addSetDialogTag));
        } else {
            AddExerciseDialog dialog = new AddExerciseDialog();
            dialog.show(fm, getString(R.string.addExerciseDialogTag));
        }
    }
}
