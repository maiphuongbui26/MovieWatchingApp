package com.app.moviesstreamingappclient;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.androidquery.util.Progress;
import com.app.moviesstreamingappclient.Adapter.MoviesShowAdapter;
import com.app.moviesstreamingappclient.Adapter.SliderPagerAdapterNew;
import com.app.moviesstreamingappclient.Model.GetVideoDetails;
import com.app.moviesstreamingappclient.Model.MovieItemClickListenerNew;
import com.app.moviesstreamingappclient.Model.SliderSide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements MovieItemClickListenerNew {

    MoviesShowAdapter moviesShowAdapter;
    DatabaseReference mDatabaserefence;
    private List<GetVideoDetails> uploads,uploadslistLatests,uploadsListPopular;
    private List<GetVideoDetails> actionsmovies, sportsmovies,
            adventuredmovies, romanticmovies, comedymovies;
    private ViewPager sliderPager;
    private List<SliderSide> uploadsSlider;
    private TabLayout indicator, tabmoviesactions;
    private RecyclerView MoviesRv,moviesRvWeek,tab;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(R.layout.actionbar);
        }

        // Khởi tạo ProgressDialog
        progressDialog = new ProgressDialog(this);

        inViews();
        addAllMovies();
        inWeekMovies();
        inPopularMovies();
        moviesViewTab();
    }

    private void addAllMovies() {
        // Khởi tạo danh sách
        uploads = new ArrayList<>();
        uploadslistLatests = new ArrayList<>();
        uploadsListPopular = new ArrayList<>();
        actionsmovies = new ArrayList<>();
        adventuredmovies = new ArrayList<>();
        comedymovies = new ArrayList<>();
        sportsmovies = new ArrayList<>();
        romanticmovies = new ArrayList<>();
        uploadsSlider = new ArrayList<>();

        mDatabaserefence = FirebaseDatabase.getInstance().getReference("videos");
        progressDialog.setMessage("loading...");
        progressDialog.show();

        mDatabaserefence.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    GetVideoDetails upload = postSnapshot.getValue(GetVideoDetails.class);
                    SliderSide slide = postSnapshot.getValue(SliderSide.class);

                    // Kiểm tra giá trị null
                    if (upload != null) {
                        if (upload.getVideo_type().equals("latest movies")) {
                            uploadslistLatests.add(upload);
                        }
                        if (upload.getVideo_type().equals("best popular movies")) {
                            uploadsListPopular.add(upload);
                        }
                        // Phân loại phim
                        switch (upload.getVideo_category()) {
                            case "Action":
                                actionsmovies.add(upload);
                                break;
                            case "Adventure":
                                adventuredmovies.add(upload);
                                break;
                            case "Comedy":
                                comedymovies.add(upload);
                                break;
                            case "Romantic":
                                romanticmovies.add(upload);
                                break;
                            case "Sports":
                                sportsmovies.add(upload);
                                break;
                        }
                        if (upload.getVideo_type().equals("Slide movies")) {
                            uploadsSlider.add(slide);
                        }
                        uploads.add(upload);
                    }
                }
                inSlider();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi ở đây
                progressDialog.dismiss();
            }
        });
    }

    // Phương thức cập nhật danh sách phim
    private void updateMoviesList(List<GetVideoDetails> moviesList) {
        moviesShowAdapter = new MoviesShowAdapter(this, moviesList, this);
        tab.setAdapter(moviesShowAdapter);
        tab.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        moviesShowAdapter.notifyDataSetChanged();
    }

    private void inSlider() {
        SliderPagerAdapterNew adapterNew = new SliderPagerAdapterNew(this,uploadsSlider);
        sliderPager.setAdapter(adapterNew);
        adapterNew.notifyDataSetChanged();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new SliderTimer(), 4000, 6000);
        indicator.setupWithViewPager(sliderPager,true);
    }

    private void inWeekMovies(){
        moviesShowAdapter = new MoviesShowAdapter(this,uploadslistLatests,this);
        moviesRvWeek.setAdapter(moviesShowAdapter);
        moviesRvWeek.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,false));
        moviesShowAdapter.notifyDataSetChanged();
    }

    private void inPopularMovies(){
        moviesShowAdapter = new MoviesShowAdapter(this,uploadsListPopular,this);
        MoviesRv.setAdapter(moviesShowAdapter);
        MoviesRv.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,false));
        moviesShowAdapter.notifyDataSetChanged();
    }

    private void moviesViewTab(){
        getActionMovies();
        tabmoviesactions.addTab(tabmoviesactions.newTab().setText("Action"));
        tabmoviesactions.addTab(tabmoviesactions.newTab().setText("Adventure"));
        tabmoviesactions.addTab(tabmoviesactions.newTab().setText("Comedy"));
        tabmoviesactions.addTab(tabmoviesactions.newTab().setText("Romantic"));
        tabmoviesactions.setTabGravity(TabLayout.GRAVITY_FILL);
        tabmoviesactions.setTabTextColors(ColorStateList.valueOf(Color.WHITE));
        tabmoviesactions.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case 0:
                        getActionMovies();
                        break;
                    case 1:
                        getAdventureMovies();
                        break;
                    case 2:
                        getComedyMovies();
                        break;
                    case 3:
                        getRomanticMovies();
                        break;
                    case 4:
                        getSportMovies();
                        break;
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case 0:
                        break;
                    case 1:
                        break;
                }
            }

            public void onTabReselected(TabLayout.Tab tab){

            }
        });

    }
    private void inViews(){
        tabmoviesactions = findViewById(R.id.tabActionMovies);
        sliderPager = findViewById(R.id.slider_pager);
        indicator = findViewById(R.id.indicator);
        moviesRvWeek = findViewById(R.id.rv_movies_week);
        MoviesRv = findViewById(R.id.Rv_movies);
        tab = findViewById(R.id.tabrecyler);


    }

    @Override
    public void onMovieClick(GetVideoDetails movie, ImageView imageView) {
        Intent in = new Intent(this,MovieDetailsActivity.class);
        in.putExtra("title",movie.getVideo_name());
        in.putExtra("imgURL",movie.getVideo_thumb());
        in.putExtra("imgCover",movie.getVideo_thumb());
        in.putExtra("movieDetails",movie.getVideo_description());
        in.putExtra("movieUrl",movie.getVideo_url());
        in.putExtra("movieCategory",movie.getVideo_category());
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this,
                imageView,"shareName");
        startActivity(in,options.toBundle());

    }

    public class SliderTimer extends TimerTask {
        public void run(){
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (sliderPager.getCurrentItem() <uploadsSlider.size() - 1){
                        sliderPager.setCurrentItem(sliderPager.getCurrentItem()+1);
                    }else {
                        sliderPager.setCurrentItem(0);
                    }
                }
            });
        }
    }

    private void getActionMovies(){
        moviesShowAdapter = new MoviesShowAdapter(this,actionsmovies,this);
        tab.setAdapter(moviesShowAdapter);
        tab.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,false));
        moviesShowAdapter.notifyDataSetChanged();
    }

    private void getSportMovies(){
        moviesShowAdapter = new MoviesShowAdapter(this,sportsmovies,this);
        tab.setAdapter(moviesShowAdapter);
        tab.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,false));
        moviesShowAdapter.notifyDataSetChanged();
    }

    private void getRomanticMovies(){
        moviesShowAdapter = new MoviesShowAdapter(this,romanticmovies,this);
        tab.setAdapter(moviesShowAdapter);
        tab.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,false));
        moviesShowAdapter.notifyDataSetChanged();
    }

    private void getComedyMovies(){
        moviesShowAdapter = new MoviesShowAdapter(this,comedymovies,this);
        tab.setAdapter(moviesShowAdapter);
        tab.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,false));
        moviesShowAdapter.notifyDataSetChanged();
    }

    private void getAdventureMovies(){
        moviesShowAdapter = new MoviesShowAdapter(this,adventuredmovies,this);
        tab.setAdapter(moviesShowAdapter);
        tab.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,false));
        moviesShowAdapter.notifyDataSetChanged();
    }
}
