package ecap.studio.group.justalittlefit.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;

import butterknife.ButterKnife;
import ecap.studio.group.justalittlefit.R;

public class LibraryCreditsDialog extends DialogFragment {

    private static final String TITLE = "Open Source Library Credits";

    public static LibraryCreditsDialog newInstance() {
        LibraryCreditsDialog dialog = new LibraryCreditsDialog();
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(getActivity(), R.style.AppCompatAlertDialogStyle));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.library_credits_dialog_view, null);
        ButterKnife.inject(this, view);

        builder.setTitle(TITLE);
        builder.setView(view);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // onClick will close dialog
            }
        });
        final AlertDialog libCredDialog = builder.create();
        return libCredDialog;
    }
}
