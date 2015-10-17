package ecap.studio.group.justalittlefit.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.model.Workout;

public class WorkoutRvNameAdapter extends RecyclerView.Adapter<WorkoutRvNameAdapter.ViewHolder> {

    private List<Workout> workouts;
    private Context context;

    public WorkoutRvNameAdapter(List<Workout> workouts, Context context) {
        this.workouts = workouts;
        this.context = context;
    }

    @Override
    public WorkoutRvNameAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_workout_title_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(WorkoutRvNameAdapter.ViewHolder holder, int position) {
        Workout workout = workouts.get(position);
        holder.workoutName.setText(workout.getName());
    }

    @Override
    public int getItemCount() {
        return workouts == null ? 0 : workouts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView workoutName;

        public ViewHolder(View itemView) {
            super(itemView);
            workoutName = (TextView) itemView.findViewById(R.id.tvWorkoutTitle);
        }
    }
}
