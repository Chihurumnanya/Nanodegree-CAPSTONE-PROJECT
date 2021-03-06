package com.chihurumnanya.itransmit.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.chihurumnanya.itransmit.GlideApp;
import com.chihurumnanya.itransmit.R;
import com.chihurumnanya.itransmit.models.TravelerModel;
import com.chihurumnanya.itransmit.utils.Constants;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Adapter for Traveler items inside trip factory
 */
public class TravelerAdapter extends RecyclerView.Adapter<TravelerAdapter.TravelerHolder> {

    private Cursor mCursor;
    private Context mContext;

    public TravelerAdapter(Cursor cursor) {
        mCursor = cursor;
    }

    @Override
    public TravelerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.traveler_item, parent, false);

        return new TravelerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final TravelerHolder holder, int position) {
        if (mCursor == null) {
            return;
        }

        mCursor.moveToPosition(position);
        final TravelerModel traveler = new TravelerModel(mCursor);

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, TravelerModel> travelerHashMap = null;
                if (mContext instanceof OnTravelerAdapter) {
                    travelerHashMap = ((OnTravelerAdapter) mContext).getTravelerHashMap();
                }

                if (travelerHashMap != null) {

                    if (travelerHashMap.get(traveler.getContactId()) != null) {
                        //Selected
                        travelerHashMap.remove(traveler.getContactId());
                        ((OnTravelerAdapter) mContext).setTravelerHashMap(travelerHashMap);
                        holder.rootView.setBackgroundColor(Color.TRANSPARENT);
                    } else {
                        if (travelerHashMap.size() < Constants.General.MAX_TRAVELERS) {
                            //Not selected
                            travelerHashMap.put(traveler.getContactId(), traveler);
                            ((OnTravelerAdapter) mContext).setTravelerHashMap(travelerHashMap);
                            holder.rootView.setBackgroundColor(Color.LTGRAY);
                        } else {
                            Toast.makeText(mContext,
                                    R.string.reached_maximum_number_of_travelers,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        HashMap<String, TravelerModel> travelerHashMap = null;
        if (mContext instanceof OnTravelerAdapter) {
            travelerHashMap = ((OnTravelerAdapter) mContext).getTravelerHashMap();
        }

        if (travelerHashMap != null && travelerHashMap.get(traveler.getContactId()) != null) {
            holder.rootView.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.rootView.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.travelerName.setText(traveler.getName());

        GlideApp
                .with(mContext)
                .load(traveler.getPhotoUri())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.ic_default_traveler_photo)
                .error(R.drawable.ic_default_traveler_photo)
                .into(holder.travelerPhoto);
    }

    @Override
    public int getItemCount() {
        return (mCursor != null) ? mCursor.getCount() : 0;
    }

    public TravelerModel getItem(int position) {
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Invalid item position requested");
        }

        return new TravelerModel(mCursor);
    }

    @Override
    public long getItemId(int position) {
        try {
            return Long.parseLong(getItem(position).getContactId());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    public void swapCursor(Cursor cursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public interface OnTravelerAdapter {
        HashMap<String, TravelerModel> getTravelerHashMap();

        void setTravelerHashMap(HashMap<String, TravelerModel> travelerHashMap);
    }

    public class TravelerHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.traveler_item)
        protected View rootView;

        @BindView(R.id.traveler_photo)
        protected CircleImageView travelerPhoto;

        @BindView(R.id.traveler_name)
        protected TextView travelerName;

        public TravelerHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
