package com.example.photoblog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView textView=findViewById(R.id.textView);
        TextView textView1=findViewById(R.id.textView1);
        Animation animation= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.splash);
        Animation animation1= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide);
        Animation animation2= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide1);
        textView.startAnimation(animation1);
        textView1.startAnimation(animation2);

        Thread timer= new Thread(){
            @Override
            public void run() {
                try {
                    sleep(2500);
                    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                    finish();
                    super.run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };
        timer.start();
    }
}
