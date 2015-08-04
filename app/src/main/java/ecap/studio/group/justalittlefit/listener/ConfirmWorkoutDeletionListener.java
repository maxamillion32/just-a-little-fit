package ecap.studio.group.justalittlefit.listener;

import ecap.studio.group.justalittlefit.dialog.ConfirmDialog;

/**
 * Dialog listener to confirm deletions.
 */
public interface ConfirmWorkoutDeletionListener {
    public void onDeleteWorkoutClick(ConfirmDialog dialog);
    public void onDeleteAllWorkoutsClick(ConfirmDialog dialog);
}

