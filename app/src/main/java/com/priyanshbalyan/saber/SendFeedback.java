package com.priyanshbalyan.saber;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SendFeedback extends AppCompatActivity
{

    Toolbar toolbar ;
    EditText issuetext ;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO: Implement this method
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sendfeedback);

        if(Build.VERSION.SDK_INT >= 21)
            getWindow().setStatusBarColor(Utilities.getcolor(this,true));

        toolbar = (Toolbar)findViewById(R.id.supporttoolbar);
        toolbar.setBackgroundColor(Utilities.getcolor(this,false));

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Feedback");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View p1)
            {
                // TODO: Implement this method
                finish();
            }
        });

        issuetext = (EditText)findViewById(R.id.etsupport);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // TODO: Implement this method
        getMenuInflater().inflate(R.menu.sendmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // TODO: Implement this method
        if(!issuetext.getText().toString().equals("")){
            String s = issuetext.getText().toString()
                    +"\nSent from "
                    +"\nDevice: " + android.os.Build.MODEL + " " + android.os.Build.MANUFACTURER
                    +"\nAndroid:" + android.os.Build.VERSION.RELEASE
                    +"\n\nActivity Log : \n\n";

            try{
                Process process = Runtime.getRuntime().exec("logcat -d");
                BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder log = new StringBuilder();
                String line = "" ;
                while((line = bufferedreader.readLine()) != null)
                    log.append(line);

                s += log.toString();
            }catch(IOException e){}

            Intent emaili  = new Intent(Intent.ACTION_SENDTO);
            emaili.setClassName("com.google.android.gm" ,"com.google.android.gm.ComposeActivityGmail" );
            emaili.setAction(Intent.ACTION_SEND);
            emaili.setType("message/rfc822");
            emaili.putExtra(Intent.EXTRA_EMAIL, "priyanshbalyan@gmail.com");
            emaili.setData(Uri.parse("priyanshbalyan@gmail.com"));
            emaili.putExtra(Intent.EXTRA_SUBJECT, "Saber Player (Support)");
            emaili.putExtra(Intent.EXTRA_TEXT, s);
            try{  startActivity(emaili);  }
            catch(ActivityNotFoundException e){ Toast.makeText(getApplicationContext(), "Email client not found",Toast.LENGTH_SHORT).show();  }
        }else
            Toast.makeText(getApplicationContext(), "Type down your issue", Toast.LENGTH_SHORT).show();
        return true;
    }

}
