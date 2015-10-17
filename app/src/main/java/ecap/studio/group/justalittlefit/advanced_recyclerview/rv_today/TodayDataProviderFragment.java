package ecap.studio.group.justalittlefit.advanced_recyclerview.rv_today;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.ArrayList;

import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.model.Exercise;
import ecap.studio.group.justalittlefit.util.Constants;
import ecap.studio.group.justalittlefit.util.Utils;

public class TodayDataProviderFragment extends Fragment {
    private TodayDataProvider mDataProvider;
    private ArrayList<Exercise> exercises;

    public static TodayDataProviderFragment newInstance(ArrayList<Exercise> exercises) {
        TodayDataProviderFragment fragment = new TodayDataProviderFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(Constants.EXERCISE_LIST, exercises);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        Bundle args = getArguments();

        if (args != null) {
            exercises = args.getParcelableArrayList(Constants.EXERCISE_LIST);
            mDataProvider = new TodayDataProvider(exercises);
        } else {
            Utils.displayLongToast(this.getActivity(), getString(R.string.data_load_error));
        }
    }

    public AbstractExpandableDataProvider getDataProvider() {
        return mDataProvider;
    }
}