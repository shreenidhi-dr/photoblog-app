package com.example.photoblog;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.ToolbarWidgetWrapper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {

    private EditText comment_field;
    private Button comment_post_btn;

    private Toolbar commentToolbar;
    private String blog_post_id;
    private String blog_user_id;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private  String current_user_id;

    //Comment Recycle Adapter
    private RecyclerView comment_list;
    private CommentRecylceAdapter commentRecycleAdapter;
    private List<Comments> commentsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        commentToolbar =  findViewById(R.id.comment_toolbar);
        setSupportActionBar(commentToolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        blog_post_id =  getIntent().getStringExtra("blog_post_id");
        blog_user_id = getIntent().getStringExtra("blog_user_id");

        comment_field =  findViewById(R.id.comment_field);
        comment_post_btn = findViewById(R.id.comment_post_btn);

        comment_list =  findViewById(R.id.comment_list);
        commentsList = new ArrayList<>();
        commentRecycleAdapter = new CommentRecylceAdapter(commentsList);
        comment_list.setHasFixedSize(true);
        comment_list.setLayoutManager(new LinearLayoutManager(CommentActivity.this));
        comment_list.setAdapter(commentRecycleAdapter);

        firebaseAuth =  FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        current_user_id = firebaseAuth.getCurrentUser().getUid();

        comment_post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String comment_message =  comment_field.getText().toString();
                if (!TextUtils.isEmpty(comment_message)){

                    Map<String,Object> commentMap =  new HashMap<>();
                    commentMap.put("message",comment_message);
                    commentMap.put("user_id",current_user_id);
                    commentMap.put("timestamp", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("Posts/"+blog_post_id+"/Comments").add(commentMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (!task.isSuccessful()){
                                String error = task.getException().getMessage();
                                Toast.makeText(CommentActivity.this, error,Toast.LENGTH_LONG).show();
                                finish();
                            }
                            else
                            {
                                comment_field.setText("");
                            }
                        }
                    });

                    Map<String, Object> commentNotificationMap = new HashMap<>();
                    commentNotificationMap.put("user_id", firebaseAuth.getCurrentUser().getUid());
                    commentNotificationMap.put("timestamp", FieldValue.serverTimestamp());
                    commentNotificationMap.put("message", "commented your post");
                    commentNotificationMap.put("blog_post_id",blog_post_id);
                    firebaseFirestore.collection("Notification/" + blog_user_id + "/Notification").document().set(commentNotificationMap);
                }
                else
                {
                    Toast.makeText(CommentActivity.this,"Enter Comment",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //RecycleView firebase List
        firebaseFirestore.collection("Posts/"+blog_post_id+"/Comments")
                                                                    .orderBy("timestamp", Query.Direction.ASCENDING)
                                                                    .addSnapshotListener(CommentActivity.this,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent( QuerySnapshot documentSnapshots,FirebaseFirestoreException e) {
                if (!documentSnapshots.isEmpty()) {
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String commentId = doc.getDocument().getId();
                            final Comments comments = doc.getDocument().toObject(Comments.class).withId(commentId);
                            commentsList.add(comments);
                            commentRecycleAdapter.notifyDataSetChanged();

                        }
                    }
                }
            }
        });


    }
}
