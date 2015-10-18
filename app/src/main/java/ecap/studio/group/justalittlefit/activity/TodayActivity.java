package ecap.studio.group.justalittlefit.activity;

import android.content.Context;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.advanced_recyclerview.rv_create_edit_view.AbstractDataProvider;
import ecap.studio.group.justalittlefit.advanced_recyclerview.rv_create_edit_view.DataProviderFragment;
import ecap.studio.group.justalittlefit.advanced_recyclerview.rv_create_edit_view.RecyclerListViewFragment;
import ecap.studio.group.justalittlefit.advanced_recyclerview.rv_today.AbstractExpandableDataProvider;
import ecap.studio.group.justalittlefit.advanced_recyclerview.rv_today.TodayDataProviderFragment;
import ecap.studio.group.justalittlefit.advanced_recyclerview.rv_today.TodayRvListViewFragment;
import ecap.studio.group.justalittlefit.bus.TodayBus;
import ecap.studio.group.justalittlefit.database.DbAsyncTask;
import ecap.studio.group.justalittlefit.database.DbConstants;
import ecap.studio.group.justalittlefit.database.DbFunctionObject;
import ecap.studio.group.justalittlefit.database.DbTaskResult;
import ecap.studio.group.justalittlefit.database.QueryExecutor;
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

        if (savedInstanceState == null) {
            if (todayWorkout != null) {
                DbFunctionObject getFullWorkout = new DbFunctionObject(todayWorkout, DbConstants.GET_FULL_WORKOUT);
                new DbAsyncTask(Constants.TODAY).execute(getFullWorkout);
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

    }

    public void onChildItemRemoved(Set set) {

    }

    public AbstractExpandableDataProvider getDataProvider() {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_DATA_PROVIDER);
        return ((TodayDataProviderFragment) fragment).getDataProvider();
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
    }

    public TodayRvListViewFragment getRecyclerViewFrag() {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST_VIEW);
        return ((TodayRvListViewFragment) fragment);
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
