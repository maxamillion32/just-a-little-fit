package ecap.studio.group.justalittlefit.activity;

import android.content.Context;
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
import java.util.List;

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
import ecap.studio.group.justalittlefit.dialog.ConfirmDeleteWorkoutDialog;
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
    FloatingActionButton fab;
    boolean afterInsert;
    @InjectView(R.id.rlDefault)
    RelativeLayout rlDefault;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CreateEditWorkoutBus.getInstance().register(this);
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
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST_VIEW);
        AbstractDataProvider.Data data = getDataProvider().getItem(position);

        if (data.isPinnedToSwipeLeft()) {
            // unpin if tapped the pinned item
            data.setPinnedToSwipeLeft(false);
            ((RecyclerListViewFragment) fragment).notifyItemChanged(position);
        }
    }

    public void onItemPinned(int position) {
        // do nothing, pinning not supported in this app
    }

    public void onItemRemoved(int position, String dataType, Object dataObject) {
        switch (dataType) {
            case Constants.WORKOUT:
                Workout workoutToDelete = (Workout) dataObject;
                DbFunctionObject deleteWorkout = new DbFunctionObject(workoutToDelete, DbConstants.DELETE_WORKOUT);
                new DbAsyncTask(Constants.CREATE_EDIT_WORKOUT).execute(deleteWorkout);
        }
    }

    @Subscribe
    public void onAsyncTaskResult(DbTaskResult event) {
        hideProgressDialog();
        if (event == null || event.getResult() == null) {
            displayGeneralWorkoutListError();
        } else if (event.getResult() instanceof List) {
            List<Workout> workouts = (List<Workout>) event.getResult();
            getSupportFragmentManager().beginTransaction()
                    .add(DataProviderFragment.newInstance(Constants.WORKOUT, new ArrayList<>(workouts)),
                            FRAGMENT_TAG_DATA_PROVIDER)
                    .commitAllowingStateLoss();
            if (afterInsert) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new RecyclerListViewFragment(), FRAGMENT_LIST_VIEW)
                        .commit();
                afterInsert = false;
            } else {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, new RecyclerListViewFragment(), FRAGMENT_LIST_VIEW)
                        .commitAllowingStateLoss();
            }

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
        } else if (event.getResult() instanceof Workout) {
            determineDefaultStatus();
            Utils.displayLongActionSnackbar(fab, getString(R.string.workout_deleted),
                    Constants.UNDO, undoWorkoutDelete((Workout) event.getResult()),
                    getResources().getColor(R.color.app_blue_gray));
        } else if (event.getResult() instanceof Boolean) {
            afterInsert = true;
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.addWorkout_success));
            DbFunctionObject getAllWorkoutDfo = new DbFunctionObject(null, DbConstants.GET_ALL_UNASSIGNED_WORKOUTS);
            new DbAsyncTask(Constants.CREATE_EDIT_WORKOUT).execute(getAllWorkoutDfo);
        } else if (event.getResult() instanceof String) {
            int position = getDataProvider().undoLastRemoval();
            final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST_VIEW);
            ((RecyclerListViewFragment) fragment).notifyItemInserted(position);
            Utils.displayLongSimpleSnackbar(fab,
                    getString(R.string.workout_removal_undone));
        } else {
            displayGeneralWorkoutListError();
        }
    }

    private void displayConfirmDeleteAllWorkoutDialog() {
        FragmentManager fm = getSupportFragmentManager();
        ConfirmDeleteWorkoutDialog dialog = new ConfirmDeleteWorkoutDialog();
        dialog.show(fm, getString(R.string.confirmDeleteWorkoutDialogTag));
    }

    private void displayInfoDialog() {
        FragmentManager fm = getSupportFragmentManager();
        InformationDialog dialog = new InformationDialog();
        dialog.show(fm, getString(R.string.infoDialogTag));
    }

    private void displayAddWorkoutDialog() {
        FragmentManager fm = getSupportFragmentManager();
        AddWorkoutDialog dialog = new AddWorkoutDialog();
        dialog.show(fm, getString(R.string.infoDialogTag));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create_edit_workouts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_delete_all:
                displayConfirmDeleteAllWorkoutDialog();
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
    protected void onDestroy() {
        CreateEditWorkoutBus.getInstance().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onDeleteAllWorkoutsClick(AppBaseDialog dialog) {
        showProgressDialog();
        DbFunctionObject deleteWorkoutsDfo =
                new DbFunctionObject(null, DbConstants.DELETE_WORKOUTS);
        new DbAsyncTask(Constants.CREATE_EDIT_WORKOUT).execute(deleteWorkoutsDfo);
    }

    private View.OnClickListener undoWorkoutDelete(final Workout workout) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DbFunctionObject insertWorkout = new DbFunctionObject(workout, DbConstants.REVERT_WORKOUT);
                new DbAsyncTask(Constants.CREATE_EDIT_WORKOUT).execute(insertWorkout);
            }
        };
    }

    void displayGeneralWorkoutListError() {
        Log.e(LOG_TAG, getString(R.string.workout_list_error));
        Utils.displayLongSimpleSnackbar(this.findViewById(R.id.fab), getString(R.string.workout_list_error));
    }

    @Override
    public void onAddWorkoutClick(AddWorkoutDialog dialog) {
        showProgressDialog();
        String newWorkoutName = dialog.getAddWorkoutText().getText().toString();
        DataProvider dataProvider =
                (DataProvider)getDataProvider();
        if (dataProvider != null && dataProvider.getCount() >= 0 && dataProvider.getDisplayNames() != null) {
            if (!dataProvider.getDisplayNames().contains(newWorkoutName.trim())) {
                Workout newWorkout = new Workout(newWorkoutName, dataProvider.getCount());
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
}
