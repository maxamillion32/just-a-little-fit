package ecap.studio.group.justalittlefit.advanced_recyclerview;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.ArrayList;

import ecap.studio.group.justalittlefit.model.Workout;
import ecap.studio.group.justalittlefit.util.Constants;
import ecap.studio.group.justalittlefit.util.Utils;

public class DataProviderFragment extends Fragment {
    private AbstractDataProvider mDataProvider;
    private String dataType;
    private ArrayList<Workout> workouts;

    public static DataProviderFragment newInstance(String dataType, ArrayList<Workout> workouts) {
        DataProviderFragment fragment = new DataProviderFragment();
        Bundle args = new Bundle();
        args.putString(Constants.DATA_FRAG_TYPE, dataType);
        args.putParcelableArrayList(Constants.WORKOUT_LIST, workouts);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle args = getArguments();

        if (args != null) {
            dataType = args.getString(Constants.DATA_FRAG_TYPE);
            workouts = args.getParcelableArrayList(Constants.WORKOUT_LIST);
            switch (dataType) {
                case Constants.WORKOUT:
                    mDataProvider = new DataProvider(dataType, workouts);
                    break;
            }
        } else {
            Utils.displayLongToast(this.getActivity(), "Error getting DataProviderFragment");
        }
    }

    public AbstractDataProvider getDataProvider() {
        return mDataProvider;
    }
}