package com.example.photoblog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmailText;;
    private EditText mPasswordText;
    private Button mLoginBtn;
    private Button mLoginRegBtn;
        private  FirebaseAuth mAuth;
    private ProgressBar mLoginProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth =  FirebaseAuth.getInstance();

        mEmailText = (EditText) findViewById(R.id.reg_email);
        mPasswordText = (EditText) findViewById( R.id.reg_confirm_pass);
        mLoginBtn =  (Button) findViewById(R.id.login_btn);
        mLoginRegBtn = (Button) findViewById(R.id.login_reg_btn);
        mLoginProgressBar = (ProgressBar) findViewById(R.id.login_progressBar);
        mLoginProgressBar.setVisibility(View.INVISIBLE);

        mLoginRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String loginEmail = mEmailText.getText().toString();
                String loginPassword =  mPasswordText.getText().toString();



                if (!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPassword))
                {
                    mLoginProgressBar.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(loginEmail,loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                sendToMain();
                            }
                            else
                            {
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this,"ERROR: "+ errorMessage,Toast.LENGTH_LONG).show();
                            }
                            mLoginProgressBar.setVisibility(View.INVISIBLE);

                        }

                    });
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser cuurentuser = mAuth.getCurrentUser();
        if (cuurentuser != null){
           sendToMain();

        }
    }

private void sendToMain(){
    Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
    startActivity(mainIntent);
    finish();
}
}
