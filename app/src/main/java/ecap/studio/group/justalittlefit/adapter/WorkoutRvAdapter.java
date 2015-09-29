package ecap.studio.group.justalittlefit.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.model.Workout;

public class WorkoutRvAdapter extends RecyclerView.Adapter<WorkoutRvAdapter.ViewHolder> {

    private List<Workout> workouts;
    private Context context;

    @Override
    public WorkoutRvAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_workout_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(WorkoutRvAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return workouts == null ? 0 : workouts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
     /*   public TextView countryName;
        public ImageView countryImage;*/

        public ViewHolder(View itemView) {
            super(itemView);
/*            countryName = (TextView) itemView.findViewById(R.id.countryName);
            countryImage = (ImageView)itemView.findViewById(R.id.countryImage);*/
        }

    }
}
