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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.j256.ormlite.dao.CloseableIterator;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import group.g203.justalittlefit.R;
import group.g203.justalittlefit.advanced_recyclerview.rv_create_edit_view.AbstractDataProvider;
import group.g203.justalittlefit.advanced_recyclerview.rv_create_edit_view.DataProvider;
import group.g203.justalittlefit.advanced_recyclerview.rv_create_edit_view.DataProviderFragment;
import group.g203.justalittlefit.advanced_recyclerview.rv_create_edit_view.MyDraggableSwipeableItemAdapter;
import group.g203.justalittlefit.advanced_recyclerview.rv_create_edit_view.RecyclerListViewFragment;
import group.g203.justalittlefit.bus.CreateEditWorkoutBus;
import group.g203.justalittlefit.database.DbAsyncTask;
import group.g203.justalittlefit.database.DbConstants;
import group.g203.justalittlefit.database.DbFunctionObject;
import group.g203.justalittlefit.database.DbTaskResult;
import group.g203.justalittlefit.dialog.AddWorkoutDialog;
import group.g203.justalittlefit.dialog.AppBaseDialog;
import group.g203.justalittlefit.dialog.ConfirmDeleteWorkoutsDialog;
import group.g203.justalittlefit.dialog.InformationDialog;
import group.g203.justalittlefit.dialog.RenameDialog;
import group.g203.justalittlefit.listener.AddWorkoutDialogListener;
import group.g203.justalittlefit.listener.ConfirmWorkoutsDeletionListener;
import group.g203.justalittlefit.listener.RenameDialogListener;
import group.g203.justalittlefit.model.Workout;
import group.g203.justalittlefit.util.Constants;
import group.g203.justalittlefit.util.Utils;

/**
 * Creates a {@link group.g203.justalittlefit.model.Workout} in the app.
 */
