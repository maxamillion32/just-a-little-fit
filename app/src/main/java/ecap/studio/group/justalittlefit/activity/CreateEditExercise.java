package ecap.studio.group.justalittlefit.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.advanced_recyclerview.AbstractDataProvider;
import ecap.studio.group.justalittlefit.advanced_recyclerview.DataProviderFragment;
import ecap.studio.group.justalittlefit.advanced_recyclerview.RecyclerListViewFragment;
import ecap.studio.group.justalittlefit.bus.CreateEditExerciseBus;
import ecap.studio.group.justalittlefit.model.Exercise;
import ecap.studio.group.justalittlefit.util.Constants;

public class CreateEditExercise extends BaseNaviDrawerActivity {
    private static final String FRAGMENT_TAG_DATA_PROVIDER = "data provider";
    private static final String FRAGMENT_LIST_VIEW = "list view";
    FloatingActionButton fab;
    boolean afterInsert;
    @InjectView(R.id.rlDefault)
    RelativeLayout rlDefault;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CreateEditExerciseBus.getInstance().register(this);
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_create_edit_exercise, null, false);
        frameLayout.addView(contentView, 0);
        ButterKnife.inject(this, frameLayout);
        setTitle(R.string.create_edit_exercise_title_string);

        if (savedInstanceState == null) {
            List<Exercise> exercises = retrieveExercises();
            getSupportFragmentManager().beginTransaction()
                    .add(DataProviderFragment.newInstance(new ArrayList<>(exercises), Constants.EXERCISE),
                            FRAGMENT_TAG_DATA_PROVIDER)
                    .commitAllowingStateLoss();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new RecyclerListViewFragment(), FRAGMENT_LIST_VIEW)
                    .commitAllowingStateLoss();
        }
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
                // Call to display add Exercise dialog
            }
        });
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
                //Display ConfirmDeleteAllExercises
                break;
            case R.id.action_info:
                // DisplayInfoDialog for Exercise
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public AbstractDataProvider getDataProvider() {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_DATA_PROVIDER);
        return ((DataProviderFragment) fragment).getDataProvider();
    }

    public void onItemRemoved(int position, Object dataObject) {
        Exercise exercise = (Exercise) dataObject;
        //db logic to delete exercise
    }

    private List<Exercise> retrieveExercises() {
        List exercises = new ArrayList();
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(Constants.EXERCISES)) {
            exercises = extras.getParcelableArrayList(Constants.EXERCISES);
        } else {
            // Display snackbar
        }
        return exercises;
    }
}
