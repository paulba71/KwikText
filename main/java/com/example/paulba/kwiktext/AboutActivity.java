package com.example.paulba.kwiktext;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Dispplay the version number
        String verStr="Version: ";
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = pInfo.versionName;
        verStr=verStr+version;
        TextView tvVersion=(TextView)findViewById(R.id.tvVersion);
        tvVersion.setText(verStr);
    }

    public void onAboutCancelButtonClick(View v)
    {
        onBackPressed();
    }

    public void onSendFeedbackButtonClick(View v)
    {
        Toast.makeText(AboutActivity.this, "Send feedback comming really soon", Toast.LENGTH_LONG).show();
    }
}
