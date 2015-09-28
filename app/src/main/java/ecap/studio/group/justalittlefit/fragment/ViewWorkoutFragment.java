package ecap.studio.group.justalittlefit.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.model.Workout;
import ecap.studio.group.justalittlefit.util.Constants;

public class ViewWorkoutFragment extends Fragment {

    @InjectView(R.id.tvTest)
    TextView tvTest;

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
        Workout workout = (Workout)getArguments().getParcelable(Constants.WORKOUT);
        View v = inflater.inflate(R.layout.frag_view_workout, container, false);
        ButterKnife.inject(this, v);
        tvTest.setText(workout.getName());
        return v;
    }
}
