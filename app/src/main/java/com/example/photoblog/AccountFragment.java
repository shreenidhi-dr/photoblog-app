package com.example.photoblog;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {

    private TextView profile_user_name;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;
    private Uri mainImageUri=null;
    private CircleImageView profile_user_image;
    private TextView profile_user_email;

    private RecyclerView blog_list_view;
    private List<BlogPost> blog_list;
    private  BlogRecycleAdapter blogRecycleAdapter;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageisLoaded = true;
    public  List<User> user_list;


    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) throws IndexOutOfBoundsException {
        blog_list =  new ArrayList<>();
        user_list =  new ArrayList<>();

        View view = inflater.inflate(R.layout.fragment_account1, container, false);

        blog_list_view = view.findViewById(R.id.profile_blog_posts);
        blogRecycleAdapter = new BlogRecycleAdapter(blog_list,user_list);

        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecycleAdapter);

        profile_user_name = view.findViewById(R.id.profile_user_name);
        profile_user_image= view.findViewById(R.id.profile_user_image);
        profile_user_email =  view.findViewById(R.id.profile_user_email);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore =  FirebaseFirestore.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore.collection("User").document(user_id).get().addOnCompleteListener(getActivity(),new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    String user_name =  task.getResult().getString("name");
                    String image =  task.getResult().getString("image");
                    profile_user_name.setText(user_name);

                    RequestOptions placeholderRequest = new RequestOptions();
                    placeholderRequest.placeholder(R.drawable.ic_launcher);
                    Glide.with(getActivity()).setDefaultRequestOptions(placeholderRequest).load(image).into(profile_user_image);
                }
            }
        });
        String userEmail =  firebaseAuth.getCurrentUser().getEmail();
        profile_user_email.setText(userEmail);

        if (firebaseAuth.getCurrentUser() !=null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom =  !recyclerView.canScrollVertically(+1);

                    if (reachedBottom){

                        loadMorePost();

                    }

                }
            });

            Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp",Query.Direction.DESCENDING).limit(3);

            firstQuery.addSnapshotListener(Objects.requireNonNull(getActivity()),new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (!documentSnapshots.isEmpty()) {

                        //if (isFirstPageisLoaded)
                        {
                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                            blog_list.clear();
                            user_list.clear();
                        }

                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String blogPostId = doc.getDocument().getId();
                                final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);

                                final String blogUserId =  doc.getDocument().getString("user_id");
                                firebaseFirestore.collection("User").document(Objects.requireNonNull(blogUserId)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (user_id.equals(blogUserId))
                                        {
                                        if (task.isSuccessful()){

                                            User user =  task.getResult().toObject(User.class);

                                            if (isFirstPageisLoaded)
                                            {
                                                user_list.add(0,user);
                                                blog_list.add(0, blogPost);
                                            }
                                            else
                                            {
                                                blog_list.add(blogPost);
                                                user_list.add(user);
                                            }

                                            blogRecycleAdapter.notifyDataSetChanged();
                                        }

                                        }
                                    }
                                });
                            }
                        }
                        isFirstPageisLoaded = false;
                    }
                }
            });
        }
        // Inflate the layout for this fragment
        return view;
    }

    public void loadMorePost(){
        Query nextQuery = firebaseFirestore.collection("Posts").orderBy("timestamp",Query.Direction.DESCENDING).startAfter(lastVisible).limit(3);

        nextQuery.addSnapshotListener(Objects.requireNonNull(getActivity()),new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (!documentSnapshots.isEmpty())
                {
                    lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() -1);
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED)
                        {
                            final String blogPostId = doc.getDocument().getId();
                            final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                            final String blogUserId =  doc.getDocument().getString("user_id");
                            firebaseFirestore.collection("User").document(Objects.requireNonNull(blogUserId)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()){
                                        if (user_id.equals(blogUserId)){

                                        User user =  task.getResult().toObject(User.class);
                                        user_list.add(user);
                                        blog_list.add(blogPost);
                                        blogRecycleAdapter.notifyDataSetChanged();
                                    }
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }


}
