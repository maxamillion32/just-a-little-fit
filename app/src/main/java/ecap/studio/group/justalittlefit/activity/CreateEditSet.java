package ecap.studio.group.justalittlefit.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.HashSet;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.bus.CreateEditSetBus;
import ecap.studio.group.justalittlefit.model.Exercise;
import ecap.studio.group.justalittlefit.model.Set;
import ecap.studio.group.justalittlefit.util.Constants;
import ecap.studio.group.justalittlefit.util.Utils;

public class CreateEditSet extends BaseNaviDrawerActivity {
    private static final String FRAGMENT_TAG_DATA_PROVIDER = "data provider";
    private static final String FRAGMENT_LIST_VIEW = "list view";
    FloatingActionButton fab;
    Exercise parentExercise;
    boolean busRegistered;
    boolean reorderTriggeredByAddSet;
    String addedSetName;
    private HashSet<Set> exercisesToDelete;
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
                // Create DFO
                // Execute DbAsyncTask using DFO
            } else {
                Utils.displayLongSimpleSnackbar(fab, getString(R.string.exercise_list_error));
            }
        }

        setupFloatingActionButton(this);
        setTitle(R.string.create_edit_set_title_string);
        exercisesToDelete = new HashSet<>();
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
}
