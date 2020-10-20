package com.example.photoblog;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationsFragment extends Fragment {

    private RecyclerView notification_list_view;
    private List<Notification> notification_list;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String currentuser_id;
    private NotificationRecyclerAdpater notificationRecyclerAdpater;


    public NotificationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        notification_list =  new ArrayList<>();
        notification_list_view =  view.findViewById(R.id.notification_list_view);

        notificationRecyclerAdpater =  new NotificationRecyclerAdpater(notification_list);
        notification_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        notification_list_view.setAdapter(notificationRecyclerAdpater);

        firebaseFirestore =  FirebaseFirestore.getInstance();
        firebaseAuth =  FirebaseAuth.getInstance();

        currentuser_id = firebaseAuth.getCurrentUser().getUid();
        try {
            firebaseFirestore.collection("Notification/" + currentuser_id + "/Notification").orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            Notification notification = doc.getDocument().toObject(Notification.class);
                            notification_list.add(notification);
                            notificationRecyclerAdpater.notifyDataSetChanged();
                        }
                    }
                }
            });
        }catch (Exception e){}
        // Inflate the layout for this fragment
        return view;
    }

}
