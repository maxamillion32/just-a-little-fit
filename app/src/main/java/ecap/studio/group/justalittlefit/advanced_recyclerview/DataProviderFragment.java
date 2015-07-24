package ecap.studio.group.justalittlefit.advanced_recyclerview;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class DataProviderFragment extends Fragment {
    private AbstractDataProvider mDataProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);  // keep the mDataProvider instance
        mDataProvider = new DataProvider();
    }

    public AbstractDataProvider getDataProvider() {
        return mDataProvider;
    }
}