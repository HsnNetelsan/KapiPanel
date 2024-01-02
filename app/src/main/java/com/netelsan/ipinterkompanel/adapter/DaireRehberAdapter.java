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

public class DaireRehberAdapter extends RecyclerView.Adapter<DaireRehberAdapter.ViewHolder> {

    Context context;

    public ArrayList<Daire> arrayList;
    private LayoutInflater mInflater;

    public DaireRehberAdapter(Context context, ArrayList<Daire> arrayList) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_item_rehber, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Daire daire = arrayList.get(position);

        holder.daireItemIsim.setText(daire.getIsim());

        Helper.setIPInfoView(context, holder.daireItemIP);
        holder.daireItemIP.setText(daire.getIp());

        holder.daireItemKatNo.setText(context.getString(R.string.kat_no) + daire.getKatNo() + "");

        boolean isGorevli = daire.isGorevli();
        if(isGorevli) {
            holder.daireItemsSoyisim.setText(daire.getSoyisim() + " (" + context.getString(R.string.gorevli) + ")");
        } else {
            holder.daireItemsSoyisim.setText(daire.getSoyisim());
        }

        if(daire.isSelected()) {
            holder.daireItemItemContainer.setBackgroundResource(R.drawable.menu_item_selected);
            holder.daireItemIsim.setTextColor(context.getResources().getColor(R.color.black));
            holder.daireItemsSoyisim.setTextColor(context.getResources().getColor(R.color.black));
            holder.daireItemIP.setTextColor(context.getResources().getColor(R.color.black));
            holder.daireItemKatNo.setTextColor(context.getResources().getColor(R.color.black));
        } else {
            holder.daireItemItemContainer.setBackgroundResource(R.drawable.menu_item_unselected);
            holder.daireItemIsim.setTextColor(context.getResources().getColor(R.color.white));
            holder.daireItemsSoyisim.setTextColor(context.getResources().getColor(R.color.white));
            holder.daireItemIP.setTextColor(context.getResources().getColor(R.color.white));
            holder.daireItemKatNo.setTextColor(context.getResources().getColor(R.color.white));
        }

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView daireItemIsim;
        TextView daireItemsSoyisim;

        TextView daireItemIP;
        TextView daireItemKatNo;

        LinearLayout daireItemItemContainer;

        ViewHolder(View itemView) {
            super(itemView);
            daireItemIsim = itemView.findViewById(R.id.daireItemIsim);
            daireItemsSoyisim = itemView.findViewById(R.id.daireItemsSoyisim);
            daireItemIP = itemView.findViewById(R.id.daireItemIP);
            daireItemKatNo = itemView.findViewById(R.id.daireItemKatNo);

            daireItemItemContainer = itemView.findViewById(R.id.daireItemItemContainer);

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
