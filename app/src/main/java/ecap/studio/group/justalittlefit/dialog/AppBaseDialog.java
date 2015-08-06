package ecap.studio.group.justalittlefit.dialog;

/**
 * Created by Triest on 8/3/2015.
 */

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;

import ecap.studio.group.justalittlefit.R;

/**
 * Base dialog class.
 */
public class AppBaseDialog extends DialogFragment {
    AlertDialog confirmDialog;

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(getActivity(), R.style.AppCompatAlertDialogStyle));
        confirmDialog = builder.create();
        return confirmDialog;
    }

    public void setPositiveButton(String btnText, DialogInterface.OnClickListener onClickListener) {
        setButton(btnText, onClickListener, true);
    }

    public void setNegativeButton(String btnText, DialogInterface.OnClickListener onClickListener) {
        setButton(btnText, onClickListener, false);
    }

    public void setTitle(String title) {
        confirmDialog.setTitle(title);
    }

    public void setDialogMessage(String msg) {
        confirmDialog.setMessage(msg);
    }

    private void setButton(String btnText, DialogInterface.OnClickListener onClickListener, boolean isPositiveBtn) {
        int btnType = (isPositiveBtn) ? DialogInterface.BUTTON_POSITIVE : DialogInterface.BUTTON_NEGATIVE;
        confirmDialog.setButton(btnType, btnText, (DialogInterface.OnClickListener) onClickListener);
    }
}

