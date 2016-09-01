package fi.tut.cs.social.socialdisplay3;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class ProfilesRecyclerViewAdapter extends RecyclerView.Adapter<ProfilesRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "MyProfilesAdapter";

    List<Profile> profilesItemList;
    MainActivity mainActivity;

    public ProfilesRecyclerViewAdapter(List<Profile> items, MainActivity activity) {
        profilesItemList = items;
        mainActivity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.profiles_list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.mItem = profilesItemList.get(position);
        holder.profileNameTV.setText(profilesItemList.get(position).getName());
        holder.profileTypeTV.setText(profilesItemList.get(position).getType());

        if(profilesItemList.get(position).getActive()) {
            holder.activeCB.setChecked(true);
            holder.layout.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.colorPrimaryLight));
        }
        else {
            holder.activeCB.setChecked(false);
            holder.layout.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.colorSecondaryLight));
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainActivity != null) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mainActivity.onListFragmentInteraction(holder.mItem, false);
                }
            }
        });

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mainActivity != null) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mainActivity.onListFragmentInteraction(holder.mItem, true);
                    return true;
                }
                return false;
            }
        });

        holder.activeCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Checkbox clicked. " + holder.activeCB.isChecked());

                if(holder.activeCB.isChecked()) {
                    holder.mItem.setActive(true);
                    mainActivity.activateProfile(holder.mItem);
                    holder.layout.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.colorPrimaryLight));
                }
                else {
                    holder.layout.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.colorSecondaryLight));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return profilesItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView profileNameTV;
        public final TextView profileTypeTV;
        public Profile mItem;
        public CheckBox activeCB;
        public RelativeLayout layout;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            profileNameTV = (TextView) view.findViewById(R.id.nameTV);
            profileTypeTV = (TextView) view.findViewById(R.id.typeTV);
            activeCB = (CheckBox) view.findViewById(R.id.activeCB);
            layout = (RelativeLayout) view.findViewById(R.id.profile_item_layout);
        }

    }
}
