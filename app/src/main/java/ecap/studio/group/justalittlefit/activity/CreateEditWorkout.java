package ecap.studio.group.justalittlefit.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.advanced_recyclerview.AbstractDataProvider;
import ecap.studio.group.justalittlefit.advanced_recyclerview.DataProvider;
import ecap.studio.group.justalittlefit.advanced_recyclerview.DataProviderFragment;
import ecap.studio.group.justalittlefit.advanced_recyclerview.MyDraggableSwipeableItemAdapter;
import ecap.studio.group.justalittlefit.advanced_recyclerview.RecyclerListViewFragment;
import ecap.studio.group.justalittlefit.bus.CreateEditWorkoutBus;
import ecap.studio.group.justalittlefit.database.DbAsyncTask;
import ecap.studio.group.justalittlefit.database.DbConstants;
import ecap.studio.group.justalittlefit.database.DbFunctionObject;
import ecap.studio.group.justalittlefit.database.DbTaskResult;
import ecap.studio.group.justalittlefit.dialog.AddWorkoutDialog;
import ecap.studio.group.justalittlefit.dialog.AppBaseDialog;
import ecap.studio.group.justalittlefit.dialog.ConfirmDeleteWorkoutsDialog;
import ecap.studio.group.justalittlefit.dialog.InformationDialog;
import ecap.studio.group.justalittlefit.listener.AddWorkoutDialogListener;
import ecap.studio.group.justalittlefit.listener.ConfirmWorkoutsDeletionListener;
import ecap.studio.group.justalittlefit.model.Workout;
import ecap.studio.group.justalittlefit.util.Constants;
import ecap.studio.group.justalittlefit.util.Utils;

public class CreateEditWorkout extends BaseNaviDrawerActivity implements ConfirmWorkoutsDeletionListener,
        AddWorkoutDialogListener {
    private final String LOG_TAG = getClass().getSimpleName();
    private static final String FRAGMENT_TAG_DATA_PROVIDER = "data provider";
    private static final String FRAGMENT_LIST_VIEW = "list view";
    private HashSet<Workout> workoutsToDelete;
    FloatingActionButton fab;
    boolean busRegistered;
    boolean reorderTriggeredByAddWorkout;
    String addedWorkoutName;
    @InjectView(R.id.rlDefault)
    RelativeLayout rlDefault;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerBus();
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_create_edit_workout, null, false);
        frameLayout.addView(contentView, 0);
        ButterKnife.inject(this, frameLayout);

        if (savedInstanceState == null) {
            DbFunctionObject getAllWorkoutDfo = new DbFunctionObject(null, DbConstants.GET_ALL_UNASSIGNED_WORKOUTS);
            new DbAsyncTask(Constants.CREATE_EDIT_WORKOUT).execute(getAllWorkoutDfo);
        }

        setupFloatingActionButton(this);
        setTitle(R.string.create_edit_title_string);
        workoutsToDelete = new HashSet<>();
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
        return ((DataProviderFragment) fragment).getDataProvider();
    }

    public RecyclerListViewFragment getRecyclerViewFrag() {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST_VIEW);
        return ((RecyclerListViewFragment) fragment);
    }

    public void onItemClicked(int position) {
        AbstractDataProvider.Data data = getDataProvider().getItem(position);
        Workout workout = (Workout) data.getDataObject();
        Intent createEditExercise = new Intent(this, CreateEditExercise.class);
        createEditExercise.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.WORKOUT, workout);
        createEditExercise.putExtras(bundle);
        startActivity(createEditExercise);
    }

    public void onItemRemoved(Object dataObject) {
        determineDefaultStatus();
        Utils.displayLongActionSnackbar(fab, getString(R.string.workout_deleted),
                Constants.UNDO, undoWorkoutDelete(),
                getResources().getColor(R.color.app_blue_gray));
        Workout workoutToDelete = (Workout) dataObject;
        this.workoutsToDelete.add(workoutToDelete);
    }

    @Subscribe
    public void onAsyncTaskResult(DbTaskResult event) {
        hideProgressDialog();
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
        } else if (event.getResult() instanceof List) {
            List<Workout> workouts = (List<Workout>) event.getResult();
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
        } else if (event.getResult() instanceof Boolean) {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.addWorkout_success));
            DbFunctionObject getAllWorkoutDfo = new DbFunctionObject(null, DbConstants.GET_ALL_UNASSIGNED_WORKOUTS);
            new DbAsyncTask(Constants.CREATE_EDIT_WORKOUT).execute(getAllWorkoutDfo);
        } else {
            displayGeneralWorkoutListError();
        }
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
        if (workoutsToDelete != null && !workoutsToDelete.isEmpty()) {
            DbFunctionObject deleteWorkoutsDfo =
                    new DbFunctionObject(new ArrayList<>(workoutsToDelete),
                            DbConstants.DELETE_WORKOUTS);
            new DbAsyncTask(Constants.CREATE_EDIT_WORKOUT).execute(deleteWorkoutsDfo);
        } else {
           reorderWorkouts();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!busRegistered) {
            registerBus();
        }
    }

    private void reorderWorkouts() {
        DataProvider dataProvider =
                (DataProvider) getDataProvider();
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
                int position = getDataProvider().undoLastRemoval();
                final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST_VIEW);
                ((RecyclerListViewFragment) fragment).notifyItemInserted(position);
                Utils.displayLongSimpleSnackbar(fab,
                        getString(R.string.workout_removal_undone));
                AbstractDataProvider.Data data = getDataProvider().getItem(position);
                Workout workout = (Workout) data.getDataObject();
                workoutsToDelete.remove(workout);
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
        addedWorkoutName = dialog.getAddWorkoutText().getText().toString();
    }

    private void addWorkoutToUI() {
        DataProvider dataProvider =
                (DataProvider)getDataProvider();
        if (dataProvider != null && dataProvider.getCount() >= 0 && dataProvider.getDisplayNames() != null
                && addedWorkoutName != null) {
            if (!dataProvider.getDisplayNames().contains(addedWorkoutName.trim())) {
                Workout newWorkout = new Workout(addedWorkoutName, dataProvider.getCount());
                DbFunctionObject insertWorkout = new DbFunctionObject(newWorkout, DbConstants.INSERT_WORKOUT);
                new DbAsyncTask(Constants.CREATE_EDIT_WORKOUT).execute(insertWorkout);
            } else {
                Utils.displayLongSimpleSnackbar(fab, getString(R.string.add_workout_error_already_exists));
            }
        } else {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.add_workout_error));
        }
    }

    void determineDefaultStatus() {
        DataProvider dataProvider =
                (DataProvider)getDataProvider();
        if (dataProvider != null && dataProvider.getCount() == 0) {
            rlDefault.setVisibility(View.VISIBLE);
        } else {
            rlDefault.setVisibility(View.INVISIBLE);
        }
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
}
