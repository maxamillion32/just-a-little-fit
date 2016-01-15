package group.g203.justalittlefit.dialog;

/**
 * Created by Triest on 8/3/2015.
 */

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;

import group.g203.justalittlefit.R;

/**
 * Base dialog class.
 */
public class AppBaseDialog extends DialogFragment {
    AlertDialog alertDialog;
    AlertDialog.Builder builder;

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(
                new ContextThemeWrapper(getActivity(), R.style.AppCompatAlertDialogStyle));
        builder = alertBuilder;
        alertDialog = alertBuilder.create();
        return alertDialog;
    }

    public void setPositiveButton(String btnText, DialogInterface.OnClickListener onClickListener) {
        setButton(btnText, onClickListener, true);
    }

    public void setNegativeButton(String btnText, DialogInterface.OnClickListener onClickListener) {
        setButton(btnText, onClickListener, false);
    }

    public void setTitle(String title) {
        alertDialog.setTitle(title);
    }

    public void setDialogMessage(String msg) {
        alertDialog.setMessage(msg);
    }

    private void setButton(String btnText, DialogInterface.OnClickListener onClickListener, boolean isPositiveBtn) {
        int btnType = (isPositiveBtn) ? DialogInterface.BUTTON_POSITIVE : DialogInterface.BUTTON_NEGATIVE;
        alertDialog.setButton(btnType, btnText, (DialogInterface.OnClickListener) onClickListener);
    }

    public AlertDialog getAlertDialog() {
        return alertDialog;
    }

    public AlertDialog.Builder getBuilder() {
        return builder;
    }
}

