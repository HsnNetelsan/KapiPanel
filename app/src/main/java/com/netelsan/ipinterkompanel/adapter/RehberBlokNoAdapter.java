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

public class RehberBlokNoAdapter extends RecyclerView.Adapter<RehberBlokNoAdapter.ViewHolder> {

    Context context;

    public ArrayList<Daire> arrayList;
    private LayoutInflater mInflater;

    public RehberBlokNoAdapter(Context context, ArrayList<Daire> arrayList) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_item_rehber_blok_no, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Daire daire = arrayList.get(position);
        holder.rehberBlokNoItemBlokNo.setText(context.getString(R.string.blok) + " " + daire.getBlok());

        if(daire.isSelected()) {
            holder.rehberBlokNoItemContainer.setBackgroundResource(R.drawable.menu_item_selected);
            holder.rehberBlokNoItemBlokNo.setTextColor(context.getResources().getColor(R.color.black));
        } else {
            holder.rehberBlokNoItemContainer.setBackgroundResource(R.drawable.menu_item_unselected);
            holder.rehberBlokNoItemBlokNo.setTextColor(context.getResources().getColor(R.color.white));
        }


     }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView rehberBlokNoItemBlokNo;

        LinearLayout rehberBlokNoItemContainer;

        ViewHolder(View itemView) {
            super(itemView);
            rehberBlokNoItemBlokNo = itemView.findViewById(R.id.rehberBlokNoItemBlokNo);

            rehberBlokNoItemContainer = itemView.findViewById(R.id.rehberBlokNoItemContainer);

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
