package com.virtualightning.borderview

import android.graphics.Color
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn1.setOnClickListener {
            borderView.setBoxBorderColor(Color.BLACK)
        }
        btn2.setOnClickListener {
            borderView.setBoxBorderActiveColor(Color.RED)
        }
        btn3.setOnClickListener {
            borderView.setBoxRadius(10f)
        }
        btn4.setOnClickListener {
            borderView.setBoxBorderWidth(5f)
        }
        btn5.setOnClickListener {
            borderView.setCursorWidth(10f)
        }
        btn7.setOnClickListener {
            borderView.setCursorColor(Color.BLUE)
        }
        btn6.setOnClickListener {
            borderView.setCursorGravity(BorderView.GRAVITY_START)
        }


        borderView.setOnCompletedInputListener(object : BorderView.OnCompletedInputListener {
            override fun onCompletedInput(inputContent: String?) {
                Toast.makeText(this@MainActivity,inputContent,Toast.LENGTH_SHORT).show()
            }
        })
    }
}
