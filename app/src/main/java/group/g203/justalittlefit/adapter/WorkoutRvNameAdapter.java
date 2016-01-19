package group.g203.justalittlefit.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import group.g203.justalittlefit.R;
import group.g203.justalittlefit.activity.ViewActivity;
import group.g203.justalittlefit.activity.ViewChooserActivity;
import group.g203.justalittlefit.model.Workout;
import group.g203.justalittlefit.util.Constants;

/**
 * RecyclerView adapter for {@link ViewChooserActivity}.
 */
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

        WorkoutRvNameAdapter.ViewHolder viewHolder = new ViewHolder(v, new ViewHolder.ViewHolderClick() {
            @Override
            public void onRvRowClick(View caller, Workout workoutObj) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(Constants.WORKOUT, workoutObj);

                Intent intent = new Intent(context, ViewActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(WorkoutRvNameAdapter.ViewHolder holder, int position) {
        Workout workout = workouts.get(position);
        holder.workoutName.setText(workout.getName());
        holder.workout = workout;
    }

    @Override
    public int getItemCount() {
        return workouts == null ? 0 : workouts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView workoutName;
        public RelativeLayout container;
        public Workout workout;
        public ViewHolderClick mListener;

        public ViewHolder(View itemView, ViewHolderClick viewHolderClick) {
            super(itemView);
            workoutName = (TextView) itemView.findViewById(R.id.tvWorkoutTitle);
            mListener = viewHolderClick;
            container = (RelativeLayout) itemView.findViewById(R.id.rlRvContainer);
            container.setOnClickListener(this);
            workoutName.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onRvRowClick(v, workout);
        }

        public static interface ViewHolderClick {
            public void onRvRowClick(View caller, Workout workoutObj);
        }
    }
}
