package fred.docapp;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SaveFragment extends Fragment {

    // data object we want to retain
    private SavedSearchData data;

    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    public void setData(SavedSearchData data) {
        this.data = data;
    }

    public SavedSearchData getData() {
        return data;
    }
}
