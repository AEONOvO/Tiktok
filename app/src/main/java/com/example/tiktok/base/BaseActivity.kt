package com.example.tiktok.base


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


abstract class BaseActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    protected  abstract fun init()

}
