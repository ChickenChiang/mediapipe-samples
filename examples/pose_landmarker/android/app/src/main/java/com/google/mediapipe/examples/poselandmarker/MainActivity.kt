/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.mediapipe.examples.poselandmarker

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.mediapipe.examples.poselandmarker.databinding.ActivityMainBinding
import kotlin.math.floor

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {


    private lateinit var activityMainBinding: ActivityMainBinding
//    private val viewModel: MainViewModel by viewModels()



    val viewModel: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        var startButton = findViewById<FloatingActionButton>(R.id.startButton)
        var resetButton = findViewById<FloatingActionButton>(R.id.resetButton)
        startButton.setOnClickListener { view ->
            viewModel.startStopTimer()
            Log.d(TAG, "Start/Stop button pressed")
        }
        viewModel.count.observe(this) { count ->
            updatePushUpCounter(count)
        }
        viewModel.secondsRemaing.observe(this) { secondsLeft ->
            updateTimerUI(secondsLeft)
        }
        resetButton.setOnClickListener { view ->
            viewModel.resetTimer()
        }
    }

    fun updateTimerUI(secondsLeft : Long) {
        var minutesLeftString = floor((secondsLeft / 60).toDouble()).toInt().toString()
        var secondsLeftString = (secondsLeft % 60).toString()
        if (secondsLeftString.length < 2) {
            secondsLeftString = "0" + secondsLeftString
        }
        var timerText = findViewById<TextView>(R.id.countdown_Timer)
        timerText.text = getString(R.string.timer_text, minutesLeftString, secondsLeftString)

    }


//    fun updateCountDownTimer() {
//        var timerText = findViewById<TextView>(R.id.countdown_Timer)
//        val minutesUntilFinished = secondsRemaining / 60
//        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
//        var secondsStr = secondsInMinuteUntilFinished.toString()
//        if (secondsStr.length < 2) {
//            secondsStr = "0" + secondsStr
//        }
//        timerText.text = "$minutesUntilFinished:$secondsStr"
//    }

    fun updatePushUpCounter(count : Int) {
        var countText = findViewById<TextView>(R.id.pushup_counter)
        countText.text = getString(R.string.count_text, count)

    }

    override fun onBackPressed() {
        finish()
    }
}