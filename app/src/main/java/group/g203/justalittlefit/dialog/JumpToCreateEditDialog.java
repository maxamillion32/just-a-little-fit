package group.g203.justalittlefit.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;

import group.g203.justalittlefit.R;
import group.g203.justalittlefit.activity.CreateEditWorkout;

public class JumpToCreateEditDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(getActivity(), R.style.AppCompatAlertDialogStyle));

        builder.setTitle(getString(R.string.jumpToCreateEditDialog_Title));
        builder.setPositiveButton(getString(R.string.goToCreateEdit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Activity activity = getActivity();
                Intent createEditWorkoutIntent =
                        new Intent(activity, CreateEditWorkout.class);
                createEditWorkoutIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                activity.startActivity(createEditWorkoutIntent);
            }
        });
        builder.setNegativeButton(getString(R.string.noGoToCreateEdit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // onClick will close dialog
            }
        });
        builder.setMessage((getString(R.string.optForCreateEdit)));
        final AlertDialog alertDialog = builder.create();
        return alertDialog;
    }
}
