package com.example.photoblog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecycleAdapter extends RecyclerView.Adapter<BlogRecycleAdapter.ViewHolder> {

    public List<BlogPost> blog_list;
    public Context context;
    public List<User> user_list;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;


    public BlogRecycleAdapter(List<BlogPost> blog_list,List<User> user_list){
        this.blog_list = blog_list;
        this.user_list = user_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.blog_list_view, viewGroup ,false);
        context = viewGroup.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int x) {

        final int i = x;

        viewHolder.setIsRecyclable(false);

        String desc_data = blog_list.get(i).getDesc();
        viewHolder.setDescText(desc_data);

        String image_uri = blog_list.get(i).getImage_url();
        String thumbnailuri = blog_list.get(i).getThumb();
        viewHolder.setBlogImage(image_uri,thumbnailuri);

        final String blog_user_id = blog_list.get(i).getUser_id();

        final String blogPostId = blog_list.get(i).BlogPostId;

        final String currentUserId =  firebaseAuth.getCurrentUser().getUid();



        if (firebaseAuth.getCurrentUser() != null) {
            String username = user_list.get(i).getName();
            String userImage = user_list.get(i).getImage();
            viewHolder.setUserData(username, userImage);


            //retriving timestamp as a long value to dateformat
            try {
                long milliseconds = blog_list.get(i).getTimestamp().getTime();
                String dateString = DateFormat.format("dd/MM/yyyy", new Date(milliseconds)).toString();
                viewHolder.setTime(dateString);
            } catch (Exception e) {

                //Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
            //Likes on click feature
            viewHolder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (!task.getResult().exists()) {
                                Map<String, Object> likesMap = new HashMap<>();
                                likesMap.put("timestamp", FieldValue.serverTimestamp());
                                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).set(likesMap);

                                //like notification add
                                Map<String, Object> likeNotificationMap = new HashMap<>();
                                likeNotificationMap.put("user_id", firebaseAuth.getCurrentUser().getUid());
                                likeNotificationMap.put("timestamp", FieldValue.serverTimestamp());
                                likeNotificationMap.put("message", "liked your post");
                                likeNotificationMap.put("blog_post_id",blogPostId);
                                firebaseFirestore.collection("Notification/" + blog_user_id + "/Notification").document().set(likeNotificationMap);


                            } else {
                                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).delete();
                            }
                        }
                    });
                }
            });

            //get Like feature
            try{
                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).addSnapshotListener((Activity) context,new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                        if (documentSnapshot.exists()) {

                            viewHolder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_accent));

                        } else {
                            viewHolder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_gray));
                        }
                    }
                });}catch (Exception e){}

            //get Likes Count
            try {
                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").addSnapshotListener((Activity) context, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            int count = queryDocumentSnapshots.size();
                            viewHolder.upadteLikeCount(count);

                        } else {

                            viewHolder.upadteLikeCount(0);

                        }

                    }
                });
            }
            catch (Exception e){}

            try {
                firebaseFirestore.collection("Posts/" + blogPostId + "/Comments").addSnapshotListener((Activity) context, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            int count = queryDocumentSnapshots.size();
                            viewHolder.upadteCommentCount(count);

                        } else {
                            viewHolder.upadteCommentCount(0);
                        }

                    }
                });
            }catch (Exception e){}

            viewHolder.blogCommentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent commentIntent = new Intent(context, CommentActivity.class);
                    commentIntent.putExtra("blog_post_id", blogPostId);
                    commentIntent.putExtra("blog_user_id",blog_user_id);
                    context.startActivity(commentIntent);
                }
            });

            String user_id1 =  blog_list.get(i).getUser_id();
            firebaseFirestore.collection("User").document(user_id1).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        String username = task.getResult().getString("name");
                        String userImage = task.getResult().getString("image");
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private View mView;
        private TextView descView;
        private ImageView blogImageView;
        private TextView blogDate;
        private TextView blogUserName;
        private CircleImageView blogUserImage;
        private ImageView blogLikeBtn;
        private TextView blogLikeCount;
        private ImageView blogCommentBtn;
        private TextView commentCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            blogLikeBtn = mView.findViewById(R.id.blog_like_btn_photo);
            blogCommentBtn =  mView.findViewById(R.id.blog_comment_btn);



        }

        public  void setDescText (String descText){
            descView =  mView.findViewById(R.id.blog_desc_photo);
            descView.setText(descText);
        }

        public void setBlogImage(String downloadUri,String thumburi){

            blogImageView = mView.findViewById(R.id.blog_image_photo);

            RequestOptions placeHolder =  new RequestOptions();
            placeHolder.placeholder(R.drawable.ic_launcher);

            Glide.with(context).applyDefaultRequestOptions(placeHolder).load(downloadUri)
                    .thumbnail(Glide.with(context).load(thumburi))
                    .into(blogImageView);

        }
        public void setTime(String date){
            blogDate =  mView.findViewById(R.id.blog_date);
            blogDate.setText(date);

        }

        public void setUserData(String name,String image)
        {
            blogUserImage =  mView.findViewById(R.id.blog_user_image_photo);
            blogUserName =  mView.findViewById(R.id.blog_user_name_photo);

            blogUserName.setText(name);
            RequestOptions placeHolder = new RequestOptions();
            placeHolder.placeholder(R.drawable.ic_launcher);
            Glide.with(context).applyDefaultRequestOptions(placeHolder).load(image).into(blogUserImage);
        }

        public void upadteLikeCount(int count)
        {
            blogLikeCount = mView.findViewById(R.id.blog_like_count);
            blogLikeCount.setText(count+ " Likes");
        }
        public void upadteCommentCount(int count)
        {
            commentCount = mView.findViewById(R.id.blog_comment_count);
            commentCount.setText(count+ " Comments");
        }
    }

}