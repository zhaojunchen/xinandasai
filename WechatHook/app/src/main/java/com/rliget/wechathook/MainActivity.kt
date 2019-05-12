package com.rliget.wechathook

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!isModuleActive()) {
            Toast.makeText(this, "模块未启动", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "模块已启动", Toast.LENGTH_LONG).show()
        }
    }

    private fun isModuleActive(): Boolean {
        return false
    }
}
