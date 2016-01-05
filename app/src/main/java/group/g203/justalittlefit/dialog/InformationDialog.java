package group.g203.justalittlefit.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Html;

import group.g203.justalittlefit.R;
import group.g203.justalittlefit.util.Constants;
import group.g203.justalittlefit.util.Utils;

/**
 * Info dialog class.
 */
public class InformationDialog extends AppBaseDialog {
    private static final String INFO_TITLE = "Information";
    private static final String LIBS_TITLE = "Open Source Library Credits";

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
        String dialogBtnTxt = getString(R.string.got_it);
        String dialogType = Utils.ensureValidString(
                getArguments().getString(Constants.DIALOG_TYPE));
        switch(dialogType) {
            case Constants.EXERCISES_NORM_CASE:
                setDialogMessage(getString(R.string.deleteBySwipe_exercise) + Constants.NEWLINE
                        + Constants.NEWLINE + getString(R.string.dragSort_exercise)
                        + Constants.NEWLINE + Constants.NEWLINE + getString(R.string.onClick_exercise)
                        + Constants.NEWLINE + Constants.NEWLINE + getString(R.string.onLongClick_exercise)
                        + Constants.NEWLINE + Constants.NEWLINE + getString(R.string.onSave));
                break;
            case Constants.SETS_NORM_CASE:
                setDialogMessage(getString(R.string.deleteBySwipe_set) + Constants.NEWLINE
                        + Constants.NEWLINE + getString(R.string.dragSort_set)
                        + Constants.NEWLINE + Constants.NEWLINE + getString(R.string.onClick_set)
                        + Constants.NEWLINE + Constants.NEWLINE + getString(R.string.onSave));
                break;
            case Constants.WORKOUTS_NORM_CASE:
                setDialogMessage(getString(R.string.deleteBySwipe) + Constants.NEWLINE
                        + Constants.NEWLINE + getString(R.string.dragSort)
                        + Constants.NEWLINE + Constants.NEWLINE + getString(R.string.onClick)
                        + Constants.NEWLINE + Constants.NEWLINE + getString(R.string.onLongClick)
                        + Constants.NEWLINE + Constants.NEWLINE + getString(R.string.onSave));
                break;
            case Constants.ASSIGN:
                setDialogMessage(getString(R.string.select_dates_info) + Constants.NEWLINE
                        + Constants.NEWLINE + getString(R.string.select_dates_today_info)
                        + Constants.NEWLINE + Constants.NEWLINE + getString(R.string.mulitple_dates_info)
                        + Constants.NEWLINE + Constants.NEWLINE + getString(R.string.duplicate_workout_assign_info));
                break;
            case Constants.CHOOSE:
                setDialogMessage(getString(R.string.choose_date_info) + Constants.NEWLINE
                        + Constants.NEWLINE + getString(R.string.select_dates_today_info));
                break;
            case Constants.VIEW_TEXT:
                setDialogMessage(getString(R.string.view_workout_info) + Constants.NEWLINE
                        + Constants.NEWLINE + getString(R.string.view_multiple_workout_info)
                        + Constants.NEWLINE + Constants.NEWLINE + getString(R.string.view_workout_strikethrough));
                break;
            case Constants.TODAY:
                setDialogMessage(getString(R.string.view_today_workout_info) + Constants.NEWLINE
                        + Constants.NEWLINE + getString(R.string.today_expand_info)
                        + Constants.NEWLINE + Constants.NEWLINE + getString(R.string.today_rename_workout)
                        + Constants.NEWLINE + Constants.NEWLINE + getString(R.string.today_check_info)
                        + Constants.NEWLINE + Constants.NEWLINE + getString(R.string.today_delete_info)
                        + Constants.NEWLINE + Constants.NEWLINE + getString(R.string.today_drag_info)
                        + Constants.NEWLINE + Constants.NEWLINE + getString(R.string.onSave));
                break;
            case Constants.CHOOSER:
                setDialogMessage(getString(R.string.choose_today_workout));
                break;
            case Constants.HOME:
                setDialogMessage(getString(R.string.home_today_info) + Constants.NEWLINE
                        + Constants.NEWLINE + getString(R.string.home_create_edit_info)
                        + Constants.NEWLINE + Constants.NEWLINE + getString(R.string.home_assign_info)
                        + Constants.NEWLINE + Constants.NEWLINE + getString(R.string.home_view_info));
                break;
            case Constants.LIBS:
                setTitle(LIBS_TITLE);
                dialogBtnTxt = getString(R.string.ok);
                setDialogMessage(Html.fromHtml(getString(R.string.picasso)) + Constants.NEWLINE + getString(R.string.picasso_info)
                        + Constants.NEWLINE + Constants.NEWLINE + getString(R.string.adv_rv)
                        + Constants.NEWLINE + getString(R.string.adv_rv_info)  + Constants.NEWLINE
                        + getString(R.string.butter_knife) + Constants.NEWLINE + getString(R.string.butter_knife_info)
                        + Constants.NEWLINE + Constants.NEWLINE + getString(R.string.otto)
                        + Constants.NEWLINE + getString(R.string.otto_info)
                        + Constants.NEWLINE + Constants.NEWLINE + getString(R.string.joda_time)
                        + Constants.NEWLINE + getString(R.string.joda_time_info)
                        + Constants.NEWLINE + Constants.NEWLINE + getString(R.string.circle_indicator)
                        + Constants.NEWLINE + getString(R.string.circle_indicator_info)
                        + Constants.NEWLINE + Constants.NEWLINE + getString(R.string.android_times_square)
                        + Constants.NEWLINE + getString(R.string.android_times_square_info)
                        + Constants.NEWLINE + Constants.NEWLINE + getString(R.string.orm_lite)
                        + Constants.NEWLINE + getString(R.string.orm_lite_info)
                        + Constants.NEWLINE + Constants.NEWLINE + getString(R.string.picasso)
                        + Constants.NEWLINE + getString(R.string.picasso_info));
                break;
        }
        setPositiveButton(dialogBtnTxt, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // onClick will close dialog
            }
        });
        return this.confirmDialog;
    }
}
