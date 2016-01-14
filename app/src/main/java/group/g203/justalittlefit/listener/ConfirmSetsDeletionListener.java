package group.g203.justalittlefit.listener;

import group.g203.justalittlefit.dialog.AppBaseDialog;

/**
 * Dialog listener to confirm {@link group.g203.justalittlefit.model.Set} deletions.
 */
public interface ConfirmSetsDeletionListener {
    public void onDeleteAllSetsClick(AppBaseDialog dialog);
}
