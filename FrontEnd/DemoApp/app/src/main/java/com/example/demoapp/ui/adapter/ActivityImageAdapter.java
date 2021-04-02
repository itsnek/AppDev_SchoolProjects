package com.example.demoapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.demoapp.R;
import com.example.demoapp.data.model.Activity;
import com.example.demoapp.data.model.Trip;
import com.example.demoapp.data.model.TripContent;

import java.util.List;

public class ActivityImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private List<TripContent> items;

    public ActivityImageAdapter(List<TripContent> itemList)
    {
        this.items = itemList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        switch (viewType)
        {
            case 0:
                return new ActivityImageAdapter.ActivityViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, null));
            case 1:
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {

        StaggeredGridLayoutManager.LayoutParams layoutParams = new StaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        switch (holder.getItemViewType())
        {
            case 0:
                ActivityViewHolder viewHolder = (ActivityViewHolder)holder;
                Activity item = (Activity)items.get(position);

                viewHolder.title.setText(item.getTitle());
                viewHolder.description.setText(item.getDescription());
                viewHolder.address.setText(item.getAddress());
                viewHolder.type.setText(item.getType());
                viewHolder.tags.setText("Tags: " + item.getTags());
                layoutParams.setFullSpan(true);
                break;

            case 2:

                break;
        }

        holder.itemView.setLayoutParams(layoutParams);
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }

    @Override
    public int getItemViewType(int position)
    {
        return items.get(position).getContentType().ordinal();
    }

    public static class ActivityViewHolder extends RecyclerView.ViewHolder
    {
        private TextView title;
        private TextView description;
        private TextView tags;
        private TextView address;
        private TextView type;

        public ActivityViewHolder(@NonNull View itemView)
        {
            super(itemView);

            title = itemView.findViewById(R.id.Activity_Title);
            description = itemView.findViewById(R.id.Activity_Description);
            tags = itemView.findViewById(R.id.Activity_Tags);
            address = itemView.findViewById(R.id.Activity_Address);
            type = itemView.findViewById(R.id.Activity_Type);

            Button showMore = itemView.findViewById(R.id.Activity_ShowMore);
            Button showLess = itemView.findViewById(R.id.Activity_ShowLess);

            showMore.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    showMore.setVisibility(View.GONE);

                    address.setVisibility(View.VISIBLE);
                    type.setVisibility(View.VISIBLE);
                    tags.setVisibility(View.VISIBLE);
                    showLess.setVisibility(View.VISIBLE);
                }
            });

            showLess.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    showLess.setVisibility(View.GONE);
                    address.setVisibility(View.GONE);
                    type.setVisibility(View.GONE);
                    tags.setVisibility(View.GONE);

                    showMore.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder
    {

        public ImageViewHolder(@NonNull View itemView)
        {
            super(itemView);
        }
    }
}