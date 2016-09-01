package fi.tut.cs.social.socialdisplay3;

import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Aris on 26/02/16.
 */
public class AppsRecyclerViewAdapter extends RecyclerView.Adapter<AppsRecyclerViewAdapter.ViewHolder> {

    List<App> appsItemList;
    AppsListActivity appsListActivity;

    public AppsRecyclerViewAdapter(List<App> items, AppsListActivity activity) {
        appsItemList = items;
        appsListActivity = activity;
    }

    @Override
    public AppsRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.apps_list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AppsRecyclerViewAdapter.ViewHolder holder, int position) {

        final int pos = position;
        holder.mItem = appsItemList.get(position);
        holder.nameTV.setText(appsItemList.get(position).getName());
        holder.iconIV.setImageBitmap(appsItemList.get(position).getIcon());

        holder.selectedCB.setOnCheckedChangeListener(null);

        final App app = appsItemList.get(position);
        holder.selectedCB.setChecked(app.getSelected());

        holder.selectedCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                app.setSelected(b);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appsItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView iconIV;
        public final TextView nameTV;
        public CheckBox selectedCB;
        public App mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            iconIV = (ImageView) view.findViewById(R.id.iconIV);
            nameTV = (TextView) view.findViewById(R.id.nameTV);
            selectedCB = (CheckBox) view.findViewById(R.id.selectedCB);
        }

    }
}
