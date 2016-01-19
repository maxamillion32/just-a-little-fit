package group.g203.justalittlefit.advanced_recyclerview.rv_view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.ArrayList;

import group.g203.justalittlefit.R;
import group.g203.justalittlefit.model.Exercise;
import group.g203.justalittlefit.util.Constants;
import group.g203.justalittlefit.util.Utils;

public class DataProviderFragmentForView extends Fragment {
    private DataProviderForView mDataProvider;
    private ArrayList<Exercise> exercises;

    public static DataProviderFragmentForView newInstance(ArrayList<Exercise> exercises) {
        DataProviderFragmentForView fragment = new DataProviderFragmentForView();
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
            mDataProvider = new DataProviderForView(exercises);
        } else {
            Utils.displayLongToast(this.getActivity(), getString(R.string.data_load_error));
        }
    }

    public AbstractExpandableDataProvider getDataProvider() {
        return mDataProvider;
    }
}