public class CreateEditWorkout extends BaseNaviDrawerActivity implements ConfirmWorkoutsDeletionListener,
        AddWorkoutDialogListener, RenameDialogListener {
    private final String LOG_TAG = getClass().getSimpleName();
    private static final String FRAGMENT_TAG_DATA_PROVIDER = "data provider";
    private static final String FRAGMENT_LIST_VIEW = "list view";
    FloatingActionButton fab;
    CoordinatorLayout clFab;
    boolean busRegistered;
    boolean reorderTriggeredByAddWorkout;
    CloseableIterator<Workout> ciWorkouts;
    String addedWorkoutName;
    @InjectView(R.id.rlDefault)
    RelativeLayout rlDefault;
    HashSet<Workout> queuedWorkoutsToDelete;
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

    void displayWorkoutList() {
        showProgressDialog();
        DbFunctionObject getAllWorkoutDfo = new DbFunctionObject(null, DbConstants.GET_ALL_UNASSIGNED_WORKOUTS);
        new DbAsyncTask(Constants.CREATE_EDIT_WORKOUT).execute(getAllWorkoutDfo);
    }

    @Override
    void setupDrawerContent(NavigationView navigationView) {
        // Check menu item of currently displayed activity
        MenuItem selectedItem = navigationView.getMenu().findItem(R.id.navi_createEdit);
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
                displayAddWorkoutDialog();
            }
        });
    }

    public AbstractDataProvider getDataProvider() {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_DATA_PROVIDER);
        if (fragment != null) {
            return ((DataProviderFragment) fragment).getDataProvider();
        } else {
            return null;
        }
    }

    public void onItemClicked(int position) {
        DataProvider dataProvider =
                (DataProvider) getDataProvider();
        Utils.dataProviderCheck(dataProvider, this);
        AbstractDataProvider.Data data = dataProvider.getItem(position);
        Workout workout = (Workout) data.getDataObject();
        Intent createEditExercise = new Intent(this, CreateEditExercise.class);
        createEditExercise.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.WORKOUT, workout);
        createEditExercise.putExtras(bundle);
        startActivity(createEditExercise);
    }

    public void onItemLongClicked(int position) {
        DataProvider dataProvider =
                (DataProvider) getDataProvider();
        Utils.dataProviderCheck(dataProvider, this);
        AbstractDataProvider.Data data = dataProvider.getItem(position);
        Workout workout = (Workout) data.getDataObject();
        FragmentManager fm = getSupportFragmentManager();
        RenameDialog dialog = RenameDialog.newInstance(workout);
        dialog.show(fm, getString(R.string.renameDialog_Tag));
    }

    public void onItemRemoved(Object dataObject) {
        determineDefaultStatus();
        Workout workoutToDelete = (Workout) dataObject;

        queuedWorkoutsToDelete.add(workoutToDelete);
        Utils.displayLongActionSnackbar(fab, getString(R.string.workout_deleted),
                Constants.UNDO, undoWorkoutDelete(),
                getResources().getColor(R.color.app_blue_gray));
    }

    @Subscribe
    public void onAsyncTaskResult(DbTaskResult event) {
        if (event == null || event.getResult() == null) {
            displayGeneralWorkoutListError();
        } else if (event.getResult() instanceof Set) {
            // Data order saved
            if (reorderTriggeredByAddWorkout) {
                // Call method to add workout to view
                addWorkoutToUI();
            } else {
                // onPause result returned and data order saved, reset boolean trigger
                reorderTriggeredByAddWorkout = false;
            }
            dropQueuedWorkoutsToDelete();
        } else if (event.getResult() instanceof HashMap) {
            HashMap<String, Object> map = (HashMap<String, Object>) event.getResult();
            ciWorkouts = (CloseableIterator<Workout>) map.get(Constants.ITERATOR);
            List<Workout> workouts = (List<Workout>) map.get(Constants.LIST);
            getSupportFragmentManager().beginTransaction()
                    .add(DataProviderFragment.newInstance(Constants.WORKOUT, new ArrayList<>(workouts)),
                            FRAGMENT_TAG_DATA_PROVIDER)
                    .commitAllowingStateLoss();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new RecyclerListViewFragment(), FRAGMENT_LIST_VIEW)
                    .commitAllowingStateLoss();
            if (workouts.size() == 0) {
                rlDefault.setVisibility(View.VISIBLE);
            } else {
                rlDefault.setVisibility(View.INVISIBLE);
            }
        } else if (event.getResult() instanceof Integer) {
            final Fragment recyclerFrag = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST_VIEW);
            MyDraggableSwipeableItemAdapter adapter =
                    ((RecyclerListViewFragment) recyclerFrag).getAdapter();
            DataProvider dataProvider =
                    (DataProvider) getDataProvider();
            Utils.dataProviderCheck(dataProvider, this);
            if (adapter != null && dataProvider != null && dataProvider.getCount() >= 0) {
                adapter.removeAllItems(dataProvider.getCount() - 1);
                Utils.displayLongSimpleSnackbar(fab, getString(R.string.confirmDeleteWorkoutDialog_success));
                rlDefault.setVisibility(View.VISIBLE);
            } else {
                Utils.displayLongSimpleSnackbar(fab, getString(R.string.deletion_workout_error));
            }
        } else if (event.getResult() instanceof String) {
            // onPause delete returned, reorder workouts before leaving activity
            reorderWorkouts();
        } else if (event.getResult() instanceof Workout) {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.renameDialog_WorkoutSuccess));
            displayWorkoutList();
        } else if (event.getResult() instanceof Boolean) {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.addWorkout_success));
            displayWorkoutList();
        } else {
            displayGeneralWorkoutListError();
        }
        hideProgressDialog();
    }

    private void displayConfirmDeleteAllWorkoutsDialog() {
        FragmentManager fm = getSupportFragmentManager();
        ConfirmDeleteWorkoutsDialog dialog = new ConfirmDeleteWorkoutsDialog();
        dialog.show(fm, getString(R.string.confirmDeleteWorkoutsDialogTag));
    }

    private void displayInfoDialog() {
        FragmentManager fm = getSupportFragmentManager();
        InformationDialog dialog = InformationDialog.newInstance(Constants.WORKOUTS_NORM_CASE);
        dialog.show(fm, getString(R.string.infoDialogTag));
    }

    private void displayAddWorkoutDialog() {
        FragmentManager fm = getSupportFragmentManager();
        AddWorkoutDialog dialog = new AddWorkoutDialog();
        dialog.show(fm, getString(R.string.addWorkoutDialogTag));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_delete_all:
                displayConfirmDeleteAllWorkoutsDialog();
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
    protected void onPause() {
        super.onPause();
        unregisterBus();
        reorderWorkouts();
        hideProgressDialog();
        dropQueuedWorkoutsToDelete();
    }

    @Override
    public void onResume() {
        super.onResume();
        showProgressDialog();
        registerBus();
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_create_edit_workout, null, false);
        frameLayout.addView(contentView, 0);
        queuedWorkoutsToDelete = new HashSet<>();
        ButterKnife.inject(this, frameLayout);

        if (savedBundle == null) {
            displayWorkoutList();
        }

        setupFloatingActionButton(this);
        setTitle(R.string.create_edit_title_string);
        MenuItem selectedItem = navigationView.getMenu().findItem(R.id.navi_createEdit);
        selectedItem.setChecked(true);
    }

    private void reorderWorkouts() {
        DataProvider dataProvider =
                (DataProvider) getDataProvider();
        Utils.dataProviderCheck(dataProvider, this);
        List<Workout> workoutsToSave = (List<Workout>) (Object) dataProvider.getDataObjects();
        for (int i = 0; i < workoutsToSave.size(); i++) {
            workoutsToSave.get(i).setOrderNumber(i);
        }
        DbFunctionObject saveWorkoutsDfo =
                new DbFunctionObject(workoutsToSave, DbConstants.UPDATE_WORKOUTS);
        new DbAsyncTask(Constants.CREATE_EDIT_WORKOUT).execute(saveWorkoutsDfo);
    }

    @Override
    protected void onDestroy() {
        unregisterBus();
        super.onDestroy();
    }

    @Override
    public void onDeleteAllWorkoutsClick(AppBaseDialog dialog) {
        if (rlDefault.getVisibility() == View.VISIBLE) {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.no_workouts_to_delete));
        } else {
            showProgressDialog();
            DbFunctionObject deleteWorkoutsDfo =
                    new DbFunctionObject(null, DbConstants.DELETE_ALL_WORKOUTS);
            new DbAsyncTask(Constants.CREATE_EDIT_WORKOUT).execute(deleteWorkoutsDfo);
        }
    }

    private View.OnClickListener undoWorkoutDelete() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataProvider dataProvider =
                        (DataProvider) getDataProvider();
                Utils.dataProviderCheck(dataProvider, getParent());
                int position = dataProvider.undoLastRemoval();
                final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST_VIEW);
                ((RecyclerListViewFragment) fragment).notifyItemInserted(position);
                Utils.displayLongSimpleSnackbar(fab,
                        getString(R.string.workout_removal_undone));
                AbstractDataProvider.Data data = dataProvider.getItem(position);
                Workout workout = (Workout) data.getDataObject();
                queuedWorkoutsToDelete.remove(workout);
                determineDefaultStatus();
            }
        };
    }

    void displayGeneralWorkoutListError() {
        String errorMsg = getString(R.string.workout_list_error);
        Log.e(LOG_TAG, errorMsg);
        Utils.displayLongSimpleSnackbar(this.findViewById(R.id.fab), errorMsg);
    }

    @Override
    public void onAddWorkoutClick(AddWorkoutDialog dialog) {
        showProgressDialog();
        reorderTriggeredByAddWorkout = true;
        reorderWorkouts();
        addedWorkoutName = Utils.ensureValidString(dialog.getAddWorkoutText().getText().toString());
    }

    private void addWorkoutToUI() {
        DataProvider dataProvider =
                (DataProvider) getDataProvider();
        Utils.dataProviderCheck(dataProvider, this);
        if (dataProvider != null && dataProvider.getCount() >= 0 && dataProvider.getDisplayNames() != null
                && addedWorkoutName != null) {
            if (!dataProvider.getDisplayNames().contains(addedWorkoutName.trim())) {
                Workout newWorkout = new Workout(addedWorkoutName, dataProvider.getCount());
                DbFunctionObject insertWorkout = new DbFunctionObject(newWorkout, DbConstants.INSERT_WORKOUT);
                new DbAsyncTask(Constants.CREATE_EDIT_WORKOUT).execute(insertWorkout);
            } else {
                Utils.displayLongSimpleSnackbar(fab, getString(R.string.add_workout_error_already_exists));
                hideProgressDialog();
            }
        } else {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.add_workout_error));
            hideProgressDialog();
        }
    }

    void dropQueuedWorkoutsToDelete() {
        Integer removeResult = Utils.removeWorkouts(ciWorkouts, queuedWorkoutsToDelete);
        if (removeResult == Constants.INT_NEG_ONE) {
            Log.e(LOG_TAG, getString(R.string.exercise_modify_error));
        } else {
            // do nothing, successful deletions
        }
    }

    void determineDefaultStatus() {
        DataProvider dataProvider =
                (DataProvider) getDataProvider();
        Utils.dataProviderCheck(dataProvider, this);
        if (dataProvider != null && dataProvider.getCount() == 0) {
            rlDefault.setVisibility(View.VISIBLE);
        } else {
            rlDefault.setVisibility(View.INVISIBLE);
        }
    }

    private void registerBus() {
        if (!busRegistered) {
            CreateEditWorkoutBus.getInstance().register(this);
            busRegistered = true;
        }
    }

    private void unregisterBus() {
        if (busRegistered) {
            CreateEditWorkoutBus.getInstance().unregister(this);
            busRegistered = false;
        }
    }

    @Override
    public void onRenameClick(RenameDialog dialog) {
        Workout workout = dialog.getWorkout();
        workout.setName(Utils.ensureValidString(dialog.getRenameText().getText().toString()));
        DbFunctionObject updateWorkout = new DbFunctionObject(workout, DbConstants.UPDATE_WORKOUT);
        new DbAsyncTask(Constants.CREATE_EDIT_WORKOUT).execute(updateWorkout);
    }
}
