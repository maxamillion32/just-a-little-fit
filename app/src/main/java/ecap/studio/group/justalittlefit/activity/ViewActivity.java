package ecap.studio.group.justalittlefit.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.adapter.ViewWorkoutPagerAdapter;
import ecap.studio.group.justalittlefit.database.QueryExecutor;
import ecap.studio.group.justalittlefit.dialog.InformationDialog;
import ecap.studio.group.justalittlefit.fragment.ViewWorkoutFragment;
import ecap.studio.group.justalittlefit.model.Workout;
import ecap.studio.group.justalittlefit.util.Constants;
import me.relex.circleindicator.CircleIndicator;

public class ViewActivity extends BaseNaviDrawerActivity {

    @InjectView(R.id.vpWorkouts)
    ViewPager vpWorkouts;
    @InjectView(R.id.circleIndicator)
    CircleIndicator circleIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final BaseNaviDrawerActivity activity = this;
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_view, null, false);
        frameLayout.addView(contentView, 0);
        ButterKnife.inject(this, frameLayout);
        setTitle(R.string.view_title_string);
        setViewPager();
    }

    void setViewPager() {
        List<Fragment> workoutFragments = createFrags();
        ViewWorkoutPagerAdapter viewWorkoutPagerAdapter = new ViewWorkoutPagerAdapter(getSupportFragmentManager(), createFrags());
        vpWorkouts.setAdapter(viewWorkoutPagerAdapter);
        circleIndicator.setViewPager(vpWorkouts);
        if (viewWorkoutPagerAdapter.getCount() <= Constants.INT_ONE) {
            circleIndicator.setVisibility(View.GONE);
        }
    }

    private List<Fragment> createFrags() {
        try {
            List<Fragment> workoutFrags = new ArrayList<>();
            Workout workout = QueryExecutor.getWorkoutById(1);
            Workout workout1 = QueryExecutor.getWorkoutById(2);
            ViewWorkoutFragment frag = ViewWorkoutFragment.getNewInstance(workout);
            ViewWorkoutFragment frag1 = ViewWorkoutFragment.getNewInstance(workout1);
            workoutFrags.add(frag);
            workoutFrags.add(frag1);
            return workoutFrags;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    void setupDrawerContent(NavigationView navigationView) {
        // Check menu item of currently displayed activity
        MenuItem selectedItem = navigationView.getMenu().findItem(R.id.navi_view);
        selectedItem.setChecked(true);
        super.setupDrawerContent(navigationView);
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

    private void displayInfoDialog() {
        FragmentManager fm = getSupportFragmentManager();
        InformationDialog dialog = InformationDialog.newInstance(Constants.VIEW_TEXT);
        dialog.show(fm, getString(R.string.infoDialogTagExercise));
    }
}
