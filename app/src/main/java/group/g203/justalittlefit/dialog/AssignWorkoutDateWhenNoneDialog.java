package group.g203.justalittlefit.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;

import org.joda.time.DateTime;

import java.util.ArrayList;

import group.g203.justalittlefit.R;
import group.g203.justalittlefit.util.Constants;

/**
 * Dialog to display when a user selects a date or hits the Today view and there is no
 * {@link group.g203.justalittlefit.model.Workout} associated to it.
 */
public class AssignWorkoutDateWhenNoneDialog extends AppBaseDialog {
    DateTime dateTime;

    public static AssignWorkoutDateWhenNoneDialog newInstance(DateTime dateTime) {
        AssignWorkoutDateWhenNoneDialog dialog = new AssignWorkoutDateWhenNoneDialog();

        Bundle args = new Bundle();
        args.putSerializable(Constants.ASSIGN_DATE, dateTime);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        builder.setTitle(getString(R.string.assignWorkoutDateWhenNoneDialog_Title));
        builder.setMessage(getString(R.string.assignWorkoutDateWhenNoneDialog_msg));

        Bundle args = getArguments();
        if (args != null && args.containsKey(Constants.ASSIGN_DATE)) {
            dateTime = (DateTime) args.getSerializable(Constants.ASSIGN_DATE);
        }
        builder.setPositiveButton(getString(R.string.assignWorkoutDateWhenNoneDialog_assign), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Will be overridden
            }
        });
        builder.setNegativeButton(getString(R.string.assignWorkoutDateWhenNoneDialog_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // onClick will close dialog
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        if (getActivity() != null) {
                            ArrayList<DateTime> dateTimeList = new ArrayList<>();
                            dateTimeList.add(dateTime);

                            FragmentManager fm = getActivity().getSupportFragmentManager();
                            AssignWorkoutDialog dialog = AssignWorkoutDialog.newInstance(dateTimeList);
                            dialog.show(fm, getString(R.string.assignWorkoutDialogTag));
                        }
                    }
                });
            }
        });
        return alertDialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
}
