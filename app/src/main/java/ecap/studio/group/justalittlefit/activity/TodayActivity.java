package ecap.studio.group.justalittlefit.activity;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.advanced_recyclerview.rv_today.AbstractExpandableDataProvider;
import ecap.studio.group.justalittlefit.advanced_recyclerview.rv_today.TodayDataProvider;
import ecap.studio.group.justalittlefit.advanced_recyclerview.rv_today.TodayDataProviderFragment;
import ecap.studio.group.justalittlefit.advanced_recyclerview.rv_today.TodayRvListViewFragment;
import ecap.studio.group.justalittlefit.bus.TodayBus;
import ecap.studio.group.justalittlefit.database.DbAsyncTask;
import ecap.studio.group.justalittlefit.database.DbConstants;
import ecap.studio.group.justalittlefit.database.DbFunctionObject;
import ecap.studio.group.justalittlefit.database.DbTaskResult;
import ecap.studio.group.justalittlefit.dialog.AddExerciseDialog;
import ecap.studio.group.justalittlefit.dialog.AddExerciseOrSetDialog;
import ecap.studio.group.justalittlefit.dialog.AddSetDialog;
import ecap.studio.group.justalittlefit.dialog.AppBaseDialog;
import ecap.studio.group.justalittlefit.dialog.ConfirmDeleteTodayWorkoutDialog;
import ecap.studio.group.justalittlefit.dialog.InformationDialog;
import ecap.studio.group.justalittlefit.listener.AddExerciseDialogListener;
import ecap.studio.group.justalittlefit.listener.AddSetDialogListener;
import ecap.studio.group.justalittlefit.listener.ConfirmDeleteTodayWorkoutListener;
import ecap.studio.group.justalittlefit.model.Exercise;
import ecap.studio.group.justalittlefit.model.Set;
import ecap.studio.group.justalittlefit.model.Workout;
import ecap.studio.group.justalittlefit.util.Constants;
import ecap.studio.group.justalittlefit.util.Utils;

public class TodayActivity extends BaseNaviDrawerActivity implements AddExerciseDialogListener, AddSetDialogListener, ConfirmDeleteTodayWorkoutListener {

    private final String LOG_TAG = getClass().getSimpleName();
    private static final String FRAGMENT_TAG_DATA_PROVIDER = "data provider";
    private static final String FRAGMENT_LIST_VIEW = "list view";

