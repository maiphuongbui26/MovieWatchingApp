package com.app.moviesstreamingappclient;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.moviesstreamingappclient.Adapter.MoviesShowAdapter;
import com.app.moviesstreamingappclient.Model.GetVideoDetails;
import com.app.moviesstreamingappclient.Model.MovieItemClickListenerNew;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MovieDetailsActivity extends AppCompatActivity implements MovieItemClickListenerNew {

    private ImageView MoviesThumbnail, MoviesCoverImg;
    private TextView tv_title, tv_description;
    private FloatingActionButton play_fab;
    private RecyclerView recyclerView_similarMovies;
    private MoviesShowAdapter moviesShowAdapter;
    private DatabaseReference mDatabaseReference;
    private List<GetVideoDetails> actionMovies, sportMovies, comedyMovies, romanticMovies, adventureMovies;
    private String current_video_url, current_video_category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        initViews();
        initializeMovieLists();
        fetchSimilarMovies();

        play_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MovieDetailsActivity.this, MoviePlayerActivity.class);
                intent.putExtra("videoUrl", current_video_url);
                startActivity(intent);
            }
        });
    }

    private void initViews() {
        play_fab = findViewById(R.id.play_fab);
        tv_title = findViewById(R.id.detail_movie_title);
        tv_description = findViewById(R.id.detail_movie_desc);
        MoviesThumbnail = findViewById(R.id.detail_movie_img);
        MoviesCoverImg = findViewById(R.id.detail_movie_cover);
        recyclerView_similarMovies = findViewById(R.id.recycler_similar_movies);

        String moviesTitle = getIntent().getExtras().getString("title");
        String imgResourceId = getIntent().getExtras().getString("imgURL");
        String imageCover = getIntent().getExtras().getString("imgCover");
        String moviesDetailsText = getIntent().getExtras().getString("movieDetails");
        String moviesUrl = getIntent().getExtras().getString("movieUrl");
        String moviesCategory = getIntent().getExtras().getString("movieCategory");

        current_video_url = moviesUrl;
        current_video_category = moviesCategory;

        Glide.with(this).load(imgResourceId).into(MoviesThumbnail);
        Glide.with(this).load(imageCover).into(MoviesCoverImg);
        tv_title.setText(moviesTitle);
        tv_description.setText(moviesDetailsText);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(moviesTitle);
        }
    }

    private void initializeMovieLists() {
        actionMovies = new ArrayList<>();
        sportMovies = new ArrayList<>();
        comedyMovies = new ArrayList<>();
        romanticMovies = new ArrayList<>();
        adventureMovies = new ArrayList<>();
    }

    private void fetchSimilarMovies() {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("video");
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    GetVideoDetails movie = postSnapshot.getValue(GetVideoDetails.class);
                    if (movie != null) {
                        categorizeMovie(movie);
                    }
                }
                displaySimilarMovies();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if necessary
            }
        });
    }

    private void categorizeMovie(GetVideoDetails movie) {
        switch (movie.getVideo_category()) {
            case "Action":
                actionMovies.add(movie);
                break;
            case "Sport":
                sportMovies.add(movie);
                break;
            case "Adventure":
                adventureMovies.add(movie);
                break;
            case "Comedy":
                comedyMovies.add(movie);
                break;
            case "Romantic":
                romanticMovies.add(movie);
                break;
        }
    }

    private void displaySimilarMovies() {
        List<GetVideoDetails> selectedMoviesList = getMoviesByCategory(current_video_category);
        if (selectedMoviesList != null) {
            moviesShowAdapter = new MoviesShowAdapter(this, selectedMoviesList, this);
            recyclerView_similarMovies.setAdapter(moviesShowAdapter);
            recyclerView_similarMovies.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            moviesShowAdapter.notifyDataSetChanged();
        }
    }

    private List<GetVideoDetails> getMoviesByCategory(String category) {
        switch (category) {
            case "Action":
                return actionMovies;
            case "Sport":
                return sportMovies;
            case "Adventure":
                return adventureMovies;
            case "Comedy":
                return comedyMovies;
            case "Romantic":
                return romanticMovies;
            default:
                return null;
        }
    }

    @Override
    public void onMovieClick(GetVideoDetails movie, ImageView imageView) {
        tv_title.setText(movie.getVideo_name());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(movie.getVideo_name());
        }
        Glide.with(this).load(movie.getVideo_thumb()).into(MoviesThumbnail);
        Glide.with(this).load(movie.getVideo_thumb()).into(MoviesCoverImg);
        tv_description.setText(movie.getVideo_description());
        current_video_url = movie.getVideo_url();
        current_video_category = movie.getVideo_category();

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, imageView, "shareName");
        options.toBundle();
    }
}
