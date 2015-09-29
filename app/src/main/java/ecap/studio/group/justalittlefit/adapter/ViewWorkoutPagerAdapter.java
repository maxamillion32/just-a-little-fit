package ecap.studio.group.justalittlefit.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

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
