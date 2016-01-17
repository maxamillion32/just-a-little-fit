package group.g203.justalittlefit.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import butterknife.ButterKnife;
import group.g203.justalittlefit.R;

/**
 * Dialog that displays the libraries used in the app and their respective licenses.
 */
public class LibraryCreditsDialog extends AppBaseDialog {

    private static final String TITLE = "Open Source Library Credits";

    public static LibraryCreditsDialog newInstance() {
        LibraryCreditsDialog dialog = new LibraryCreditsDialog();
        return dialog;
    }

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.library_credits_dialog_view, null);
        ButterKnife.bind(this, view);

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
