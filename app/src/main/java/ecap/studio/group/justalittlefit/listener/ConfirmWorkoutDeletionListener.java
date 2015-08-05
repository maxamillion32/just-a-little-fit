package ecap.studio.group.justalittlefit.listener;

import ecap.studio.group.justalittlefit.dialog.AppBaseDialog;

/**
 * Dialog listener to confirm deletions.
 */
public interface ConfirmWorkoutDeletionListener {
    public void onDeleteWorkoutClick(AppBaseDialog dialog);
    public void onDeleteAllWorkoutsClick(AppBaseDialog dialog);
}

