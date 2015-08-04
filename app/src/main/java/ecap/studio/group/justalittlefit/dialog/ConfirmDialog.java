package ecap.studio.group.justalittlefit.dialog;

/**
 * Created by Triest on 8/3/2015.
 */

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ecap.studio.group.justalittlefit.R;

/**
 * Base confirm dialog class.
 */
public class ConfirmDialog extends DialogFragment {

    @InjectView(R.id.confirmDialogTextView)
    TextView titleTextView;
    AlertDialog confirmDialog;
    private static final String DELETE_ALL = "Delete All Workouts?";
    private static final String DELETE_SOME = "Delete Workout(s)?";

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(getActivity(), R.style.AppCompatAlertDialogStyle));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.confirm_dialog_view, null);
        builder.setView(view);
        ButterKnife.inject(this, view);
        confirmDialog = builder.create();
        return confirmDialog;
    }

    public void setPositiveButton(String btnText, DialogInterface.OnClickListener onClickListener) {
        setButton(btnText, onClickListener, true);
    }

    public void setNegativeButton(String btnText, DialogInterface.OnClickListener onClickListener) {
        setButton(btnText, onClickListener, false);
    }

    public void setTitleTextView(boolean deleteAll) {
        titleTextView.setText((deleteAll) ? DELETE_ALL : DELETE_SOME);
    }

    private void setButton(String btnText, DialogInterface.OnClickListener onClickListener, boolean isPositiveBtn) {
        int btnType = (isPositiveBtn) ? DialogInterface.BUTTON_POSITIVE : DialogInterface.BUTTON_NEGATIVE;
        confirmDialog.setButton(btnType, btnText, (DialogInterface.OnClickListener) onClickListener);
    }
}

