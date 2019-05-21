package com.zhao.a_native;

import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        String mingwen = "赵君臣好帅";
        byte[] key = new byte[16];
        key[0] = -22;
        key[1] = 63;
        key[2] = 116;
        key[3] = 2;
        key[4] = -25;
        key[5] = -68;
        key[6] = 83;
        key[7] = 70;
        key[8] = -17;
        key[9] = -117;
        key[10] = 27;
        key[11] = -10;
        key[12] = -85;
        key[13] = 110;
        key[14] = 126;
        key[15] = -37;
        for (int i = 0; i < 16; i++) {
            Log.e(TAG, "onCreate:" + key[i]);
        }

//        tv.append(stringFromJNI());
        SM4 sm4 = new SM4();
        tv.append("mingwen" + mingwen + "\n");
        /** 出现报错  */
        byte[] miwen = sm4.encryption(mingwen.getBytes(), key);
        tv.append("miwen" + new String(miwen)+ "\n");
        byte[] jiemi = sm4.decryption(miwen, key);
        tv.append("jiemi" + new String(jiemi) + "\n");
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
