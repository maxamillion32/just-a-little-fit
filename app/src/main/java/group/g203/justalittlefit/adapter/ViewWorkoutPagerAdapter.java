package group.g203.justalittlefit.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Pager adapter for {@link group.g203.justalittlefit.activity.ViewActivity}.
 */
public class ViewWorkoutPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> viewWorkoutFragments;

    public ViewWorkoutPagerAdapter(FragmentManager fm, List<Fragment> viewWorkoutFragments) {
        super(fm);
        this.viewWorkoutFragments = viewWorkoutFragments;
    }

    @Override
    public Fragment getItem(int position) {
        return this.viewWorkoutFragments.get(position);
    }

    @Override
    public int getCount() {
        return this.viewWorkoutFragments.size();
    }
}