    FloatingActionButton fab;
    CoordinatorLayout clFab;
    boolean busRegistered;
    Workout todayWorkout;
    private HashSet<Exercise> exercisesToDelete;
    private HashSet<Set> setsToDelete;
    boolean reorderTriggeredByAdd;
    String addedExerciseName;
    Exercise parentExercise;
    Set addedSet;
    @InjectView(R.id.rlDefault)
    RelativeLayout rlDefault;
    boolean reorderTriggeredByEditSet;
    Integer editedGroupPosition;
    Integer editedChildPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_today, null, false);
        frameLayout.addView(contentView, 0);
        ButterKnife.inject(this, frameLayout);
        setTitle(R.string.today_title_string);
        setupFloatingActionButton(this);
        getWorkout();
        exercisesToDelete = new HashSet<>();
        setsToDelete = new HashSet<>();

        if (savedInstanceState == null) {
            displayTodayWorkout();
        }
    }

    void displayTodayWorkout() {
        if (todayWorkout != null) {
            DbFunctionObject getFullWorkoutDfo = new DbFunctionObject(todayWorkout, DbConstants.GET_FULL_WORKOUT);
            new DbAsyncTask(Constants.TODAY).execute(getFullWorkoutDfo);
        } else {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.workout_list_error));
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
        hideProgressDialog();
        if (event == null || event.getResult() == null) {
            displayGeneralWorkoutListError();
        } else if (event.getResult() instanceof Workout) {
            Workout workoutObj = (Workout) event.getResult();
            getSupportFragmentManager().beginTransaction()
                    .add(TodayDataProviderFragment.newInstance(new ArrayList<>(workoutObj.getExercises())), FRAGMENT_TAG_DATA_PROVIDER)
                    .commitAllowingStateLoss();
            if (reorderTriggeredByEditSet) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new TodayRvListViewFragment(), FRAGMENT_LIST_VIEW)
                        .commitAllowingStateLoss();
                Utils.displayLongSimpleSnackbar(fab, getString(R.string.editSet_success));
                reorderTriggeredByEditSet = false;
            } else {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, new TodayRvListViewFragment(), FRAGMENT_LIST_VIEW)
                        .commitAllowingStateLoss();
            }
        } else if (event.getResult() instanceof Map) {
            // Data order saved
            if (reorderTriggeredByAdd) {
                // Call method to add exercise to view
                addExerciseOrSetToUI();
            } else {
                // onPause result returned and data order saved, reset boolean trigger
                reorderTriggeredByAdd = false;
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
        }  else if (event.getResult() instanceof Double) {
            // Do nothing, UI already updated
        } else {
            displayGeneralWorkoutListError();
        }
    }

    private void displayInfoDialog() {
        FragmentManager fm = getSupportFragmentManager();
        InformationDialog dialog = InformationDialog.newInstance(Constants.TODAY);
        dialog.show(fm, getString(R.string.infoDialogTagToday));
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
        Utils.displayLongActionSnackbar(fab, getString(R.string.exercise_deleted),
                Constants.UNDO, undoDelete(),
                getResources().getColor(R.color.app_blue_gray));
        this.exercisesToDelete.add(exercise);
    }

    public void onChildItemRemoved(Set set) {
        Utils.displayLongActionSnackbar(fab, getString(R.string.set_deleted),
                Constants.UNDO, undoDelete(),
                getResources().getColor(R.color.app_blue_gray));
        this.setsToDelete.add(set);
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
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST_VIEW);
        final long result = getDataProvider().undoLastRemoval();

        if (result == RecyclerViewExpandableItemManager.NO_EXPANDABLE_POSITION) {
            return;
        }

        final int groupPosition = RecyclerViewExpandableItemManager.getPackedPositionGroup(result);
        final int childPosition = RecyclerViewExpandableItemManager.getPackedPositionChild(result);

        if (childPosition == RecyclerView.NO_POSITION) {
            // group item
            ((TodayRvListViewFragment) fragment).notifyGroupItemRestored(groupPosition);
            AbstractExpandableDataProvider.GroupData data = getDataProvider().getGroupItem(groupPosition);
            Exercise exercise = data.getExercise();
            exercisesToDelete.remove(exercise);
            Utils.displayLongSimpleSnackbar(fab,
                    getString(R.string.exercise_removal_undone));
            determineDefaultStatus();
        } else {
            // child item
            ((TodayRvListViewFragment) fragment).notifyChildItemRestored(groupPosition, childPosition);
            AbstractExpandableDataProvider.ChildData data = getDataProvider().getChildItem(groupPosition, childPosition);
            Set set = data.getSet();
            setsToDelete.remove(set);
            Utils.displayLongSimpleSnackbar(fab,
                    getString(R.string.set_removal_undone));
            determineDefaultStatus();
        }
    }

    public AbstractExpandableDataProvider getDataProvider() {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_DATA_PROVIDER);
        return ((TodayDataProviderFragment) fragment).getDataProvider();
    }

    void determineDefaultStatus() {
        TodayDataProvider dataProvider =
                (TodayDataProvider)getDataProvider();
        if (dataProvider != null && dataProvider.getGroupCount() == 0) {
            rlDefault.setVisibility(View.VISIBLE);
        } else {
            rlDefault.setVisibility(View.INVISIBLE);
        }
    }

    private void registerBus() {
        if (!busRegistered) {
            TodayBus.getInstance().register(this);
            busRegistered = true;
        }
    }

    private void unregisterBus() {
        if (busRegistered) {
            TodayBus.getInstance().unregister(this);
            busRegistered = false;
        }
    }

    public void onChildItemClicked(int groupPosition, int childPosition) {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST_VIEW);
        ((TodayRvListViewFragment) fragment).notifyChildItemRestored(groupPosition, childPosition);
        AbstractExpandableDataProvider.ChildData data = getDataProvider().getChildItem(groupPosition, childPosition);
        editedChildPosition = childPosition;
        editedGroupPosition = groupPosition;
        Set set = data.getSet();
        displayAddSetDialogUponEdit(set);
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
        if (!busRegistered) {
            registerBus();
        }
        MenuItem selectedItem = navigationView.getMenu().findItem(R.id.navi_today);
        selectedItem.setChecked(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterBus();
        if ((exercisesToDelete != null && !exercisesToDelete.isEmpty()) ||
                (setsToDelete != null && !setsToDelete.isEmpty())) {
            HashMap<String, Object> exerciseAndSetMap = new HashMap<>();
            exerciseAndSetMap.put(Constants.EXERCISES, exercisesToDelete);
            exerciseAndSetMap.put(Constants.SETS_NORM_CASE, setsToDelete);

            DbFunctionObject deleteExercisesAndSetsDfo =
                    new DbFunctionObject(exerciseAndSetMap,
                            DbConstants.DELETE_EXERCISES_AND_SETS);
            new DbAsyncTask(Constants.TODAY).execute(deleteExercisesAndSetsDfo);
        } else {
            reorderWorkouts();
        }
    }

    public TodayRvListViewFragment getRecyclerViewFrag() {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST_VIEW);
        return ((TodayRvListViewFragment) fragment);
    }

    private void reorderWorkouts() {
        TodayDataProvider dataProvider =
                (TodayDataProvider) getDataProvider();
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
    }

    void showProgressDialog() {
        if (isProgressDialogReady()) {
            getRecyclerViewFrag().getProgressDialog().setVisibility(View.VISIBLE);
        }
    }

    void hideProgressDialog() {
        if (isProgressDialogReady()) {
            getRecyclerViewFrag().getProgressDialog().setVisibility(View.INVISIBLE);
        }
    }

    private void displayAddExerciseOrSetDialog() {
        FragmentManager fm = getSupportFragmentManager();
        AddExerciseOrSetDialog dialog = AddExerciseOrSetDialog.newInstance(todayWorkout,
                new ArrayList<>(exercisesToDelete));
        dialog.show(fm, getString(R.string.addExerciseOrSetDialogTag));
    }

    void displayGeneralWorkoutListError() {
        String errorMsg = getString(R.string.workout_list_error);
        Log.e(LOG_TAG, errorMsg);
        Utils.displayLongSimpleSnackbar(this.findViewById(R.id.fab), errorMsg);
    }

    @Override
    public void onAddExerciseClick(AddExerciseDialog dialog) {
        showProgressDialog();
        reorderTriggeredByAdd = true;
        reorderWorkouts();
        addedExerciseName = dialog.getAddExerciseText().getText().toString();
    }

    @Override
    public void onAddSetClick(AddSetDialog dialog) {
        showProgressDialog();
        reorderTriggeredByAdd = true;
        parentExercise = dialog.getExercise();
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
            addedSet = new Set(reps, weightCd, exerciseCd, weight);
        } else {
            int reps = Utils.returnValidNumberFromEditText(dialog.getEtTimedRepCount());
            Integer hours = Utils.returnValidNumberFromEditText(dialog.getEtHours());
            Integer mins = Utils.returnValidNumberFromEditText(dialog.getEtMins());
            Integer seconds = Utils.returnValidNumberFromEditText(dialog.getEtSeconds());
            String exerciseCd = Constants.LOGGED_TIMED;
            addedSet = new Set(reps, exerciseCd, hours, mins, seconds);
        }
        reorderWorkouts();
    }

    @Override
    public void onEditSetClick(AddSetDialog dialog) {
        showProgressDialog();
        reorderTriggeredByEditSet = true;
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
        } else {
            int reps = Utils.returnValidNumberFromEditText(dialog.getEtTimedRepCount());
            Integer hours = Utils.returnValidNumberFromEditText(dialog.getEtHours());
            Integer mins = Utils.returnValidNumberFromEditText(dialog.getEtMins());
            Integer seconds = Utils.returnValidNumberFromEditText(dialog.getEtSeconds());
            String exerciseCd = Constants.LOGGED_TIMED;
            set.setReps(reps);
            set.setExerciseTypeCode(exerciseCd);
            set.setMinutes(mins);
            set.setHours(hours);
            set.setSeconds(seconds);
        }

        dialog.dismiss();

        getDataProvider().setChildItem(editedGroupPosition, editedChildPosition, set);
        ((TodayRvListViewFragment) fragment).notifyChildItemChanged(editedGroupPosition, editedChildPosition);
        ((TodayRvListViewFragment) fragment).notifyChildItemRestored(editedGroupPosition, editedChildPosition);

        DbFunctionObject editSetDfo =
                new DbFunctionObject(set, DbConstants.UPDATE_SET);
        new DbAsyncTask(Constants.TODAY).execute(editSetDfo);
    }

    private void addExerciseOrSetToUI() {
        TodayDataProvider dataProvider =
                (TodayDataProvider)getDataProvider();

        if (dataProvider != null && dataProvider.getCount() >= 0 && dataProvider.getExerciseDisplayNames() != null
                && addedExerciseName != null) {
            if (!dataProvider.getExerciseDisplayNames().contains(addedExerciseName.trim())) {
                Exercise newExercise = new Exercise(todayWorkout, addedExerciseName, dataProvider.getCount());
                DbFunctionObject insertExercise = new DbFunctionObject(newExercise, DbConstants.INSERT_EXERCISE);
                new DbAsyncTask(Constants.TODAY).execute(insertExercise);
            } else {
                Utils.displayLongSimpleSnackbar(fab, getString(R.string.add_exercise_error_already_exists));
            }
        } else {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.add_exercise_error));
        }

        if (dataProvider != null && dataProvider.getCount() >= 0 && addedSet != null) {
            addedSet.setExercise(parentExercise);
            addedSet.setOrderNumber(dataProvider.getCount());
            DbFunctionObject insertWorkoutSet = new DbFunctionObject(addedSet, DbConstants.INSERT_SET);
            new DbAsyncTask(Constants.TODAY).execute(insertWorkoutSet);
        } else {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.add_set_error));
        }
    }

    @Override
    public void onDeleteTodayWorkoutClick(AppBaseDialog dialog) {
        DbFunctionObject deleteWorkoutDfo = new DbFunctionObject(todayWorkout, DbConstants.DELETE_WORKOUT);
        new DbAsyncTask(Constants.TODAY).execute(deleteWorkoutDfo);
    }
}
