package fi.tut.cs.social.socialdisplay3;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class ProfilesFragment extends Fragment {
    private static final String TAG = "ProfilesFragment";

    private OnListFragmentInteractionListener mListener;
    private List<Profile> profilesItemList;
    private ProfilesRecyclerViewAdapter adapter;
    MainActivity mainActivity;

    public ProfilesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        if (getArguments() != null) {
            //mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        profilesItemList = new ArrayList<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profiles_list, container, false);
        Log.d(TAG, "onCreateView");

        mainActivity = (MainActivity) getActivity();

        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.profiles_list);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        adapter = new ProfilesRecyclerViewAdapter(profilesItemList, (MainActivity) getActivity());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        Log.d(TAG, "list size: " + profilesItemList.size());

        mainActivity.showFab();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
        mListener = null;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");

        // Populate the list of profiles
        final View mView = view;
        view.post(new Runnable() {
            @Override
            public void run() {
                mainActivity.readProfiles();
            }
        });

    }

    public void addProfilesToList() {

        profilesItemList.clear();
        for(Profile tempProfile : mainActivity.myProfiles) {
            profilesItemList.add(tempProfile);
        }
        adapter.notifyDataSetChanged();
    }

    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Profile profile, Boolean longPress);
    }
}
