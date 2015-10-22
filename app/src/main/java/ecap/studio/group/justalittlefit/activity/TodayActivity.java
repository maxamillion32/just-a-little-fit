package ecap.studio.group.justalittlefit.activity;

import android.content.Context;
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
import com.j256.ormlite.dao.ForeignCollection;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.advanced_recyclerview.rv_create_edit_view.RecyclerListViewFragment;
import ecap.studio.group.justalittlefit.advanced_recyclerview.rv_today.AbstractExpandableDataProvider;
import ecap.studio.group.justalittlefit.advanced_recyclerview.rv_today.TodayDataProvider;
import ecap.studio.group.justalittlefit.advanced_recyclerview.rv_today.TodayDataProviderFragment;
import ecap.studio.group.justalittlefit.advanced_recyclerview.rv_today.TodayRvListViewFragment;
import ecap.studio.group.justalittlefit.bus.TodayBus;
import ecap.studio.group.justalittlefit.database.DbAsyncTask;
import ecap.studio.group.justalittlefit.database.DbConstants;
import ecap.studio.group.justalittlefit.database.DbFunctionObject;
import ecap.studio.group.justalittlefit.database.DbTaskResult;
import ecap.studio.group.justalittlefit.dialog.InformationDialog;
import ecap.studio.group.justalittlefit.model.Exercise;
import ecap.studio.group.justalittlefit.model.Set;
import ecap.studio.group.justalittlefit.model.Workout;
import ecap.studio.group.justalittlefit.util.Constants;
import ecap.studio.group.justalittlefit.util.Utils;

public class TodayActivity extends BaseNaviDrawerActivity {

    private final String LOG_TAG = getClass().getSimpleName();
    private static final String FRAGMENT_TAG_DATA_PROVIDER = "data provider";
    private static final String FRAGMENT_LIST_VIEW = "list view";

    FloatingActionButton fab;
    CoordinatorLayout clFab;
    boolean busRegistered;
    Workout todayWorkout;
    private HashSet<Exercise> exercisesToDelete;
    private HashSet<Set> setsToDelete;
    @InjectView(R.id.rlDefault)
    RelativeLayout rlDefault;

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
            if (todayWorkout != null) {
                DbFunctionObject getFullWorkoutDfo = new DbFunctionObject(todayWorkout, DbConstants.GET_FULL_WORKOUT);
                new DbAsyncTask(Constants.TODAY).execute(getFullWorkoutDfo);
            } else {
                Utils.displayLongSimpleSnackbar(fab, getString(R.string.workout_list_error));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
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
                // todo Add exercise and set dialog
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
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new TodayRvListViewFragment(), FRAGMENT_LIST_VIEW)
                    .commitAllowingStateLoss();
        } else if (event.getResult() instanceof String) {
            // onPause delete returned, reorder workouts before leaving activity
            reorderWorkouts();
        }
    }

    private void displayInfoDialog() {
        FragmentManager fm = getSupportFragmentManager();
        InformationDialog dialog = InformationDialog.newInstance(Constants.TODAY);
        dialog.show(fm, getString(R.string.infoDialogTagToday));
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

    void displayGeneralWorkoutListError() {
        String errorMsg = getString(R.string.workout_list_error);
        Log.e(LOG_TAG, errorMsg);
        Utils.displayLongSimpleSnackbar(this.findViewById(R.id.fab), errorMsg);
    }
}
