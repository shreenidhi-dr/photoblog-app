package com.example.photoblog;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class SetupActivity extends AppCompatActivity {

    private CircleImageView setupImage;
    private static  final int GalleryPick = 1;
    private Uri mainImageUri=null;
    private EditText setUpName;
    private Button setUpBtn;
    private String user_id;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private Boolean isChanged = false;

    private ProgressBar setupProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        setupImage = findViewById(R.id.setup_image);

        setUpName = (EditText) findViewById(R.id.setup_name);
        setUpBtn = (Button) findViewById(R.id.setup_btn1);
        setupProgressBar = (ProgressBar) findViewById(R.id.setup_progress);


        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore= FirebaseFirestore.getInstance();
        user_id =  firebaseAuth.getCurrentUser().getUid();
        setUpBtn.setEnabled(false);
        setupProgressBar.setVisibility(View.INVISIBLE);

        setUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String user_name = setUpName.getText().toString();

                if (!TextUtils.isEmpty(user_name) && mainImageUri!=null)

                    setupProgressBar.setVisibility(View.VISIBLE);

                {
                if (isChanged)
                {

                    user_id = firebaseAuth.getCurrentUser().getUid();
                    setupProgressBar.setVisibility(View.VISIBLE);

                    StorageReference image_path = storageReference.child("profile_images").child(user_id+ ".jpg");
                    image_path.putFile(mainImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful())
                            {
                                storeFireestore(task,user_name);

                            }
                            else
                            {
                                String error = task.getException().getMessage();
                                Toast.makeText(SetupActivity.this,"ERROR : " + error,Toast.LENGTH_LONG).show();
                                setupProgressBar.setVisibility(View.INVISIBLE);
                            }

                        }
                    });

                }else {
                    storeFireestore(null,user_name);
                }

            }

            }
        });


        firebaseFirestore.collection("User").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if (task.getResult().exists()){

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");
                        mainImageUri = Uri.parse(image);

                        setUpName.setText(name);
                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.ic_launcher);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(setupImage);
                    }

                }
                else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this,"ERROR : " + error,Toast.LENGTH_LONG).show();

                }
                setUpBtn.setEnabled(true);

            }
        });

        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    if (ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
                    {
                        Toast.makeText(SetupActivity.this,"permission Granted",Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(SetupActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }
                    else{
                        Crop.pickImage(SetupActivity.this);
                    }
                }
            }
        });


    }

    private void storeFireestore( Task<UploadTask.TaskSnapshot> task, String user_name) {
        Uri download_uri;
        if(task!=null) {
            download_uri = task.getResult().getDownloadUrl();
        }
        else
        {
            download_uri = mainImageUri;
        }
        Map<String,String> user_map = new HashMap<>();
        user_map.put("name",user_name);
        user_map.put("image",download_uri.toString());

        firebaseFirestore.collection("User").document(user_id).set(user_map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful())
                {
                    Toast.makeText(SetupActivity.this,"User setting updated ",Toast.LENGTH_LONG).show();
                    startActivity(new Intent(SetupActivity.this,MainActivity.class));
                    setupProgressBar.setVisibility(View.INVISIBLE);
                }
                else
                {
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this,"ERROR : " + error,Toast.LENGTH_LONG).show();
                    setupProgressBar.setVisibility(View.INVISIBLE);
                }
                setupProgressBar.setVisibility(View.INVISIBLE);
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
                setupImage.setImageURI(Crop.getOutput(data));

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
            setupImage.setImageURI(Crop.getOutput(data));
            mainImageUri = Crop.getOutput(data);
            isChanged=true;

        }

    }
}
