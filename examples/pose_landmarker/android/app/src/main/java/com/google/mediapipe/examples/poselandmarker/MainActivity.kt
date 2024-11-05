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
import android.os.CountDownTimer
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.mediapipe.examples.poselandmarker.databinding.ActivityMainBinding

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {


    private lateinit var activityMainBinding: ActivityMainBinding
//    private val viewModel: MainViewModel by viewModels()

    // Timer stuff
    enum class TimerState {
        Stopped, Paused, Running
    }
    val viewModel : MainViewModel by viewModels()

    private lateinit var timer: CountDownTimer
    private var timerLengthSeconds: Long = 0L
    private var timerState: TimerState = TimerState.Stopped
    private var secondsRemaining: Long = 0L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        var startButton = findViewById<FloatingActionButton>(R.id.startButton)
        var pauseButton = findViewById<FloatingActionButton>(R.id.pauseButton)
        startButton.setOnClickListener { view ->
            if (timerState == TimerState.Stopped) {
                startTimer()
            } else if (timerState == TimerState.Running) {
                pauseTimer()
            } else if (timerState == TimerState.Paused) {
                continueTimer()
            }
            Log.d(TAG, "Start button pressed")
//            updateButtons()
        }

        pauseButton.setOnClickListener { view ->
            if (timerState == TimerState.Running) {
                timerState = TimerState.Paused
                timer.cancel()
            }
        }
    }

    fun startTimer() {
        timerState = TimerState.Running
        timer = object : CountDownTimer(60 * 1000, 1000) {
            override fun onFinish() {

            }

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                updateCountDownUI()
            }
        }.start()
    }

    fun pauseTimer() {
        timerState = TimerState.Paused
        timer.cancel()
    }

    fun continueTimer() {
        Log.d(TAG, "Continuing timer")
        assert(timerState == TimerState.Paused)
        timerState = TimerState.Running
        val timeLeftInMillis = secondsRemaining * 1000
        timer = object : CountDownTimer(timeLeftInMillis * 1000, 1000) {
            override fun onFinish() {

            }

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                updateCountDownUI()
            }
        }.start()
    }

    fun updateCountDownUI() {
        var timerText = findViewById<TextView>(R.id.countdown_Timer)
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        var secondsStr = secondsInMinuteUntilFinished.toString()
        if (secondsStr.length < 2) {
            secondsStr = "0" + secondsStr
        }
        timerText.text = "$minutesUntilFinished:$secondsStr"
    }

    override fun onBackPressed() {
        finish()
    }
}