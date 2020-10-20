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

public class RegisterActivity extends AppCompatActivity {

    private EditText reg_email_field;
    private EditText reg_pass_field;
    private EditText reg_confirm_pass_field;
    private Button reg_login_btn;
    private Button reg_btn;
    private FirebaseAuth mAuth;
    private ProgressBar reg_progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        reg_email_field = (EditText) findViewById(R.id.reg_email);
        reg_pass_field = (EditText) findViewById(R.id.reg_pass);
        reg_confirm_pass_field =(EditText) findViewById(R.id.reg_confirm_pass);
        reg_btn = (Button) findViewById(R.id.reg_btn);
        reg_login_btn = (Button) findViewById(R.id.reg_login_btn);
        reg_progressBar = (ProgressBar) findViewById(R.id.reg_progressBar);

        reg_progressBar.setVisibility(View.INVISIBLE);

        reg_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                finish();
            }
        });

        reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = reg_email_field.getText().toString();
                String pass = reg_pass_field.getText().toString();
                String comfirm_pass = reg_confirm_pass_field.getText().toString();

                if(!TextUtils.isEmpty(email)&& !TextUtils.isEmpty(pass)&& !TextUtils.isEmpty(comfirm_pass)){

                    if (pass.equals(comfirm_pass)){

                        reg_progressBar.setVisibility(View.VISIBLE);
                        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful())
                                {
                                    Intent intent = new Intent(RegisterActivity.this,SetupActivity.class);
                                    startActivity(intent);
                                }
                                else {
                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this,"ERROR: "+ errorMessage,Toast.LENGTH_LONG).show();
                                }

                                reg_progressBar.setVisibility(View.INVISIBLE);
                            }
                        });

                    }
                    else
                    {
                        Toast.makeText(RegisterActivity.this,"Password not matched",Toast.LENGTH_LONG).show();
                    }
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
        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
