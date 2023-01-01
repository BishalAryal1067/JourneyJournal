package com.example.journeyjournal.Adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.journeyjournal.JournalDetailsActivity;
import com.example.journeyjournal.Model.JournalModel;
import com.example.journeyjournal.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

public class JournalAdapter extends FirebaseRecyclerAdapter<JournalModel, JournalAdapter.MyViewHolder> {
    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public JournalAdapter(@NonNull FirebaseRecyclerOptions<JournalModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull JournalModel model) {
        holder.recyclerJournalTitle.setText(model.getTitle());
        holder.recyclerJournalDate.setText(model.getDate());
        holder.recyclerJournalLocation.setText(model.getLocation());
        Glide.with(holder.recyclerJournalImage.getContext()).load(model.getImage()).into(holder.recyclerJournalImage);


        //forwarding data to journal details page
        holder.recyclerJournalCard.setOnClickListener(view -> view.getContext().startActivity(new Intent(view.getContext(), JournalDetailsActivity.class)
                .putExtra("image", model.getImage())
                .putExtra("title", model.getTitle())
                .putExtra("date", model.getDate())
                .putExtra("location", model.getLocation())
                .putExtra("description", model.getDescription())
                .putExtra("position", getRef(position).getKey())
                .putExtra("latitude", model.getLocationLatitude())
                .putExtra("longitude", model.getLocationLongitude())
        ));
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new MyViewHolder(view);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView recyclerJournalCard;
        AppCompatImageView recyclerJournalImage;
        AppCompatTextView recyclerJournalTitle, recyclerJournalDate, recyclerJournalLocation;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            recyclerJournalCard = itemView.findViewById(R.id.recyclerCard);
            recyclerJournalTitle = itemView.findViewById(R.id.recyclerJournalTitle);
            recyclerJournalDate = itemView.findViewById(R.id.recyclerJournalDate);
            recyclerJournalLocation = itemView.findViewById(R.id.recyclerJournalLocation);
            recyclerJournalImage = itemView.findViewById(R.id.recyclerJournalImage);

        }
    }
}
