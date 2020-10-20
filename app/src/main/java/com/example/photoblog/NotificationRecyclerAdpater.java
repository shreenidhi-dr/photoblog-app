package com.example.photoblog;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationRecyclerAdpater extends RecyclerView.Adapter<NotificationRecyclerAdpater.ViewHolder> {

    public List<Notification> notification_list;
    public Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    public String imageUri;

    public NotificationRecyclerAdpater(List<Notification> notification_list){
        this.notification_list = notification_list;
    }

    @NonNull
    @Override
    public NotificationRecyclerAdpater.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notification_list_view,viewGroup,false);
        context = viewGroup.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth =  FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NotificationRecyclerAdpater.ViewHolder viewHolder, int i) {
        String user_id = notification_list.get(i).getUser_id();

        firebaseFirestore.collection("User").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()){

                    String user_name = task.getResult().getString("name");
                    String userImage = task.getResult().getString("image");
                    viewHolder.setUserData(userImage,user_name);
                }
            }
        });

        String message = notification_list.get(i).getMessage();
        viewHolder.setNotification_text(message);
        final String blogPostId = notification_list.get(i).getBlog_post_id();

        firebaseFirestore.collection("Posts").document(blogPostId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    String thumb_image_uri = task.getResult().getString("thumb");
                    viewHolder.setPost_image(thumb_image_uri);
                    imageUri = task.getResult().getString("image_uri");

                }
            }
        });

        viewHolder.post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent PhotoViewIntent = new Intent(context,PhotoView.class);
                PhotoViewIntent.putExtra("image_uri",imageUri);
                PhotoViewIntent.putExtra("blogPostId",blogPostId);
                PhotoViewIntent.putExtra("user_id",firebaseAuth.getCurrentUser().getUid());
                context.startActivity(PhotoViewIntent);
            }
        });
    }



    @Override
    public int getItemCount() {
        return notification_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView notification_text;
        private View mView;
        private TextView user_name;
        private CircleImageView user_image;
        private ImageView post_image;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView =itemView;

            post_image= mView.findViewById(R.id.notificiation_post_image);
        }

        public void setNotification_text(String text){
            notification_text =  mView.findViewById(R.id.notification_text);
            notification_text.setText(text);
        }
        public void setUserData(String image,String name)
        {
            user_name = mView.findViewById(R.id.notification_text_username);
            user_name.setText(name);
            user_image = mView.findViewById(R.id.notification_blog_image);
            RequestOptions placeHolder = new RequestOptions();
            placeHolder.placeholder(R.drawable.ic_launcher);
            Glide.with(context).applyDefaultRequestOptions(placeHolder).load(image).into(user_image);
        }
        public void setPost_image(String image){
            post_image = mView.findViewById(R.id.notificiation_post_image);
            RequestOptions placeHolder = new RequestOptions();
            placeHolder.placeholder(R.drawable.ic_launcher);
            Glide.with(context).applyDefaultRequestOptions(placeHolder).load(image).into(post_image);

        }
    }
 }
