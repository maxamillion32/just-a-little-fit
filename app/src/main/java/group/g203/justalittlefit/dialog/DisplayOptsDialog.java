package group.g203.justalittlefit.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import group.g203.justalittlefit.R;
import group.g203.justalittlefit.util.Utils;

/**
 * Dialog that allows user to hide or display tooltips and {@link group.g203.justalittlefit.dialog.AssignWorkoutDateWhenNoneDialog}.
 */
public class DisplayOptsDialog extends AppBaseDialog {
    @Bind(R.id.rbDialog_yes)
    RadioButton rbDialogYes;
    @Bind(R.id.rbDialog_no)
    RadioButton rbDialogNo;
    @Bind(R.id.rbFabNoti_yes)
    RadioButton rbFabNotiYes;
    @Bind(R.id.rbFabNoti_no)
    RadioButton rbFabNotiNo;

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.display_opts_dialog_view, null);
        ButterKnife.bind(this, view);
        builder.setTitle(getString(R.string.display_opts));
        builder.setView(view);
        builder.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Will exit dialog
            }
        });
        final AlertDialog displayOptsDialog = builder.create();
        displayOptsDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                if (Utils.getAssignDialogPref(getActivity())) {
                    rbDialogYes.setChecked(true);
                    rbDialogNo.setChecked(false);
                } else {
                    rbDialogYes.setChecked(false);
                    rbDialogNo.setChecked(true);
                }

                if (Utils.getFabNotiShowPref(getActivity())) {
                    rbFabNotiYes.setChecked(true);
                    rbFabNotiNo.setChecked(false);
                } else {
                    rbFabNotiYes.setChecked(false);
                    rbFabNotiNo.setChecked(true);
                }
            }
        });
        return displayOptsDialog;
    }

    @OnClick(R.id.rbDialog_yes)
    void onShowDialogYes(View view) {
        Utils.setAssignDialogPref(getActivity(), true);
    }

    @OnClick(R.id.rbDialog_no)
    void onShowDialogNo(View view) {
        Utils.setAssignDialogPref(getActivity(), false);
    }

    @OnClick(R.id.rbFabNoti_yes)
    void onShowTooltipYes(View view) {
        Utils.setFabNotiShowPref(getActivity(), true);
    }

    @OnClick(R.id.rbFabNoti_no)
    void onShowTooltipNo(View view) {
        Utils.setFabNotiShowPref(getActivity(), false);
    }
}