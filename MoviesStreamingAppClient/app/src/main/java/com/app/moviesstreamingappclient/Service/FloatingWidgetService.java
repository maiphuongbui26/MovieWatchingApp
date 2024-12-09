package com.app.moviesstreamingappclient.Service;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.app.moviesstreamingappclient.MoviePlayerActivity;
import com.app.moviesstreamingappclient.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class FloatingWidgetService extends Service {

    WindowManager mWindowManager;
    private View mFloatingWidget;
    Uri videoUri;
    ExoPlayer exoPlayer;
    PlayerView playerView;

    public FloatingWidgetService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String uriStr = intent.getStringExtra("videoUri");
            videoUri = Uri.parse(uriStr);

            if (mWindowManager != null && mFloatingWidget != null && mFloatingWidget.isShown() && exoPlayer != null) {
                mWindowManager.removeView(mFloatingWidget);
                mFloatingWidget = null;
                mWindowManager = null;
                exoPlayer.setPlayWhenReady(false);
                exoPlayer.release();
                exoPlayer = null;
            }

            // Inflate the floating widget layout
            final WindowManager.LayoutParams params;
            mFloatingWidget = LayoutInflater.from(this).inflate(R.layout.custom_pop_up_window, null);

            // Set layout parameters for floating widget
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                params = new WindowManager.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        android.graphics.PixelFormat.TRANSLUCENT
                );
            } else {
                params = new WindowManager.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        android.graphics.PixelFormat.TRANSLUCENT
                );
            }

            params.gravity = Gravity.TOP | Gravity.LEFT;
            params.x = 200;
            params.y = 200;

            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            mWindowManager.addView(mFloatingWidget, params);

            // Initialize ExoPlayer and playerView
            exoPlayer = new ExoPlayer.Builder(this).build();
            playerView = mFloatingWidget.findViewById(R.id.playerView);

            // Handle the maximize button
            ImageView imageViewMaximize = mFloatingWidget.findViewById(R.id.imageviewmaximize);
            imageViewMaximize.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mWindowManager != null && mFloatingWidget.isShown() && exoPlayer != null) {
                        mWindowManager.removeView(mFloatingWidget);
                        mFloatingWidget = null;
                        mWindowManager = null;
                        exoPlayer.setPlayWhenReady(false);
                        exoPlayer.release();
                        exoPlayer = null;

                        stopSelf();

                        Intent openActivityIntent = new Intent(FloatingWidgetService.this, MoviePlayerActivity.class);
                        openActivityIntent.putExtra("videoUri", videoUri.toString());
                        openActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(openActivityIntent);
                    }
                }
            });

            // Handle the close button
            ImageView imageViewClose = mFloatingWidget.findViewById(R.id.imageviewdismiss);
            imageViewClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mWindowManager != null && mFloatingWidget.isShown() && exoPlayer != null) {
                        mWindowManager.removeView(mFloatingWidget);
                        mFloatingWidget = null;
                        mWindowManager = null;
                        exoPlayer.setPlayWhenReady(false);
                        exoPlayer.release();
                        exoPlayer = null;

                        stopSelf();
                    }
                }
            });

            // Set the touch listener to allow dragging of the floating widget
            mFloatingWidget.findViewById(R.id.relatavielayoutcustompopup).setOnTouchListener(new View.OnTouchListener() {
                private int initialX, initialY;
                private float initialTouchX, initialTouchY;

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = motionEvent.getRawX();
                            initialTouchY = motionEvent.getRawY();
                            return true;
                        case MotionEvent.ACTION_UP:
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            params.x = initialX + (int) (motionEvent.getRawX() - initialTouchX);
                            params.y = initialY + (int) (motionEvent.getRawY() - initialTouchY);
                            mWindowManager.updateViewLayout(mFloatingWidget, params);
                            return true;
                    }
                    return false;
                }
            });

            // Start video playback
            playVideos();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void playVideos() {
        try {
            // Prepare the media source
            String playerInfo = Util.getUserAgent(this, "MoviesStreamingAppClient");
            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, playerInfo);
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(videoUri));

            // Assign the player to the PlayerView
            playerView.setPlayer(exoPlayer);

            // Prepare and start playback
            exoPlayer.setMediaSource(mediaSource);
            exoPlayer.prepare();
            exoPlayer.setPlayWhenReady(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingWidget != null) {
            mWindowManager.removeView(mFloatingWidget);
        }
        if (exoPlayer != null) {
            exoPlayer.release();
        }
    }
}
