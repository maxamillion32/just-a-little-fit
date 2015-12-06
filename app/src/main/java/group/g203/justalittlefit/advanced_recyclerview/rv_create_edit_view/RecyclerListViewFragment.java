package group.g203.justalittlefit.advanced_recyclerview.rv_create_edit_view;

import android.app.Activity;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

import group.g203.justalittlefit.R;
import group.g203.justalittlefit.activity.BaseNaviDrawerActivity;
import group.g203.justalittlefit.activity.CreateEditExercise;
import group.g203.justalittlefit.activity.CreateEditSet;
import group.g203.justalittlefit.activity.CreateEditWorkout;

public class RecyclerListViewFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;
    private ProgressBar progressDialog;
    private Activity mActivity;
    private CreateEditWorkout createEditWorkoutActivity;
    private CreateEditExercise createEditExerciseActivity;
    private CreateEditSet createEditSetActivity;

    public RecyclerListViewFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recycler_list_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressDialog = (ProgressBar) getView().findViewById(R.id.progressDialog);
        progressDialog.bringToFront();
        ((BaseNaviDrawerActivity)mActivity).setProgressDialogReady(true);
        this.populateActivity();

        //noinspection ConstantConditions
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());

        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        // drag & drop manager
        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        mRecyclerViewDragDropManager.setDraggingItemShadowDrawable(
                (NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow_z3));

        // swipe manager
        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        //adapter
        final MyDraggableSwipeableItemAdapter myItemAdapter = new MyDraggableSwipeableItemAdapter(getDataProvider());
        myItemAdapter.setEventListener(new MyDraggableSwipeableItemAdapter.EventListener() {
            @Override
            public void onItemRemoved(int position, Object dataObject) {
                if (createEditWorkoutActivity != null) {
                    createEditWorkoutActivity.onItemRemoved(dataObject);
                } else if (createEditExerciseActivity != null) {
                    createEditExerciseActivity.onItemRemoved(dataObject);
                }  else if (createEditSetActivity != null) {
                    createEditSetActivity.onItemRemoved(dataObject);
                }
            }

            @Override
            public void onItemPinned(int position) {
                // do nothing, pinning not supported in this app
            }

            @Override
            public void onItemViewClicked(View v, boolean pinned) {
                onItemViewClick(v, pinned);
            }
        });

        mAdapter = myItemAdapter;

        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(myItemAdapter);      // wrap for dragging
        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(mWrappedAdapter);      // wrap for swiping

        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();

        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Disable the change animation in order to make turning back animation of swiped item works properly.
        animator.setSupportsChangeAnimations(false);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        mRecyclerView.setItemAnimator(animator);

        // additional decorations
        //noinspection StatementWithEmptyBody
        if (supportsViewElevation()) {
            // Lollipop or later has native drop shadow feature. ItemShadowDecorator is not required.
        } else {
            mRecyclerView.addItemDecoration(new ItemShadowDecorator((NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow_z1)));
        }
        mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(getResources().getDrawable(R.drawable.list_divider), true));

        // NOTE:
        // The initialization order is very important! This order determines the priority of touch event handling.
        //
        // priority: TouchActionGuard > Swipe > DragAndDrop
        mRecyclerViewTouchActionGuardManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewSwipeManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);

        // for debugging
//        animator.setDebug(true);
//        animator.setMoveDuration(2000);
//        animator.setRemoveDuration(2000);
//        mRecyclerViewSwipeManager.setMoveToOutsideWindowAnimationDuration(2000);
//        mRecyclerViewSwipeManager.setReturnToDefaultPositionAnimationDuration(2000);
    }

    @Override
    public void onPause() {
        mRecyclerViewDragDropManager.cancelDrag();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if (mRecyclerViewDragDropManager != null) {
            mRecyclerViewDragDropManager.release();
            mRecyclerViewDragDropManager = null;
        }

        if (mRecyclerViewSwipeManager != null) {
            mRecyclerViewSwipeManager.release();
            mRecyclerViewSwipeManager = null;
        }

        if (mRecyclerViewTouchActionGuardManager != null) {
            mRecyclerViewTouchActionGuardManager.release();
            mRecyclerViewTouchActionGuardManager = null;
        }

        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
        mAdapter = null;
        mLayoutManager = null;
        ((BaseNaviDrawerActivity)mActivity).setProgressDialogReady(false);
        mActivity = null;
        createEditWorkoutActivity = null;
        createEditExerciseActivity = null;
        createEditSetActivity = null;

        super.onDestroyView();
    }

    private void onItemViewClick(View v, boolean pinned) {
        int position = mRecyclerView.getChildPosition(v);
        if (createEditWorkoutActivity != null) {
            if (position != RecyclerView.NO_POSITION) {
                createEditWorkoutActivity.onItemClicked(position);
            }
        } else if (createEditWorkoutActivity == null && createEditSetActivity == null) {
            if (createEditExerciseActivity != null && position != RecyclerView.NO_POSITION) {
                createEditExerciseActivity.onItemClicked(position);
            }
        } else if (createEditWorkoutActivity == null && createEditExerciseActivity == null) {
            if (createEditSetActivity != null && position != RecyclerView.NO_POSITION) {
                createEditSetActivity.onItemClicked(position);
            }
        }
    }

    private boolean supportsViewElevation() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    public AbstractDataProvider getDataProvider() {
        if (createEditWorkoutActivity != null) {
            return createEditWorkoutActivity.getDataProvider();
        } else if (createEditExerciseActivity != null) {
            return createEditExerciseActivity.getDataProvider();
        } else if (createEditSetActivity != null) {
            return createEditSetActivity.getDataProvider();
        }
        return null;
    }

    public MyDraggableSwipeableItemAdapter getAdapter() {
        return (MyDraggableSwipeableItemAdapter) mAdapter;
    }

    public void notifyItemChanged(int position) {
        mAdapter.notifyItemChanged(position);
    }

    public void notifyItemInserted(int position) {
        mAdapter.notifyItemInserted(position);
        mRecyclerView.scrollToPosition(position);
    }

    public ProgressBar getProgressDialog() {
        return progressDialog;
    }

    @Override
    public void onAttach (Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    private void populateActivity() {
        if (mActivity instanceof CreateEditWorkout) {
            createEditWorkoutActivity = (CreateEditWorkout) mActivity;
            createEditExerciseActivity = null;
            createEditSetActivity = null;
        } else if (mActivity instanceof CreateEditExercise) {
            createEditExerciseActivity = (CreateEditExercise) mActivity;
            createEditWorkoutActivity = null;
            createEditSetActivity = null;
        } else if (mActivity instanceof CreateEditSet) {
            createEditSetActivity = (CreateEditSet) mActivity;
            createEditWorkoutActivity = null;
            createEditExerciseActivity = null;
        }
    }
}