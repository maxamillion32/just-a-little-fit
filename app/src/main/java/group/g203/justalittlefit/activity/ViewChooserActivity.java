package group.g203.justalittlefit.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import group.g203.justalittlefit.R;
import group.g203.justalittlefit.adapter.WorkoutRvNameAdapter;
import group.g203.justalittlefit.dialog.InformationDialog;
import group.g203.justalittlefit.model.Workout;
import group.g203.justalittlefit.util.Constants;
import group.g203.justalittlefit.util.Utils;

/**
 * The activity that displays when there are multiple
 * {@link group.g203.justalittlefit.model.Workout} objects assigned.
 */
public class ViewChooserActivity extends BaseNaviDrawerActivity {

    private static final String DATE_FORMAT = "MMMM d, yyyy";

    @Bind(R.id.tvDate)
    TextView tvDate;
    @Bind(R.id.rvWorkoutName)
    RecyclerView rvWorkoutName;
    WorkoutRvNameAdapter workoutRvNameAdapter;
    List<Workout> workouts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
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

    @Override
    protected void onPause() {
        super.onPause();
        hideProgressDialog();
        frameLayout.removeAllViews();
    }

    private List<Workout> getWorkouts() {
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(Constants.WORKOUTS)) {
            return extras.getParcelableArrayList(Constants.WORKOUTS);
        } else {
            return null;
        }
    }

    private void setDisplayDate() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT);
        if (workouts.get(0) != null && workouts.get(0).getWorkoutDate() != null) {
            tvDate.setText(dateTimeFormatter.print(workouts.get(0).getWorkoutDate()));
        }
    }

    private void displayInfoDialog() {
        FragmentManager fm = getSupportFragmentManager();
        InformationDialog dialog = InformationDialog.newInstance(Constants.CHOOSER);
        dialog.show(fm, getString(R.string.infoDialogTagViewChooser));
    }

    private void setupRecyclerView() {
        rvWorkoutName.setLayoutManager(new LinearLayoutManager(this));
        rvWorkoutName.setItemAnimator(new DefaultItemAnimator());

        workoutRvNameAdapter = new WorkoutRvNameAdapter(
                new ArrayList<>(workouts), this);
        rvWorkoutName.setAdapter(workoutRvNameAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        showProgressDialog();
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_view_chooser, null, false);
        frameLayout.addView(contentView, 0);
        ButterKnife.bind(this, frameLayout);
        workouts = getWorkouts();

        if (!Utils.collectionIsNullOrEmpty(workouts)) {
            if (Utils.isToday(workouts.get(0).getWorkoutDate())) {
                setTitle(R.string.today_title_string);
            } else {
                setTitle(R.string.view_title_string);
            }
        } else {
          setTitle(Constants.EMPTY_STRING);
        }

        setDisplayDate();
        setupRecyclerView();
        hideProgressDialog();
        MenuItem selectedItem = navigationView.getMenu().findItem(R.id.navi_today);
        selectedItem.setChecked(true);
    }
}
