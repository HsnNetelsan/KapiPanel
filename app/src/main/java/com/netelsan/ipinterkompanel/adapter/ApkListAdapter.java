package com.netelsan.ipinterkompanel.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.model.APKListItem;

import java.io.File;
import java.util.ArrayList;

public class ApkListAdapter extends RecyclerView.Adapter<ApkListAdapter.ViewHolder> {

    public ArrayList<APKListItem> arrayList;
    private LayoutInflater mInflater;
    Context context;

    public ApkListAdapter(Context context, ArrayList<APKListItem> arrayList) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.arrayList = arrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_item_apk, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        APKListItem apkListItem = arrayList.get(position);

        File file = apkListItem.getFile();

        if (apkListItem.isSelected()) {
            holder.apkItemContainer.setBackgroundColor(Color.WHITE);
            holder.apkItemName.setTextColor(Color.BLACK);
            holder.apkItemCheck.setVisibility(View.VISIBLE);
        } else {
            holder.apkItemContainer.setBackgroundColor(Color.BLACK);
            holder.apkItemName.setTextColor(Color.WHITE);
            holder.apkItemCheck.setVisibility(View.GONE);
        }

        holder.apkItemName.setText(file.getName());

//        holder.daireItemItemContainer.post(new Runnable() {
//            @Override
//            public void run() {
//                Helper.showTutorialIfNeeded(context, holder.daireItemItemContainer, Constants.TUTORIAL_HOME_ARAMA_REHBER, false);
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        LinearLayout apkItemContainer;

        TextView apkItemName;

        ImageView apkItemCheck;

        ViewHolder(View itemView) {
            super(itemView);
            apkItemContainer = itemView.findViewById(R.id.apkItemContainer);
            apkItemName = itemView.findViewById(R.id.apkItemName);
            apkItemCheck = itemView.findViewById(R.id.apkItemCheck);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
//            if(listener != null) {
//                int position = getAdapterPosition();
//                listener.itemClickedAPK(position, arrayList.get(position));
//            }

        }

    }

    APKListItem getItem(int id) {
        return arrayList.get(id);
    }

    public ArrayList<APKListItem> getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList<APKListItem> arrayList) {
        this.arrayList = arrayList;
    }
}
