package group.g203.justalittlefit.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import group.g203.justalittlefit.R;
import group.g203.justalittlefit.advanced_recyclerview.rv_today.AbstractExpandableDataProvider;
import group.g203.justalittlefit.advanced_recyclerview.rv_today.TodayDataProvider;
import group.g203.justalittlefit.advanced_recyclerview.rv_today.TodayDataProviderFragment;
import group.g203.justalittlefit.advanced_recyclerview.rv_today.TodayRvListViewFragment;
import group.g203.justalittlefit.database.DbAsyncTask;
import group.g203.justalittlefit.database.DbConstants;
import group.g203.justalittlefit.database.DbFunctionObject;
import group.g203.justalittlefit.database.DbTaskResult;
import group.g203.justalittlefit.dialog.AddExerciseDialog;
import group.g203.justalittlefit.dialog.AddExerciseOrSetDialog;
import group.g203.justalittlefit.dialog.AddSetDialog;
import group.g203.justalittlefit.dialog.AppBaseDialog;
import group.g203.justalittlefit.dialog.ConfirmDeleteTodayWorkoutDialog;
import group.g203.justalittlefit.dialog.InformationDialog;
import group.g203.justalittlefit.dialog.RenameDialog;
import group.g203.justalittlefit.listener.AddExerciseDialogListener;
import group.g203.justalittlefit.listener.AddSetDialogListener;
import group.g203.justalittlefit.listener.ConfirmDeleteTodayWorkoutListener;
import group.g203.justalittlefit.listener.RenameDialogListener;
import group.g203.justalittlefit.model.Exercise;
import group.g203.justalittlefit.model.Set;
import group.g203.justalittlefit.model.Workout;
import group.g203.justalittlefit.util.BusFactory;
import group.g203.justalittlefit.util.Constants;
import group.g203.justalittlefit.util.Utils;

/**
 * Actiity that displays a {@link group.g203.justalittlefit.model.Workout} in the
 * Today view which allows a user to fully edit said {@link group.g203.justalittlefit.model.Workout}.
 */
