package com.app.moviesstreamingappclient.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.app.moviesstreamingappclient.Model.GetVideoDetails;
import com.app.moviesstreamingappclient.Model.MovieItemClickListenerNew;
import com.app.moviesstreamingappclient.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class MoviesShowAdapter extends RecyclerView.Adapter<MoviesShowAdapter.MyViewHolder> {
    private Context mContext;
    private List<GetVideoDetails> uploads;
    MovieItemClickListenerNew movieItemClickListenerNew;

    public MoviesShowAdapter(Context mContext, List<GetVideoDetails> uploads,
                             MovieItemClickListenerNew movieItemClickListenerNew) {
        this.mContext = mContext;
        this.uploads = uploads;
        this.movieItemClickListenerNew = movieItemClickListenerNew;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.movie_item_new, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MoviesShowAdapter.MyViewHolder holder, int position) {
        GetVideoDetails getVideoDetails = uploads.get(position);
        holder.tvTitle.setText(getVideoDetails.getVideo_name());
        Glide.with(mContext).load(getVideoDetails.getVideo_thumb()).into(holder.ImgMovie);
    }

    @Override
    public int getItemCount() {
        return uploads.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageView ImgMovie;
        ConstraintLayout container;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.item_movies_title);
            ImgMovie = itemView.findViewById(R.id.item_movies_img);
            container = itemView.findViewById(R.id.container);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    movieItemClickListenerNew.onMovieClick(uploads.get(getAdapterPosition()),ImgMovie);
                }
            });
        }
    }
}
