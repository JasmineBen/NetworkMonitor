package com.conan.networkmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionHelper.checkPermissions(this);
        final EditText et = (EditText) findViewById(R.id.package_name);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pckName = et.getText().toString();
                if(TextUtils.isEmpty(pckName)){
                    Toast.makeText(MainActivity.this,R.string.inout_package_toast,Toast.LENGTH_SHORT).show();
                    return;
                }
                if(PermissionHelper.checkAccessibility(MainActivity.this)) {
                    startService(
                            new Intent(MainActivity.this, MonitorService.class)
                                    .putExtra(MonitorService.COMMAND, MonitorService.COMMAND_OPEN).putExtra(MonitorService.PACKAGE_NAME,pckName)
                    );
                    finish();
                }
            }
        });
    }
}
