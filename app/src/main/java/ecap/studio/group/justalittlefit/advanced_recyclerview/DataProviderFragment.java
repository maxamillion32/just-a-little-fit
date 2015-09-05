package ecap.studio.group.justalittlefit.advanced_recyclerview;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.HashSet;

import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.model.Exercise;
import ecap.studio.group.justalittlefit.model.Set;
import ecap.studio.group.justalittlefit.model.Workout;
import ecap.studio.group.justalittlefit.util.Constants;
import ecap.studio.group.justalittlefit.util.Utils;

public class DataProviderFragment extends Fragment {
    private AbstractDataProvider mDataProvider;
    private String dataType;
    private ArrayList<Workout> workouts;
    private ArrayList<Exercise> exercises;
    private ArrayList<Set> sets;

    public static DataProviderFragment newInstance(String dataType, ArrayList<Workout> workouts) {
        DataProviderFragment fragment = new DataProviderFragment();
        Bundle args = new Bundle();
        args.putString(Constants.DATA_FRAG_TYPE, dataType);
        args.putParcelableArrayList(Constants.WORKOUT_LIST, workouts);
        fragment.setArguments(args);
        return fragment;
    }

    public static DataProviderFragment newInstance(ArrayList<Exercise> exercises, String dataType) {
        DataProviderFragment fragment = new DataProviderFragment();
        Bundle args = new Bundle();
        args.putString(Constants.DATA_FRAG_TYPE, dataType);
        args.putParcelableArrayList(Constants.EXERCISE_LIST, exercises);
        fragment.setArguments(args);
        return fragment;
    }

    public static DataProviderFragment newInstance(java.util.Set<Set> sets, String dataType) {
        DataProviderFragment fragment = new DataProviderFragment();
        ArrayList<Set> setArrayList = new ArrayList<>(sets);
        Bundle args = new Bundle();
        args.putString(Constants.DATA_FRAG_TYPE, dataType);
        args.putParcelableArrayList(Constants.SET_LIST, setArrayList);
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

            switch (dataType) {
                case Constants.WORKOUT:
                    workouts = args.getParcelableArrayList(Constants.WORKOUT_LIST);
                    mDataProvider = new DataProvider(dataType, workouts);
                    break;
                case Constants.EXERCISE:
                    exercises = args.getParcelableArrayList(Constants.EXERCISE_LIST);
                    mDataProvider = new DataProvider(exercises, dataType);
                    break;
                case Constants.SET:
                    sets = args.getParcelableArrayList(Constants.SET_LIST);
                    HashSet<Set> hashSet = new HashSet<>(sets);
                    mDataProvider = new DataProvider(hashSet, Constants.SET);
                    break;
            }
        } else {
            Utils.displayLongToast(this.getActivity(), getString(R.string.data_load_error));
        }
    }

    public AbstractDataProvider getDataProvider() {
        return mDataProvider;
    }
}