public class TodayActivity extends BaseNaviDrawerActivity implements AddExerciseDialogListener,
        AddSetDialogListener, ConfirmDeleteTodayWorkoutListener, RenameDialogListener {

    private final String LOG_TAG = getClass().getSimpleName();
    private static final String FRAGMENT_TAG_DATA_PROVIDER = "data provider";
    private static final String FRAGMENT_LIST_VIEW = "list view";

    FloatingActionButton fab;
    CoordinatorLayout clFab;
    boolean busRegistered;
    Workout todayWorkout;
    String addedExerciseName;
    Exercise parentExercise;
    Set addedSet;
    @Bind(R.id.rlDefault)
    RelativeLayout rlDefault;
    Integer editedGroupPosition;
    Integer editedChildPosition;
    Bundle savedBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            getIntent().getExtras().putBundle(Constants.SAVED_BUNDLE, savedInstanceState);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(Constants.SAVED_BUNDLE)) {
            savedBundle = extras.getBundle(Constants.SAVED_BUNDLE);
        }
        super.onNewIntent(intent);
        setIntent(intent);
    }

    void displayTodayWorkout() {
        if (todayWorkout != null) {
            setTitle(Utils.returnStandardDateString(todayWorkout.getWorkoutDate()) + Constants.COLON +
                    Constants.SPACE + todayWorkout.getName());
            DbFunctionObject getFullWorkoutDfo = new DbFunctionObject(todayWorkout, DbConstants.GET_FULL_WORKOUT);
            new DbAsyncTask(Constants.TODAY).execute(getFullWorkoutDfo);
        } else {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.workout_modify_error));
            hideProgressDialog();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info_delete_single, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_delete:
                displayConfirmDeleteWorkoutDialog();
                break;
            case R.id.action_rename:
                displayRenameWorkoutDialog();
                break;
            case R.id.action_info:
                displayInfoDialog();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    void setupDrawerContent(NavigationView navigationView) {
        // Check menu item of currently displayed activity
        MenuItem selectedItem = navigationView.getMenu().findItem(R.id.navi_today);
        selectedItem.setChecked(true);
        super.setupDrawerContent(navigationView);
    }

    private void setupFloatingActionButton(final BaseNaviDrawerActivity activity) {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        clFab = (CoordinatorLayout) findViewById(R.id.clFab);
        clFab.setVisibility(View.VISIBLE);
        fab.setImageResource(R.drawable.ic_plus_white);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAddExerciseOrSetDialog();
            }
        });
    }

    @Subscribe
    public void onAsyncTaskResult(DbTaskResult event) {
        if (event == null || event.getResult() == null) {
            displayGeneralWorkoutListError();
        } else if (event.getResult() instanceof Workout) {
            Workout workoutObj = (Workout) event.getResult();
            todayWorkout = workoutObj;
            List<Fragment> frags = getSupportFragmentManager().getFragments();

            getSupportFragmentManager().beginTransaction()
                    .add(TodayDataProviderFragment.newInstance(new ArrayList<>(workoutObj.getExercises())), FRAGMENT_TAG_DATA_PROVIDER)
                    .commitAllowingStateLoss();

            if (Utils.collectionIsNullOrEmpty(frags)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, new TodayRvListViewFragment(), FRAGMENT_LIST_VIEW)
                        .commitAllowingStateLoss();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new TodayRvListViewFragment(), FRAGMENT_LIST_VIEW)
                        .commitAllowingStateLoss();
            }

            if (Utils.collectionIsNullOrEmpty(todayWorkout.getExercises())) {
                rlDefault.setVisibility(View.VISIBLE);
            } else {
                rlDefault.setVisibility(View.INVISIBLE);
            }
        } else if (event.getResult() instanceof ArrayList) {
            ArrayList<Workout> workouts = (ArrayList<Workout>) event.getResult();
            if (!Utils.collectionIsNullOrEmpty(workouts)) {
                Workout workout = workouts.get(0);
                Utils.displayLongSimpleSnackbar(fab, getString(R.string.renameDialog_WorkoutSuccess));
                setTitle(Utils.returnStandardDateString(workout.getWorkoutDate()) + Constants.COLON +
                        Constants.SPACE + Utils.ensureValidString(workout.getName()));
            } else {
                displayGeneralWorkoutListError();
            }
        } else if (event.getResult() instanceof LinkedList) {
            LinkedList<Exercise> exercises = (LinkedList<Exercise>) event.getResult();
            if (!Utils.collectionIsNullOrEmpty(exercises)) {
                Utils.displayLongSimpleSnackbar(fab, getString(R.string.renameDialog_ExerciseSuccess));
                DbFunctionObject getFullWorkoutDfo = new DbFunctionObject(todayWorkout, DbConstants.GET_FULL_WORKOUT);
                new DbAsyncTask(Constants.TODAY).execute(getFullWorkoutDfo);
            } else {
                displayGeneralWorkoutListError();
            }
        } else if (event.getResult() instanceof Exercise) {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.addExercise_success));
            DbFunctionObject getFullWorkoutDfo = new DbFunctionObject(todayWorkout, DbConstants.GET_FULL_WORKOUT);
            new DbAsyncTask(Constants.TODAY).execute(getFullWorkoutDfo);
        } else if (event.getResult() instanceof Set) {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.addSet_success));
            DbFunctionObject getFullWorkoutDfo = new DbFunctionObject(todayWorkout, DbConstants.GET_FULL_WORKOUT);
            new DbAsyncTask(Constants.TODAY).execute(getFullWorkoutDfo);
        } else if (event.getResult() instanceof String) {
            // onPause delete returned, reorder workouts before leaving activity
            reorderWorkouts();
        } else if (event.getResult() instanceof Boolean) {
            Utils.displayLongToast(this, getString(R.string.workout_deleted));
            Intent intent = new Intent(this, Home.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (event.getResult() instanceof Double) {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.editSet_success));
        } else if (event.getResult() instanceof Map) {
            // Data order saved and onPause returned with data updated on screen
        } else {
            displayGeneralWorkoutListError();
        }
        hideProgressDialog();
    }

    private void displayInfoDialog() {
        FragmentManager fm = getSupportFragmentManager();
        InformationDialog dialog = InformationDialog.newInstance(Constants.TODAY);
        dialog.show(fm, getString(R.string.infoDialogTagToday));
    }

    private void displayRenameWorkoutDialog() {
        FragmentManager fm = getSupportFragmentManager();
        RenameDialog dialog = RenameDialog.newInstance(todayWorkout);
        dialog.show(fm, getString(R.string.renameDialog_Tag));
    }

    private void displayRenameExerciseDialog(Exercise exercise) {
        FragmentManager fm = getSupportFragmentManager();
        RenameDialog dialog = RenameDialog.newInstance(exercise);
        dialog.show(fm, getString(R.string.renameDialog_Tag));
    }

    private void displayConfirmDeleteWorkoutDialog() {
        FragmentManager fm = getSupportFragmentManager();
        ConfirmDeleteTodayWorkoutDialog dialog = new ConfirmDeleteTodayWorkoutDialog();
        dialog.show(fm, getString(R.string.confirmDeleteTodayWorkoutDialogTag));
    }

    private void getWorkout() {
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(Constants.WORKOUT)) {
            todayWorkout = extras.getParcelable(Constants.WORKOUT);
        }
    }

    public void onGroupItemRemoved(Exercise exercise) {
        determineDefaultStatus();
        Integer removeResult = Utils.removeExercise(todayWorkout.getExercises(),
                Utils.ensureValidString(exercise.getName().trim()));
        if (removeResult == Constants.INT_ONE) {
            Utils.displayLongActionSnackbar(fab, getString(R.string.exercise_deleted),
                    Constants.UNDO, undoDelete(),
                    getResources().getColor(R.color.app_blue_gray));
        } else {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.workout_modify_error));
        }
    }

    public void onChildItemRemoved(Set set) {
        Integer removeResult = Utils.removeSet(set.getExercise().getSets(),
                Utils.ensureValidString(set.toString().trim()));
        if (removeResult == Constants.INT_ONE) {
            Utils.displayLongActionSnackbar(fab, getString(R.string.set_deleted),
                    Constants.UNDO, undoDelete(),
                    getResources().getColor(R.color.app_blue_gray));
        } else {
            Utils.displayLongActionSnackbar(fab, getString(R.string.workout_modify_error),
                    Constants.UNDO, undoDelete(),
                    getResources().getColor(R.color.app_blue_gray));
        }
    }

    private View.OnClickListener undoDelete() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUndo();
            }
        };
    }

    private void onUndo() {
        TodayDataProvider dataProvider =
                (TodayDataProvider)getDataProvider();
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST_VIEW);
        final long result = dataProvider.undoLastRemoval();

        if (result == RecyclerViewExpandableItemManager.NO_EXPANDABLE_POSITION) {
            return;
        }

        final int groupPosition = RecyclerViewExpandableItemManager.getPackedPositionGroup(result);
        final int childPosition = RecyclerViewExpandableItemManager.getPackedPositionChild(result);

        if (childPosition == RecyclerView.NO_POSITION) {
            // group item
            ((TodayRvListViewFragment) fragment).notifyGroupItemRestored(groupPosition);
            AbstractExpandableDataProvider.GroupData data = dataProvider.getGroupItem(groupPosition);
            Exercise exercise = data.getExercise();
            todayWorkout.getExercises().add(exercise);
            Utils.displayLongSimpleSnackbar(fab,
                    getString(R.string.exercise_removal_undone));
            determineDefaultStatus();
        } else {
            // child item
            ((TodayRvListViewFragment) fragment).notifyChildItemRestored(groupPosition, childPosition);
            AbstractExpandableDataProvider.ChildData data = dataProvider.getChildItem(groupPosition, childPosition);
            Set set = data.getSet();
            set.getExercise().getSets().add(set);
            Utils.displayLongSimpleSnackbar(fab,
                    getString(R.string.set_removal_undone));
            determineDefaultStatus();
        }
    }

    // Null check
    public AbstractExpandableDataProvider getDataProvider() {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_DATA_PROVIDER);
        if (fragment != null) {
            return ((TodayDataProviderFragment) fragment).getDataProvider();
        } else {
            return null;
        }
    }

    void determineDefaultStatus() {
        TodayDataProvider dataProvider =
                (TodayDataProvider)getDataProvider();

        if (Utils.dataProviderIsValid(dataProvider)) {

            if (dataProvider != null && dataProvider.getGroupCount() == 0) {
                rlDefault.setVisibility(View.VISIBLE);
            } else {
                rlDefault.setVisibility(View.INVISIBLE);
            }
        } else {
            Utils.exitActivityOnError(this);
        }
    }

    private void registerBus() {
        if (!busRegistered) {
            BusFactory.getTodayBus().register(this);
            busRegistered = true;
        }
    }

    private void unregisterBus() {
        if (busRegistered) {
            BusFactory.getTodayBus().unregister(this);
            busRegistered = false;
        }
    }

    public void onChildItemClicked(int groupPosition, int childPosition) {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST_VIEW);
        ((TodayRvListViewFragment) fragment).notifyChildItemRestored(groupPosition, childPosition);
        TodayDataProvider dataProvider =
                (TodayDataProvider)getDataProvider();
        if (Utils.dataProviderIsValid(dataProvider)) {
            AbstractExpandableDataProvider.ChildData data = dataProvider.getChildItem(groupPosition, childPosition);
            editedChildPosition = childPosition;
            editedGroupPosition = groupPosition;
            Set set = data.getSet();
            displayAddSetDialogUponEdit(set);
        } else {
            Utils.exitActivityOnError(this);
        }
    }

    public void renameExercise(Exercise exercise) {
        displayRenameExerciseDialog(exercise);
    }

    private void displayAddSetDialogUponEdit(Set set) {
        FragmentManager fm = getSupportFragmentManager();
        AddSetDialog dialog = AddSetDialog.newInstance(set);
        dialog.show(fm, getString(R.string.addSetDialogTag));
    }

    @Override
    protected void onDestroy() {
        unregisterBus();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        showProgressDialog();
        registerBus();
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_today, null, false);
        frameLayout.addView(contentView, 0);
        ButterKnife.bind(this, frameLayout);
        setTitle(R.string.today_title_string);
        setupFloatingActionButton(this);
        getWorkout();

        if (savedBundle == null) {
            displayTodayWorkout();
        }
        MenuItem selectedItem = navigationView.getMenu().findItem(R.id.navi_today);
        selectedItem.setChecked(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterBus();
        reorderWorkouts();
        hideProgressDialog();
    }

    private void reorderWorkouts() {
        TodayDataProvider dataProvider =
                (TodayDataProvider) getDataProvider();
        if (Utils.dataProviderIsValid(dataProvider)) {
            List<Object> reorderedExercisesAndSets = dataProvider.getOrderedDataObjects();
            List<Exercise> reorderedExercises = new ArrayList<>();
            List<Set> reorderedSets = new ArrayList<>();
            HashMap<String, Object> reorderedExercisesAndSetsMap = new HashMap<>();

            for (Object object : reorderedExercisesAndSets) {
                if (object instanceof Set) {
                    reorderedSets.add((Set) object);
                } else if (object instanceof Exercise) {
                    reorderedExercises.add((Exercise) object);
                }
            }

            reorderedExercisesAndSetsMap.put(Constants.EXERCISES, reorderedExercises);
            reorderedExercisesAndSetsMap.put(Constants.SETS_NORM_CASE, reorderedSets);

            DbFunctionObject reorderExercisesAndSetsDfo =
                    new DbFunctionObject(reorderedExercisesAndSetsMap, DbConstants.UPDATE_EXERCISES_AND_SETS);
            new DbAsyncTask(Constants.TODAY).execute(reorderExercisesAndSetsDfo);
        } else {
            Utils.exitActivityOnError(this);
        }
    }

    private void displayAddExerciseOrSetDialog() {
        FragmentManager fm = getSupportFragmentManager();
        if (Utils.collectionIsNullOrEmpty(todayWorkout.getExercises())) {
            AddExerciseDialog dialog = new AddExerciseDialog();
            dialog.show(fm, getString(R.string.addExerciseDialogTag));
        } else {
            AddExerciseOrSetDialog dialog = AddExerciseOrSetDialog.newInstance(todayWorkout,
                    new ArrayList<>(todayWorkout.getExercises()));
            dialog.show(fm, getString(R.string.addExerciseOrSetDialogTag));
        }
    }

    void displayGeneralWorkoutListError() {
        String errorMsg = getString(R.string.workout_list_error);
        Log.e(LOG_TAG, errorMsg);
        Utils.displayLongSimpleSnackbar(this.findViewById(R.id.fab), errorMsg);
    }

    @Override
    public void onAddExerciseClick(AddExerciseDialog dialog) {
        showProgressDialog();
        addedExerciseName = Utils.ensureValidString(dialog.getAddExerciseText().getText().toString());

        TodayDataProvider dataProvider =
                (TodayDataProvider)getDataProvider();

        if (Utils.dataProviderIsValid(dataProvider)) {

            if (dataProvider != null && dataProvider.getExerciseDisplayNames() != null) {
                if (!dataProvider.getExerciseDisplayNames().contains(addedExerciseName.trim())) {
                    Exercise newExercise = new Exercise(todayWorkout, addedExerciseName, dataProvider.getExerciseCount());
                    DbFunctionObject insertExercise = new DbFunctionObject(newExercise, DbConstants.INSERT_EXERCISE);
                    new DbAsyncTask(Constants.TODAY).execute(insertExercise);
                } else {
                    Utils.displayLongSimpleSnackbar(fab, getString(R.string.add_exercise_error_already_exists));
                    hideProgressDialog();
                }
            } else {
                Utils.displayLongSimpleSnackbar(fab, getString(R.string.add_exercise_error));
                hideProgressDialog();
            }
        } else {
            Utils.exitActivityOnError(this);
        }
    }

    @Override
    public void onAddSetClick(AddSetDialog dialog) {
        showProgressDialog();
        parentExercise = dialog.getExercise();

        TodayDataProvider dataProvider =
                (TodayDataProvider)getDataProvider();

        if (Utils.dataProviderIsValid(dataProvider)) {
            if (dataProvider != null) {
                if (dialog.getRbWeightedSet().isChecked()) {
                    int reps = Utils.returnValidNumberFromEditText(dialog.getEtRepCount());
                    String exerciseCd = Constants.WEIGHTS;
                    int weight = Utils.returnValidNumberFromEditText(dialog.getEtWeightAmount());
                    String weightCd;
                    if (dialog.getRbLbs().isChecked()) {
                        weightCd = Constants.LBS;
                    } else {
                        weightCd = Constants.KGS;
                    }
                    addedSet = new Set(parentExercise, reps, weightCd, exerciseCd, weight, dataProvider.getSetCount(parentExercise));
                } else if (dialog.getRbTimedSet().isChecked()) {
                    int reps = Utils.returnValidNumberFromEditText(dialog.getEtRepCount());
                    Integer hours = Utils.returnValidNumberFromEditText(dialog.getEtHours());
                    Integer mins = Utils.returnValidNumberFromEditText(dialog.getEtMins());
                    Integer seconds = Utils.returnValidNumberFromEditText(dialog.getEtSeconds());
                    String exerciseCd = Constants.LOGGED_TIMED;
                    addedSet = new Set(parentExercise, reps, exerciseCd, hours, mins, seconds, dataProvider.getSetCount(parentExercise));
                } else if (dialog.getRbNonWeightedSet().isChecked()) {
                    int reps = Utils.returnValidNumberFromEditText(dialog.getEtRepCount());
                    String exerciseCd = Constants.NA;
                    addedSet = new Set(reps, exerciseCd);
                }
            } else {
                Utils.displayLongSimpleSnackbar(fab, getString(R.string.add_set_error));
                hideProgressDialog();
            }

            if (dataProvider != null && addedSet != null) {
                DbFunctionObject insertWorkoutSet = new DbFunctionObject(addedSet, DbConstants.INSERT_SET);
                new DbAsyncTask(Constants.TODAY).execute(insertWorkoutSet);
            } else {
                Utils.displayLongSimpleSnackbar(fab, getString(R.string.add_set_error));
                hideProgressDialog();
            }
        }  else {
                Utils.exitActivityOnError(this);
        }
    }

    @Override
    public void onEditSetClick(AddSetDialog dialog) {
        showProgressDialog();
        Set set = dialog.getSet();
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST_VIEW);

        if (dialog.getRbWeightedSet().isChecked()) {
            int reps = Utils.returnValidNumberFromEditText(dialog.getEtRepCount());
            String exerciseCd = Constants.WEIGHTS;
            int weight = Utils.returnValidNumberFromEditText(dialog.getEtWeightAmount());
            String weightCd;
            if (dialog.getRbLbs().isChecked()) {
                weightCd = Constants.LBS;
            } else {
                weightCd = Constants.KGS;
            }
            set.setReps(reps);
            set.setWeightTypeCode(weightCd);
            set.setExerciseTypeCode(exerciseCd);
            set.setWeight(weight);
        } else if (dialog.getRbTimedSet().isChecked()) {
            int reps = Utils.returnValidNumberFromEditText(dialog.getEtRepCount());
            Integer hours = Utils.returnValidNumberFromEditText(dialog.getEtHours());
            Integer mins = Utils.returnValidNumberFromEditText(dialog.getEtMins());
            Integer seconds = Utils.returnValidNumberFromEditText(dialog.getEtSeconds());
            String exerciseCd = Constants.LOGGED_TIMED;
            set.setReps(reps);
            set.setExerciseTypeCode(exerciseCd);
            set.setMinutes(mins);
            set.setHours(hours);
            set.setSeconds(seconds);
        } else if (dialog.getRbNonWeightedSet().isChecked()) {
            int reps = Utils.returnValidNumberFromEditText(dialog.getEtRepCount());
            String exerciseCd = Constants.NA;
            set.setReps(reps);
            set.setExerciseTypeCode(exerciseCd);
        }

        String errMsg = Utils.returnTodayEditSetErrorString(set);

        if (Utils.isEmptyString(errMsg)) {
            TodayDataProvider dataProvider =
                    (TodayDataProvider) getDataProvider();
            if (Utils.dataProviderIsValid(dataProvider)) {
                dataProvider.setChildItem(editedGroupPosition, editedChildPosition, set);
                ((TodayRvListViewFragment) fragment).notifyChildItemChanged(editedGroupPosition, editedChildPosition);
                ((TodayRvListViewFragment) fragment).notifyChildItemRestored(editedGroupPosition, editedChildPosition);
                DbFunctionObject editSetDfo =
                        new DbFunctionObject(set, DbConstants.UPDATE_SET);
                new DbAsyncTask(Constants.TODAY).execute(editSetDfo);
                dialog.dismiss();
                hideProgressDialog();
            } else {
                Utils.exitActivityOnError(this);
            }
        } else {
            Utils.displayLongToast(this, errMsg);
        }
        hideProgressDialog();
    }

    @Override
    public void onDeleteTodayWorkoutClick(AppBaseDialog dialog) {
        DbFunctionObject deleteWorkoutDfo = new DbFunctionObject(todayWorkout, DbConstants.DELETE_WORKOUT);
        new DbAsyncTask(Constants.TODAY).execute(deleteWorkoutDfo);
    }

    @Override
    public void onRenameClick(RenameDialog dialog) {
        Workout workout = dialog.getWorkout();
        Exercise exercise = dialog.getExercise();
        if (workout != null && exercise == null) {
            workout.setName(Utils.ensureValidString(dialog.getRenameText().getText().toString()));
            DbFunctionObject updateWorkout = new DbFunctionObject(workout, DbConstants.UPDATE_WORKOUT);
            new DbAsyncTask(Constants.TODAY).execute(updateWorkout);
        } else if (workout == null && exercise != null) {
            exercise.setName(Utils.ensureValidString(dialog.getRenameText().getText().toString()));
            DbFunctionObject updateExercise = new DbFunctionObject(exercise, DbConstants.UPDATE_EXERCISE);
            new DbAsyncTask(Constants.TODAY).execute(updateExercise);
        }
    }
}
