package com.inforse.annoywechat

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!isModuleActive) {
            Toast.makeText(this, "No welcome", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Welcome1", Toast.LENGTH_LONG).show()
        }
    }



    private val isModuleActive: Boolean
        get() = false
}
