package com.app.moviesstreamingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.app.moviesstreamingapp.Model.VideoUploadDetails;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Uri videoUri;
    TextView text_video_selected;
    String videoCategory;
    String videotitle;
    String currentuid;
    StorageReference mstorageRef;
    StorageTask mUploadsTask;
    DatabaseReference referenceVideos;
    EditText video_description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text_video_selected = findViewById(R.id.textvideoselected);
        video_description = findViewById(R.id.movies_description);
        referenceVideos = FirebaseDatabase.getInstance().getReference().child("videos");
        mstorageRef = FirebaseStorage.getInstance().getReference().child("videos");

        Spinner spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        List<String> categories = new ArrayList<>();
        categories.add("Action");
        categories.add("Adventure");
        categories.add("Sports");
        categories.add("Romantic");
        categories.add("Comedy");

        ArrayAdapter<String> dataApter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        dataApter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataApter);

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        videoCategory = adapterView.getItemAtPosition(position).toString();

        Toast.makeText(this, "selected: " + videoCategory, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void openvideoFiles (View view) {
        Intent in = new Intent(Intent.ACTION_GET_CONTENT);
        in.setType("video/*");
        startActivityForResult(in, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 101 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            videoUri = data.getData();

            String path = null;
            Cursor cursor;
            int column_index_data;
            String[] projection = {MediaStore.Video.Media.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media._ID, MediaStore.Video.Thumbnails.DATA};

            final String orderby = MediaStore.Video.Media.DEFAULT_SORT_ORDER;

            cursor = MainActivity.this.getContentResolver().query(videoUri,projection,null, null, orderby);
            column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            while (cursor.moveToNext()) {
                path = cursor.getString(column_index_data);
                videotitle = FilenameUtils.getBaseName(path);
            }
            text_video_selected.setText(videotitle);

        }
    }

    public void uploadFileToFirebase(View v) {
        if(text_video_selected.equals("no video selected")) {
            Toast.makeText(this, "please selected an video!",Toast.LENGTH_SHORT).show();
        } else  {
            uploadFiles();
        }
    }

    public void uploadFiles() {
        if(videoUri != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("video uploading...");
            progressDialog.show();
            final StorageReference storageReference = mstorageRef.child(videotitle);
            mUploadsTask = storageReference.putFile(videoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String video_url = uri.toString();

                            VideoUploadDetails videoUploadDetails = new VideoUploadDetails("", "", "", video_description.getText().toString(),
                                    video_url, videotitle, videoCategory);

                            String uploadsid = referenceVideos.push().getKey();
                            referenceVideos.child(uploadsid).setValue(videoUploadDetails);
                            currentuid = uploadsid;
                            progressDialog.dismiss();
                            if (currentuid.equals(uploadsid)){
                                startThumbnailsActivity();
                            }
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    progressDialog.setMessage("uploaded " + (int)progress + "%...");
                }
            });
        }else {
            Toast.makeText(this, "no video selected to upload",Toast.LENGTH_SHORT).show();
        }
    }
    public void startThumbnailsActivity() {
        Intent in = new Intent(MainActivity.this, UploadThumbnailActivity.class);
        in.putExtra("currentuid", currentuid);
        in.putExtra("thumbnailsName",videotitle);
        startActivity(in);
        Toast.makeText(this, "video uploaded successfully upload video thumbnail!",Toast.LENGTH_LONG).show();
    }
}