package ecap.studio.group.justalittlefit.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;

import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.model.Exercise;
import ecap.studio.group.justalittlefit.model.Set;

public class WorkoutRvAdapter extends RecyclerView.Adapter<WorkoutRvAdapter.ViewHolder> {

    private List<Exercise> exercises;
    private Context context;

    public WorkoutRvAdapter(List<Exercise> exercises, Context context) {
        this.exercises = exercises;
        this.context = context;
    }

    @Override
    public WorkoutRvAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_workout_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(WorkoutRvAdapter.ViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);
        holder.exerciseName.setText(exercise.getName());

        for (Set set : exercise.getSets()) {
            TableRow row = new TableRow(context);
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            TextView tvSet = new TextView(context);
            tvSet.setGravity(Gravity.CENTER);
            tvSet.setPadding(0, 0, 0, 5);
            tvSet.setText("\u2022 " + set.toString());
            row.addView(tvSet);
            holder.exerciseContainer.addView(row);
        }
    }


    @Override
    public int getItemCount() {
        return exercises == null ? 0 : exercises.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView exerciseName;
        public LinearLayout exerciseContainer;

        public ViewHolder(View itemView) {
            super(itemView);
            exerciseName = (TextView) itemView.findViewById(R.id.tvExerciseName);
            exerciseContainer = (LinearLayout)itemView.findViewById(R.id.exerciseContainer);
        }
    }
}
