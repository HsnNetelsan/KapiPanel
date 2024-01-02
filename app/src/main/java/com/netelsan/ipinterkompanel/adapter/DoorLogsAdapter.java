package com.netelsan.ipinterkompanel.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.database.DatabaseHelper;
import com.netelsan.ipinterkompanel.model.Daire;
import com.netelsan.ipinterkompanel.model.DoorUnlockLog;

import java.util.ArrayList;

public class DoorLogsAdapter extends RecyclerView.Adapter<DoorLogsAdapter.ViewHolder> {

    Context context;

    public ArrayList<DoorUnlockLog> arrayList;
    private LayoutInflater mInflater;

    DatabaseHelper databaseHelper;

    public DoorLogsAdapter(Context context, ArrayList<DoorUnlockLog> arrayList) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.arrayList = arrayList;
        databaseHelper = new DatabaseHelper(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_item_door_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        DoorUnlockLog doorUnlockLog = arrayList.get(position);

        Daire ownerDaire = databaseHelper.getDaireByIP(doorUnlockLog.getPasswordOwnerIP());
        if(ownerDaire == null){
            holder.doorLogItemOwner.setVisibility(View.GONE);
        }else{
            holder.doorLogItemOwner.setVisibility(View.VISIBLE);
            holder.doorLogItemOwner.setText(ownerDaire.getIsim() + " " + ownerDaire.getSoyisim());
        }

        holder.doorLogItemlabel.setText(doorUnlockLog.getPasswordLabel());
        holder.doorLogItemDate.setText(doorUnlockLog.getDatetime());

        if(doorUnlockLog.getUnlockType() == Constants.DOOR_UNLOCK_RFID){
            holder.doorLogItemType.setText(context.getString(R.string.kart_kullaranak));
        }else{
            holder.doorLogItemType.setText(context.getString(R.string.sifre_kullanarak));
        }

        if(doorUnlockLog.isSelected()) {
            holder.daireLogItemContainer.setBackgroundResource(R.drawable.menu_item_selected);
            holder.doorLogItemOwner.setTextColor(context.getResources().getColor(R.color.black));
            holder.doorLogItemlabel.setTextColor(context.getResources().getColor(R.color.black));
            holder.doorLogItemDate.setTextColor(context.getResources().getColor(R.color.black));
            holder.doorLogItemType.setTextColor(context.getResources().getColor(R.color.black));
        } else {
            holder.daireLogItemContainer.setBackgroundResource(R.drawable.menu_item_unselected);
            holder.doorLogItemOwner.setTextColor(context.getResources().getColor(R.color.white));
            holder.doorLogItemlabel.setTextColor(context.getResources().getColor(R.color.white));
            holder.doorLogItemDate.setTextColor(context.getResources().getColor(R.color.white));
            holder.doorLogItemType.setTextColor(context.getResources().getColor(R.color.white));
        }

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView doorLogItemOwner;
        TextView doorLogItemlabel;
        TextView doorLogItemDate;
        TextView doorLogItemType;

        LinearLayout daireLogItemContainer;

        ViewHolder(View itemView) {
            super(itemView);
            doorLogItemOwner = itemView.findViewById(R.id.doorLogItemOwner);
            doorLogItemlabel = itemView.findViewById(R.id.doorLogItemlabel);
            doorLogItemDate = itemView.findViewById(R.id.doorLogItemDate);
            doorLogItemType = itemView.findViewById(R.id.doorLogItemType);

            daireLogItemContainer = itemView.findViewById(R.id.daireLogItemContainer);
        }

    }

    DoorUnlockLog getItem(int id) {
        return arrayList.get(id);
    }

    public ArrayList<DoorUnlockLog> getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList<DoorUnlockLog> arrayList) {
        this.arrayList = arrayList;
    }
}
