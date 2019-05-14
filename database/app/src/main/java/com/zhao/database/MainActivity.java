package com.zhao.database;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    /***/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /** 程序的版本信息设置*/
        float nowVersionCode = getVersionName(this);
        Log.d(TAG, "onCreate: 本程序版本为" + String.valueOf(nowVersionCode));
        SharedPreferences sp = getSharedPreferences("firstrun", MODE_PRIVATE);
        float spVersionCode = sp.getFloat("spVersionCode", 0);
        Log.d(TAG, "onCreate: 上次程序的版本为" + String.valueOf(spVersionCode));

        if (nowVersionCode > spVersionCode) {
            /**加载布局文件*/
            setContentView(R.layout.activity_main);
            SharedPreferences.Editor edit = sp.edit();
            edit.putFloat("spVersionCode", nowVersionCode);
            edit.commit();
            Toast.makeText(this, "应用首次启动", Toast.LENGTH_LONG);
            Log.d(TAG, "应用首次启动");

            /**
             * 添加初始化代码
             */
        } else {
            setContentView(R.layout.activity_main);
            Log.d(TAG, "onCreate: 几次以上打开程序");
        }
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 版本初始化鉴别
     */
    private float getVersionName(Context context) {
        float versionName = 0;
        /**获取程序的versionName--->对应build-gradle的versionName*/
        try {
            versionName = Float.parseFloat(context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName);

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            return versionName;
        }
    }
}
