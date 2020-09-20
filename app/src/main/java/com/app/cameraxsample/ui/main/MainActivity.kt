package com.app.cameraxsample.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.cameraxsample.R
import com.app.cameraxsample.ui.camera.CameraFragment

private const val TAG = "MainActivity"
class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .replace(R.id.container_layout, CameraFragment.newInstance())
            .commit()
    }
}