package com.example.photoblog;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity {
    private Toolbar mainToolbar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String current_user_id;
    public FloatingActionButton addPostBtn;
    private BottomNavigationView mainBottomNav;
    private HomeFragment homeFragment;
    private NotificationsFragment notificationFragment;
    private AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Photo Blog");






        if(mAuth.getCurrentUser() != null){
            mainBottomNav = findViewById(R.id.mainBottomNav);
            addPostBtn = findViewById(R.id.add_post_btn);

        //FRAGMENTS INTIALISATION
        homeFragment = new HomeFragment();
        notificationFragment =  new NotificationsFragment();
        accountFragment =  new AccountFragment();
        //whenever mainactivity starts home fragment is setfirst(you can say it as default frgament)
        replaceFragment(homeFragment);

        addPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,NewPostActivity.class));
                finish();
            }
        });

        mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()){
                    case R.id.bottom_action_home:
                        replaceFragment(homeFragment);
                        return true;
                    case R.id.bottom_action_notification:
                        replaceFragment(notificationFragment);
                        return true;
                    case R.id.bottom_action_account:
                        replaceFragment(accountFragment);
                        return true;
                        default:
                            return false;
                } 
            }
        });



    }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null){
           sendToLogin();
        }
        else
        {
            current_user_id = mAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("User").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        if(!task.getResult().exists()){
                            Intent setupIntent = new Intent(MainActivity.this,SetupActivity.class);
                            startActivity(setupIntent);
                            finish();
                        }
                    }
                    else
                    {
                        String error = task.getException().getMessage();
                        Toast.makeText(MainActivity.this,"Error : "+ error,Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);



        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_settings_btn:
                Intent intent = new Intent(MainActivity.this,SetupActivity.class);
                startActivity(intent);

                return true;
            case R.id.action_logout_btn:
                logout();
                return true;
                default:
                    return false;
        }
    }

    private void logout() {
        mAuth.signOut();
        sendToLogin();
    }


    private void sendToLogin(){
        Intent loginIntent =  new Intent(MainActivity.this,LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container,fragment);
        fragmentTransaction.commit();
    }
}
