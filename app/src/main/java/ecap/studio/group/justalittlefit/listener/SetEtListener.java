package ecap.studio.group.justalittlefit.listener;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import ecap.studio.group.justalittlefit.util.Utils;

public class SetEtListener implements EditText.OnEditorActionListener {

    private EditText editText;

    public SetEtListener(EditText editText) {
        this.editText = editText;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (event != null) {
            if (actionId == EditorInfo.IME_ACTION_NEXT ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if (!event.isShiftPressed()) {
                    CharSequence vSeq = v.getText();
                    if (vSeq != null) {
                        String vString = Utils.ensureValidString(vSeq.toString()).trim();
                        if (vString.length() < 2) {
                            v.setText("0" + vString);
                            editText.requestFocus();
                            return true;
                        } else {
                            return true;
                        }
                    } else {
                        return false;
                    }
                }
                return false;
            }
        } else {
            if (actionId == EditorInfo.IME_ACTION_NEXT ||
                    actionId == EditorInfo.IME_ACTION_DONE) {
                CharSequence vSeq = v.getText();
                if (vSeq != null) {
                    String vString = Utils.ensureValidString(vSeq.toString()).trim();
                    if (vString.length() < 2) {
                        v.setText("0" + vString);
                        editText.requestFocus();
                        return true;
                    } else {
                        return true;
                    }
                }
            } else {
                return false;
            }
        }
        return false;
    }
}
