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
import com.netelsan.ipinterkompanel.model.Guvenlik;

import java.util.ArrayList;

public class GuvenlikRehberAdapter extends RecyclerView.Adapter<GuvenlikRehberAdapter.ViewHolder> {

    Context context;

    public ArrayList<Guvenlik> arrayList;
    private LayoutInflater mInflater;

    public GuvenlikRehberAdapter(Context context, ArrayList<Guvenlik> arrayList) {
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

        Guvenlik guvenlik = arrayList.get(position);

        holder.daireItemIsim.setText(guvenlik.getDeviceName());

        Helper.setIPInfoView(context, holder.daireItemIP);
        holder.daireItemIP.setText(guvenlik.getIp());


        if(guvenlik.isSelected()) {
            holder.daireItemItemContainer.setBackgroundResource(R.drawable.menu_item_selected);
            holder.daireItemIsim.setTextColor(context.getResources().getColor(R.color.black));
            holder.daireItemsSoyisim.setTextColor(context.getResources().getColor(R.color.black));
            holder.daireItemIP.setTextColor(context.getResources().getColor(R.color.black));
        } else {
            holder.daireItemItemContainer.setBackgroundResource(R.drawable.menu_item_unselected);
            holder.daireItemIsim.setTextColor(context.getResources().getColor(R.color.white));
            holder.daireItemsSoyisim.setTextColor(context.getResources().getColor(R.color.white));
            holder.daireItemIP.setTextColor(context.getResources().getColor(R.color.white));
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

        LinearLayout daireItemItemContainer;

        ViewHolder(View itemView) {
            super(itemView);
            daireItemIsim = itemView.findViewById(R.id.daireItemIsim);
            daireItemsSoyisim = itemView.findViewById(R.id.daireItemsSoyisim);
            daireItemIP = itemView.findViewById(R.id.daireItemIP);
            daireItemItemContainer = itemView.findViewById(R.id.daireItemItemContainer);

        }

    }

    Guvenlik getItem(int id) {
        return arrayList.get(id);
    }

    public ArrayList<Guvenlik> getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList<Guvenlik> arrayList) {
        this.arrayList = arrayList;
    }
}
