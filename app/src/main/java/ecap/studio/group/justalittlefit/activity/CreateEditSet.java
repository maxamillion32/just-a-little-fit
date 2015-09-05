package ecap.studio.group.justalittlefit.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.squareup.otto.Subscribe;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.advanced_recyclerview.AbstractDataProvider;
import ecap.studio.group.justalittlefit.advanced_recyclerview.DataProvider;
import ecap.studio.group.justalittlefit.advanced_recyclerview.DataProviderFragment;
import ecap.studio.group.justalittlefit.advanced_recyclerview.RecyclerListViewFragment;
import ecap.studio.group.justalittlefit.bus.CreateEditSetBus;
import ecap.studio.group.justalittlefit.database.DbAsyncTask;
import ecap.studio.group.justalittlefit.database.DbConstants;
import ecap.studio.group.justalittlefit.database.DbFunctionObject;
import ecap.studio.group.justalittlefit.database.DbTaskResult;
import ecap.studio.group.justalittlefit.model.Exercise;
import ecap.studio.group.justalittlefit.model.Set;
import ecap.studio.group.justalittlefit.util.Constants;
import ecap.studio.group.justalittlefit.util.Utils;

public class CreateEditSet extends BaseNaviDrawerActivity {
    private final String LOG_TAG = getClass().getSimpleName();
    private static final String FRAGMENT_TAG_DATA_PROVIDER = "data provider";
    private static final String FRAGMENT_LIST_VIEW = "list view";
    FloatingActionButton fab;
    Exercise parentExercise;
    boolean busRegistered;
    boolean reorderTriggeredByAddSet;
    String addedSetName;
    private HashSet<Set> setsToDelete;
    @InjectView(R.id.rlDefault)
    RelativeLayout rlDefault;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerBus();
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_create_edit_set, null, false);
        frameLayout.addView(contentView, 0);
        ButterKnife.inject(this, frameLayout);

        if (savedInstanceState == null) {
            parentExercise = getParentExercise();
            if (parentExercise != null) {
                DbFunctionObject getSetsByExercise = new DbFunctionObject(parentExercise, DbConstants.GET_SETS_BY_EXERCISE);
                new DbAsyncTask(Constants.CREATE_EDIT_SET).execute(getSetsByExercise);
            } else {
                Utils.displayLongSimpleSnackbar(fab, getString(R.string.exercise_list_error));
            }
        }

        setupFloatingActionButton(this);
        setTitle(R.string.create_edit_set_title_string);
        setsToDelete = new HashSet<>();
    }

    private void setupFloatingActionButton(final BaseNaviDrawerActivity activity) {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_plus_white);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Display add set dialog method
            }
        });
    }

    @Subscribe
    public void onAsyncTaskResult(DbTaskResult event) {
        hideProgressDialog();
        if (event.getResult() instanceof List) {
            List<Set> sets = (List<Set>) event.getResult();
            LinkedHashSet<Set> setsObj = new LinkedHashSet<>(sets);
            getSupportFragmentManager().beginTransaction()
                    .add(DataProviderFragment.newInstance(setsObj, Constants.SET),
                            FRAGMENT_TAG_DATA_PROVIDER)
                    .commitAllowingStateLoss();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new RecyclerListViewFragment(), FRAGMENT_LIST_VIEW)
                    .commitAllowingStateLoss();
            if (sets.size() == 0) {
                rlDefault.setVisibility(View.VISIBLE);
            } else {
                rlDefault.setVisibility(View.INVISIBLE);
            }
        } else if (event.getResult() instanceof Set) {
            // Data order saved
            if (reorderTriggeredByAddSet) {
                // Call method to add exercise to view
                addSetToUI();
            } else {
                // onPause result returned and data order saved, reset boolean trigger
                reorderTriggeredByAddSet = false;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_delete_all:
                // Display delete all set dialog
                break;
            case R.id.action_info:
                // Display info dialog
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private Exercise getParentExercise() {
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(Constants.EXERCISE)) {
            return extras.getParcelable(Constants.EXERCISE);
        } else {
            return null;
        }
    }

    public AbstractDataProvider getDataProvider() {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_DATA_PROVIDER);
        return ((DataProviderFragment) fragment).getDataProvider();
    }

    private void registerBus() {
        if (!busRegistered) {
            CreateEditSetBus.getInstance().register(this);
            busRegistered = true;
        }
    }

    private void unregisterBus() {
        if (busRegistered) {
            CreateEditSetBus.getInstance().unregister(this);
            busRegistered = false;
        }
    }

    public RecyclerListViewFragment getRecyclerViewFrag() {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST_VIEW);
        return ((RecyclerListViewFragment) fragment);
    }

    private void reorderExercises() {
        DataProvider dataProvider =
                (DataProvider) getDataProvider();
        List<Set> setsToReorder = (List<Set>) (Object) dataProvider.getDataObjects();
        for (int i = 0; i < setsToReorder.size(); i++) {
            setsToReorder.get(i).setOrderNumber(i);
        }
        DbFunctionObject reorderSetsDfo =
                new DbFunctionObject(setsToReorder, DbConstants.UPDATE_SETS);
        new DbAsyncTask(Constants.CREATE_EDIT_SET).execute(reorderSetsDfo);
    }

    private void addSetToUI() {
      /*  DataProvider dataProvider =
                (DataProvider)getDataProvider();
        if (dataProvider != null && dataProvider.getCount() >= 0 && dataProvider.getDisplayNames() != null
                && addedExerciseName != null) {
            if (!dataProvider.getDisplayNames().contains(addedExerciseName.trim())) {
                Exercise newExercise = new Exercise(parentWorkout, addedExerciseName, dataProvider.getCount());
                DbFunctionObject insertExercise = new DbFunctionObject(newExercise, DbConstants.INSERT_EXERCISE);
                new DbAsyncTask(Constants.CREATE_EDIT_EXERCISE).execute(insertExercise);
            } else {
                Utils.displayLongSimpleSnackbar(fab, getString(R.string.add_exercise_error_already_exists));
            }
        } else {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.add_exercise_error));
        }*/
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
        if (setsToDelete != null && !setsToDelete.isEmpty()) {
            /*DbFunctionObject deleteWorkoutsDfo =
                    new DbFunctionObject(new ArrayList<>(setsToDelete),
                            DbConstants.DELETE_EXERCISES);
            new DbAsyncTask(Constants.CREATE_EDIT_EXERCISE).execute(deleteWorkoutsDfo);*/
        } else {
            reorderExercises();
        }
    }
}
