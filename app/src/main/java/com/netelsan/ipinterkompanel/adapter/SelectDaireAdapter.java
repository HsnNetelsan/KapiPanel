package com.netelsan.ipinterkompanel.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.model.Daire;

import java.util.ArrayList;

public class SelectDaireAdapter extends RecyclerView.Adapter<SelectDaireAdapter.ViewHolder> {

    Context context;

    public ArrayList<Daire> arrayList;
    private LayoutInflater mInflater;

    public SelectDaireAdapter(Context context, ArrayList<Daire> arrayList) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_item_select_daire, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Daire daire = arrayList.get(position);

        holder.selectDaireIsim.setText(daire.getIsim());
        holder.selectDaireSoyisim.setText(daire.getSoyisim());

        Helper.setIPInfoView(context, holder.selectDaireIP);
        holder.selectDaireIP.setText(daire.getIp());

        if(daire.isSelected()) {
            holder.selectDaireItemContainer.setBackgroundResource(R.drawable.menu_item_selected);
            holder.selectDaireIsim.setTextColor(context.getResources().getColor(R.color.black));
            holder.selectDaireSoyisim.setTextColor(context.getResources().getColor(R.color.black));
            holder.selectDaireIP.setTextColor(context.getResources().getColor(R.color.black));
        } else {
            holder.selectDaireItemContainer.setBackgroundResource(R.drawable.menu_item_unselected);
            holder.selectDaireIsim.setTextColor(context.getResources().getColor(R.color.white));
            holder.selectDaireSoyisim.setTextColor(context.getResources().getColor(R.color.white));
            holder.selectDaireIP.setTextColor(context.getResources().getColor(R.color.white));
        }

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView selectDaireIsim;
        TextView selectDaireSoyisim;

        TextView selectDaireIP;

        LinearLayout selectDaireItemContainer;

        ViewHolder(View itemView) {
            super(itemView);
            selectDaireIsim = itemView.findViewById(R.id.selectDaireIsim);
            selectDaireSoyisim = itemView.findViewById(R.id.selectDaireSoyisim);
            selectDaireIP = itemView.findViewById(R.id.selectDaireIP);
            selectDaireItemContainer = itemView.findViewById(R.id.selectDaireItemContainer);

        }

    }

    Daire getItem(int id) {
        return arrayList.get(id);
    }

    public ArrayList<Daire> getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList<Daire> arrayList) {
        this.arrayList = arrayList;
    }
}
