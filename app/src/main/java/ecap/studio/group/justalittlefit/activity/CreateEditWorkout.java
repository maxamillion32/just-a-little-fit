package ecap.studio.group.justalittlefit.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.advanced_recyclerview.AbstractDataProvider;
import ecap.studio.group.justalittlefit.advanced_recyclerview.DataProviderFragment;
import ecap.studio.group.justalittlefit.advanced_recyclerview.RecyclerListViewFragment;
import ecap.studio.group.justalittlefit.bus.CreateEditWorkoutBus;
import ecap.studio.group.justalittlefit.database.DbAsyncTask;
import ecap.studio.group.justalittlefit.database.DbConstants;
import ecap.studio.group.justalittlefit.database.DbFunctionObject;
import ecap.studio.group.justalittlefit.database.DbTaskResult;
import ecap.studio.group.justalittlefit.model.Workout;
import ecap.studio.group.justalittlefit.util.Constants;

public class CreateEditWorkout extends BaseNaviDrawerActivity {
    private final String LOG_TAG = getClass().getSimpleName();
    private static final String FRAGMENT_TAG_DATA_PROVIDER = "data provider";
    private static final String FRAGMENT_LIST_VIEW = "list view";

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
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_plus_white);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //todo
            }
        });
    }


    public AbstractDataProvider getDataProvider() {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_DATA_PROVIDER);
        return ((DataProviderFragment) fragment).getDataProvider();
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

    public void onItemRemoved(int position) {
        //todo
/*        SnackbarManager.show(
                Snackbar.with(getApplicationContext())
                        .text(R.string.snack_bar_text_item_removed)
                        .actionLabel(R.string.snack_bar_action_undo)
                        .actionListener(new ActionClickListener() {
                            @Override
                            public void onActionClicked(Snackbar snackbar) {
                                onItemUndoActionClicked();
                            }
                        })
                        .actionColorResource(R.color.snackbar_action_color_done)
                        .duration(5000)
                        .type(SnackbarType.SINGLE_LINE)
                        .swipeToDismiss(false)
                , this);*/
    }

    @Subscribe
    public void onAsyncTaskResult(DbTaskResult event) {
        if (event == null || event.getResult() == null) {
            Log.e(LOG_TAG, getString(R.string.workout_list_error));
        } else {
            List<Workout> workouts = (List<Workout>) event.getResult();
            getSupportFragmentManager().beginTransaction()
                    .add(DataProviderFragment.newInstance(Constants.WORKOUT, new ArrayList<>(workouts)),
                            FRAGMENT_TAG_DATA_PROVIDER)
                    .commit();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new RecyclerListViewFragment(), FRAGMENT_LIST_VIEW)
                    .commit();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create_edit_workouts, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        CreateEditWorkoutBus.getInstance().unregister(this);
        super.onDestroy();
    }
}
