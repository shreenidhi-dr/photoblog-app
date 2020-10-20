package com.example.photoblog;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.soundcloud.android.crop.Crop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    private Toolbar newPostToolbar;
    private EditText newPostDesc;
    private ImageView newPostImage;
    private Button newPostBtn;

    private Uri postImageUri=null;
    private Boolean isChanged;
    private ProgressBar newPostProgress;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private  String current_user_id;
    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        newPostToolbar = findViewById(R.id.new_post_toolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true );

        newPostImage = findViewById(R.id.new_post_image);
        newPostBtn =  findViewById(R.id.post_btn);
        newPostDesc = findViewById(R.id.new_post_desc);
        newPostProgress = findViewById(R.id.new_post_progressbar);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        current_user_id = firebaseAuth.getCurrentUser().getUid();

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    if (ContextCompat.checkSelfPermission(NewPostActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
                    {
                        Toast.makeText(NewPostActivity.this,"permission Denied",Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(NewPostActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }
                    else{
                        Crop.pickImage(NewPostActivity.this);
                        //Crop.of(null,postImageUri).withAspect(1,2).pickImage(NewPostActivity.this);
                    }
                }
            }
        });

        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String desc = newPostDesc.getText().toString();

                if (!TextUtils.isEmpty(desc) && postImageUri !=null){

                    newPostProgress.setVisibility(View.VISIBLE);
                    final String randomName = UUID.randomUUID().toString();

                    StorageReference filepath = storageReference.child("post_images").child(randomName + ".jpg");


                    filepath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                            final String DownloadUri = task.getResult().getDownloadUrl().toString();

                            if (task.isSuccessful()){

                                File newImageFile = new File(postImageUri.getPath());

                                try {
                                    compressedImageFile = new Compressor( NewPostActivity.this).setMaxHeight(100).setMaxWidth(100)
                                            .setQuality(5).compressToBitmap(newImageFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] ThumData = baos.toByteArray();


                                UploadTask uploadTask = storageReference.child("post_images/thumbs")
                                                            .child(randomName + ".jpg")
                                                            .putBytes(ThumData);

                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        String downloadthumbUri = taskSnapshot.getDownloadUrl().toString();

                                        Map<String,Object> postMap = new HashMap<>();
                                        postMap.put("image_url",DownloadUri);
                                        postMap.put("thumb",downloadthumbUri);
                                        postMap.put("desc",desc);
                                        postMap.put("user_id",current_user_id);
                                        postMap.put("timestamp",FieldValue.serverTimestamp());

                                        firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if (task.isSuccessful()){



                                                    Toast.makeText(NewPostActivity.this,"Post was added",Toast.LENGTH_LONG).show();
                                                    startActivity(new Intent(NewPostActivity.this,MainActivity.class));
                                                    finish();
                                                }else
                                                {
                                                    newPostProgress.setVisibility(View.INVISIBLE);
                                                    String error = task.getException().getMessage();
                                                    Toast.makeText(NewPostActivity.this,"Error : "+ error,Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });



                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });


                            }
                            else
                            {
                                newPostProgress.setVisibility(View.INVISIBLE);
                                String error = task.getException().getMessage();
                                Toast.makeText(NewPostActivity.this,"Error : "+ error,Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode==RESULT_OK){
            if (requestCode==Crop.REQUEST_PICK)
            {
                Uri source_uri =  data.getData();
                Uri destination_uri = Uri.fromFile(new File(getCacheDir(),"cropped"));
                Crop.of(source_uri,destination_uri).asSquare().start(this);
                newPostImage.setImageURI(Crop.getOutput(data));

            }
            else if(requestCode==Crop.REQUEST_CROP)
            {
                handle_crop(resultCode,data);
            }
        }


    }

    private void handle_crop(int code, Intent data) {
        if(code==RESULT_OK)
        {
            newPostImage.setImageURI(Crop.getOutput(data));
            postImageUri = Crop.getOutput(data);
            isChanged=true;

        }

    }
}
