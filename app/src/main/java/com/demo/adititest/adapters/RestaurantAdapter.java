package com.demo.adititest.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.demo.adititest.models.PlacesDetails_Modal;
import com.demo.adititest.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Author - Aditi
 * Created on 13/10/20.
 */

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.MyViewHolder> {

    private ArrayList<PlacesDetails_Modal> storeModels;
    private Context context;
    private String current_address;

    private static final int TYPE_HEAD=0;
    private static final int TYPE_LIST=1;
    private String dist;


    public RestaurantAdapter(Context context , ArrayList<PlacesDetails_Modal> storeModels, String current_address, String dist)
    {

        this.context = context;
        this.storeModels = storeModels;
        this.current_address = current_address;
        this.dist = dist;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType==TYPE_LIST) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_listitem, parent, false);

            return new MyViewHolder(itemView,viewType);
        }
        else if(viewType==TYPE_HEAD)
        {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_header, parent, false);

            return new MyViewHolder(itemView,viewType);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {

        if(position==0)
        {
            return TYPE_HEAD;
        }
        else{
            return TYPE_LIST;
        }

    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position)
    {
            if(holder.view_type == TYPE_LIST)
            {
                holder.res_name.setText(storeModels.get(holder.getAdapterPosition()-1).name);

                Glide.with(context)
                        .load(storeModels.get(holder.getAdapterPosition() - 1).photourl)
                        .fitCenter()
                        .override(200,200)
                        .into(holder.res_image);


                holder.res_address.setText(storeModels.get(holder.getAdapterPosition() - 1).address);

                if(storeModels.get(holder.getAdapterPosition() - 1).phone_no == null)
                {
                    holder.res_phone.setText("N/A");
                }
                else  holder.res_phone.setText(storeModels.get(holder.getAdapterPosition() - 1).phone_no);

                holder.res_rating.setText(String.valueOf(storeModels.get(holder.getAdapterPosition() - 1).rating));

                if(dist.equalsIgnoreCase("km"))
                holder.res_distance.setText(storeModels.get(holder.getAdapterPosition() - 1).distance);

                else {
                    String newDist = storeModels.get(holder.getAdapterPosition() - 1).distance.replace(" km","");
                    double distanc = Double.parseDouble(newDist);
                    holder.res_distance.setText(new DecimalFormat("##.##").format(distanc* 0.6)  + " Miles");
                }

                Log.i("details on adapter", storeModels.get(holder.getAdapterPosition()-1).name + "  " +
                       storeModels.get(holder.getAdapterPosition()-1).address +
                        "  " +  storeModels.get(holder.getAdapterPosition() - 1).distance);
            }

            else if (holder.view_type == TYPE_HEAD)
            {
                if(current_address == null)
                {
                    holder.current_location.setText("Unable to Detect Current Location");
                }
                else {
                    holder.current_location.setText("Here you are now,\n" + current_address);
                }
            }
    }

    @Override
    public int getItemCount() {

        return  storeModels.size()+1;

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView res_name;
        TextView res_rating;
        TextView res_address;
        TextView res_phone;
        TextView res_distance;
        TextView current_location;
        ImageView res_image,mImgSetting;

        int view_type;

        public MyViewHolder(final View itemView, final int viewType) {
            super(itemView);

            if(viewType == TYPE_LIST) {

                view_type=1;
                this.res_name = (TextView) itemView.findViewById(R.id.name);
                this.res_rating = (TextView) itemView.findViewById(R.id.rating);
                this.res_address = (TextView) itemView.findViewById(R.id.address);
                this.res_phone = (TextView) itemView.findViewById(R.id.phone);
                this.res_distance = (TextView) itemView.findViewById(R.id.distance);
                this.res_image = (ImageView) itemView.findViewById(R.id.res_image);
            }
          else  if(viewType == TYPE_HEAD){
                view_type = 0;
                this.current_location = (TextView) itemView.findViewById(R.id.location_tv);
                //this.mImgSetting = (ImageView) itemView.findViewById(R.id.img_setting);
            }
        }
    }
}
