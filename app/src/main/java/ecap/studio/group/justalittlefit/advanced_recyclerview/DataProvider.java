package ecap.studio.group.justalittlefit.advanced_recyclerview;

import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ecap.studio.group.justalittlefit.model.Exercise;
import ecap.studio.group.justalittlefit.model.Set;
import ecap.studio.group.justalittlefit.model.Workout;
import ecap.studio.group.justalittlefit.util.Constants;

public class DataProvider extends AbstractDataProvider {
    private List<ConcreteData> mData;
    private ConcreteData mLastRemovedData;
    private int mLastRemovedPosition = -1;
    private String dataType;
    private List<String> displayNames;

    public DataProvider(String dataType, List<Workout> workouts) {
        this.dataType = dataType;
        mData = new LinkedList<>();
        displayNames = new ArrayList<>();

        for (Workout workout : workouts) {
            final long id = mData.size();
            final int viewType = 0;
            final int swipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_LEFT | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_RIGHT;
            mData.add(new ConcreteData(id, viewType, workout.getName(),
                    swipeReaction, workout, dataType));
            displayNames.add(workout.getName().trim());
        }
    }


    public DataProvider(List<Exercise> exercises, String dataType) {
        this.dataType = dataType;
        mData = new LinkedList<>();
        displayNames = new ArrayList<>();

        for (Exercise exercise : exercises) {
            final long id = mData.size();
            final int viewType = 0;
            final int swipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_LEFT | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_RIGHT;
            mData.add(new ConcreteData(id, viewType, exercise.getName(),
                    swipeReaction, exercise, dataType));
            displayNames.add(exercise.getName().trim());
        }
    }

    public DataProvider(java.util.Set<Set> sets, String dataType) {
        this.dataType = dataType;
        mData = new LinkedList<>();
        displayNames = new ArrayList<>();

        for (Set set : sets) {
            final long id = mData.size();
            final int viewType = 0;
            final int swipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_LEFT | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_RIGHT;
            mData.add(new ConcreteData(id, viewType, returnSetRowDisplayText(set),
                    swipeReaction, set, dataType));
            displayNames.add(returnSetRowDisplayText(set).trim());
        }
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Data getItem(int index) {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException("index = " + index);
        }

        return mData.get(index);
    }

    @Override
    public int undoLastRemoval() {
        if (mLastRemovedData != null) {
            int insertedPosition;
            if (mLastRemovedPosition >= 0 && mLastRemovedPosition < mData.size()) {
                insertedPosition = mLastRemovedPosition;
            } else {
                insertedPosition = mData.size();
            }

            mData.add(insertedPosition, mLastRemovedData);

            mLastRemovedData = null;
            mLastRemovedPosition = -1;

            return insertedPosition;
        } else {
            return -1;
        }
    }

    @Override
    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        final ConcreteData item = mData.remove(fromPosition);

        switch (item.mDataType) {
            case Constants.WORKOUT:
                Workout workout = (Workout) item.mDataObject;
                workout.setOrderNumber(toPosition);
        }

        mData.add(toPosition, item);
        mLastRemovedPosition = -1;
    }

    @Override
    public void removeItem(int position) {
        //noinspection UnnecessaryLocalVariable
        final ConcreteData removedItem = mData.remove(position);

        mLastRemovedData = removedItem;
        mLastRemovedPosition = position;
    }

    public static final class ConcreteData extends Data {

        private final long mId;
        private final String mText;
        private final int mViewType;
        private final int mSwipeReaction;
        private boolean mPinnedToSwipeLeft;
        private Object mDataObject;
        private String mDataType;

        ConcreteData(long id, int viewType, String text, int swipeReaction,
                     Object dataObject, String dataType) {
            mId = id;
            mViewType = viewType;
            mText = text;
            mSwipeReaction = swipeReaction;
            mDataObject = dataObject;
            mDataType = dataType;
        }

        @Override
        public boolean isSectionHeader() {
            return false;
        }

        @Override
        public int getViewType() {
            return mViewType;
        }

        @Override
        public long getId() {
            return mId;
        }

        @Override
        public String toString() {
            return mText;
        }

        @Override
        public int getSwipeReactionType() {
            return mSwipeReaction;
        }

        @Override
        public String getText() {
            return mText;
        }

        @Override
        public boolean isPinnedToSwipeLeft() {
            return mPinnedToSwipeLeft;
        }

        @Override
        public void setPinnedToSwipeLeft(boolean pinedToSwipeLeft) {
            mPinnedToSwipeLeft = pinedToSwipeLeft;
        }

        @Override
        public Object getDataObject() {
            return mDataObject;
        }

        @Override
        public String getDataType() {
            return mDataType;
        }
    }

     public List<Object> getDataObjects() {
        List<Object> dataObjs = new ArrayList<>();
        for (ConcreteData data : mData) {
            dataObjs.add(data.mDataObject);
        }
        return dataObjs;
    }

    public List<String> getDisplayNames() {
        return displayNames;
    }

    private String returnSetRowDisplayText(Set set) {
        switch (set.getExerciseTypeCode()) {
            case Constants.WEIGHTS:
                return set.getReps() + " rep(s) of " + set.getWeight() + Constants.SPACE + set.getWeightTypeCode().toLowerCase();
            case Constants.LOGGED_TIMED:
                return set.getReps() + " rep(s) timed at " + forceTwoDigitTime(set.getHours()) + Constants.COLON
                        + forceTwoDigitTime(set.getMinutes()) + Constants.COLON + forceTwoDigitTime(set.getSeconds());
        }
        return null;
    }

    private String forceTwoDigitTime(int timeVal) {
        String timeString = String.valueOf(timeVal);
        if (timeString.length() == 1) {
            return "0" + timeString;
        } else {
            return timeString;
        }
    }
}

