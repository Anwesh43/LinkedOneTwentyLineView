package com.anwesh.uiprojects.linkedonetwentylineview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.onetwentylineview.OneTwentyLineView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OneTwentyLineView.create(this)
    }
}
