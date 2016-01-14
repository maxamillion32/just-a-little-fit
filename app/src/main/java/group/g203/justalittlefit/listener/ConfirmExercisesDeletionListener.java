package group.g203.justalittlefit.listener;

import group.g203.justalittlefit.dialog.AppBaseDialog;

/**
 * Dialog listener to confirm {@link group.g203.justalittlefit.model.Exercise} deletions.
 */
public interface ConfirmExercisesDeletionListener {
    public void onDeleteAllExercisesClick(AppBaseDialog dialog);
}
