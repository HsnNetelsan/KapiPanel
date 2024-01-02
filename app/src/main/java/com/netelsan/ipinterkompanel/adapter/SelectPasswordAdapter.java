package com.netelsan.ipinterkompanel.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.database.DatabaseHelper;
import com.netelsan.ipinterkompanel.model.Daire;
import com.netelsan.ipinterkompanel.model.DoorPassword;

import java.util.ArrayList;

public class SelectPasswordAdapter extends RecyclerView.Adapter<SelectPasswordAdapter.ViewHolder> {

    Context context;

    public ArrayList<DoorPassword> arrayList;
    private LayoutInflater mInflater;
    DatabaseHelper databaseHelper;

    boolean isForDoorPassword;

    public SelectPasswordAdapter(Context context, ArrayList<DoorPassword> arrayList, boolean isForDoorPassword) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.arrayList = arrayList;
        this.isForDoorPassword = isForDoorPassword;
        databaseHelper = new DatabaseHelper(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_item_select_password, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        DoorPassword doorPassword = arrayList.get(position);

        Daire daire = databaseHelper.getDaireByIP(doorPassword.getIp());

        holder.selectPasswordIsim.setText(daire.getIsim());
        holder.selectPasswordSoyisim.setText(daire.getSoyisim());

        if(isForDoorPassword) {
            holder.selectPasswordPassword.setText("Şifre: " + doorPassword.getDoor());
        } else {
            String RFIDCode = doorPassword.getRfid();
            RFIDCode = RFIDCode.replaceAll(System.getProperty("line.separator"), "");
            holder.selectPasswordPassword.setText("RFID: " + RFIDCode);
        }
        holder.selectPasswordLabel.setText(doorPassword.getPasswordLabel());

        if(doorPassword.isSelected()) {
            holder.selectPasswordItemContainer.setBackgroundResource(R.drawable.menu_item_selected);
            holder.selectPasswordIsim.setTextColor(context.getResources().getColor(R.color.black));
            holder.selectPasswordSoyisim.setTextColor(context.getResources().getColor(R.color.black));
            holder.selectPasswordPassword.setTextColor(context.getResources().getColor(R.color.black));
            holder.selectPasswordLabel.setTextColor(context.getResources().getColor(R.color.black));
        } else {
            holder.selectPasswordItemContainer.setBackgroundResource(R.drawable.menu_item_unselected);
            holder.selectPasswordIsim.setTextColor(context.getResources().getColor(R.color.white));
            holder.selectPasswordSoyisim.setTextColor(context.getResources().getColor(R.color.white));
            holder.selectPasswordPassword.setTextColor(context.getResources().getColor(R.color.white));
            holder.selectPasswordLabel.setTextColor(context.getResources().getColor(R.color.white));
        }

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView selectPasswordIsim;
        TextView selectPasswordSoyisim;
        TextView selectPasswordLabel;

        TextView selectPasswordPassword;

        LinearLayout selectPasswordItemContainer;

        ViewHolder(View itemView) {
            super(itemView);
            selectPasswordIsim = itemView.findViewById(R.id.selectPasswordIsim);
            selectPasswordSoyisim = itemView.findViewById(R.id.selectPasswordSoyisim);
            selectPasswordPassword = itemView.findViewById(R.id.selectPasswordPassword);
            selectPasswordLabel = itemView.findViewById(R.id.selectPasswordLabel);
            selectPasswordItemContainer = itemView.findViewById(R.id.selectPasswordItemContainer);

        }

    }

    DoorPassword getItem(int id) {
        return arrayList.get(id);
    }

    public ArrayList<DoorPassword> getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList<DoorPassword> arrayList) {
        this.arrayList = arrayList;
    }
}
