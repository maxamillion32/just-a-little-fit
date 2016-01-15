package group.g203.justalittlefit.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import group.g203.justalittlefit.R;
import group.g203.justalittlefit.activity.CreateEditWorkout;

/**
 * Dialog that can take user to the
 * {@link group.g203.justalittlefit.activity.CreateEditWorkout} activity.
 */
public class JumpToCreateEditDialog extends AppBaseDialog {

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

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
