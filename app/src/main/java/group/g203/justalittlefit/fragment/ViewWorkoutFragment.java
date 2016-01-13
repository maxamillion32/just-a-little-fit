package group.g203.justalittlefit.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import group.g203.justalittlefit.R;
import group.g203.justalittlefit.adapter.WorkoutRvAdapter;
import group.g203.justalittlefit.model.Workout;
import group.g203.justalittlefit.util.Constants;
import group.g203.justalittlefit.util.Utils;

/**
 * Fragments used in {@link group.g203.justalittlefit.activity.ViewActivity}.
 */
public class ViewWorkoutFragment extends Fragment {

    @InjectView(R.id.tvWorkoutName)
    TextView tvWorkoutName;
    @InjectView(R.id.rvWorkoutInfo)
    RecyclerView rvWorkoutInfo;
    WorkoutRvAdapter workoutRvAdapter;
    Workout workout;

    public ViewWorkoutFragment() {}

    public static final ViewWorkoutFragment getNewInstance(Workout workout) {
        ViewWorkoutFragment viewWorkoutFragment = new ViewWorkoutFragment();
        Bundle bundle = new Bundle(Constants.INT_ONE);
        bundle.putParcelable(Constants.WORKOUT, workout);
        viewWorkoutFragment.setArguments(bundle);
        return viewWorkoutFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        workout = getArguments().getParcelable(Constants.WORKOUT);
        View v = inflater.inflate(R.layout.frag_view_workout, container, false);
        ButterKnife.inject(this, v);
        if (Utils.isWorkoutComplete(workout)) {
            Utils.strikeThroughText(tvWorkoutName);
        } else {
            Utils.clearStrikeThroughText(tvWorkoutName);
        }
        tvWorkoutName.setText(workout.getName());
        setupRecyclerView();
        return v;
    }

    private void setupRecyclerView() {
        rvWorkoutInfo.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvWorkoutInfo.setItemAnimator(new DefaultItemAnimator());

        workoutRvAdapter = new WorkoutRvAdapter(
                new ArrayList<>(workout.getExercises()), getActivity());
        rvWorkoutInfo.setAdapter(workoutRvAdapter);
    }
}
