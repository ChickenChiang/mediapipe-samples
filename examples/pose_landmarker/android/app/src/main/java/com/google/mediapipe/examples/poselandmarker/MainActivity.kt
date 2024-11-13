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
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.mediapipe.examples.poselandmarker.PushUpUtility.isHipStraight
import com.google.mediapipe.examples.poselandmarker.PushUpUtility.isLegStraight
import com.google.mediapipe.examples.poselandmarker.databinding.ActivityDebugBinding
import kotlin.math.floor

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    //    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var activityMainBinding: ActivityDebugBinding

    val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityDebugBinding.inflate(layoutInflater)

//        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        var startButton = findViewById<Button>(R.id.startButton)
        var resetButton = findViewById<Button>(R.id.resetButton)
        viewModel.count.observe(this) { count ->
            updatePushUpCounter(count)
        }
        viewModel.secondsRemaing.observe(this) { secondsLeft ->
            updateTimerUI(secondsLeft)
        }
        viewModel.currUserState.observe(this) { state ->
            updateUserState(state)
        }
        viewModel.angles.observe(this) { angleList ->
            updateAngles(angleList)
        }

        startButton.setOnClickListener { view ->
            viewModel.startStopTimer()
            Log.d(TAG, "Start/Stop button pressed")
        }
        resetButton.setOnClickListener { view ->
            viewModel.resetTimer()
            Snackbar.make(
                view, "Counter has been reset", Snackbar.ANIMATION_MODE_SLIDE
            ).show()
        }
    }

    fun updateAngles(angleList: List<Double>) {
        var leftElbowTextView = findViewById<TextView>(R.id.leftElbow)
        var rightElbowTextView = findViewById<TextView>(R.id.rightElbow)
        var leftHipTextView = findViewById<TextView>(R.id.leftHip)
        var rightHipTextView = findViewById<TextView>(R.id.rightHip)
        var leftKneeTextView = findViewById<TextView>(R.id.leftKnee)
        var rightKneeTextView = findViewById<TextView>(R.id.rightKnee)
        if (angleList.size == 6) {
            // ELBOWS
            leftElbowTextView.text = getString(R.string.left_elbow, "%.0f".format(angleList[0]))
            rightElbowTextView.text = getString(R.string.right_elbow, "%.0f".format(angleList[1]))
            leftElbowTextView.setBackgroundResource(R.drawable.rounded_corner)
            rightElbowTextView.setBackgroundResource(R.drawable.rounded_corner)


            // HIPS
            if (isHipStraight(angleList[2])) {
                leftHipTextView.setBackgroundResource(R.drawable.rounded_corner)
            } else {
                leftHipTextView.setBackgroundResource(R.drawable.rounded_corner_red)
            }
            leftHipTextView.text = getString(R.string.left_hip, "%.0f".format(angleList[2]))

            if (isHipStraight(angleList[3])) {
                rightHipTextView.setBackgroundResource(R.drawable.rounded_corner)
            } else {
                rightHipTextView.setBackgroundResource(R.drawable.rounded_corner_red)
            }
            rightHipTextView.text = getString(R.string.right_hip, "%.0f".format(angleList[3]))

            // KNEES
            if (isLegStraight(angleList[4])) {
                leftKneeTextView.setBackgroundResource(R.drawable.rounded_corner)
            } else {
                leftKneeTextView.setBackgroundResource(R.drawable.rounded_corner_red)
            }
            leftKneeTextView.text = getString(R.string.left_knee, "%.0f".format(angleList[4]))

            if (isLegStraight(angleList[5])) {
                rightKneeTextView.setBackgroundResource(R.drawable.rounded_corner)
            } else {
                rightKneeTextView.setBackgroundResource(R.drawable.rounded_corner_red)
            }
            rightKneeTextView.text = getString(R.string.right_knee, "%.0f".format(angleList[5]))

        } else { // Make all angle text show "NA"
            leftElbowTextView.setBackgroundResource(R.drawable.rounded_corner_grey)
            leftElbowTextView.text = getString(R.string.left_elbow, "NA")
            rightElbowTextView.setBackgroundResource(R.drawable.rounded_corner_grey)
            rightElbowTextView.text = getString(R.string.right_elbow, "NA")
            leftHipTextView.setBackgroundResource(R.drawable.rounded_corner_grey)
            leftHipTextView.text = getString(R.string.left_hip, "NA")
            rightHipTextView.setBackgroundResource(R.drawable.rounded_corner_grey)
            rightHipTextView.text = getString(R.string.right_hip, "NA")
            leftKneeTextView.setBackgroundResource(R.drawable.rounded_corner_grey)
            leftKneeTextView.text = getString(R.string.left_knee, "NA")
            rightKneeTextView.setBackgroundResource(R.drawable.rounded_corner_grey)
            rightKneeTextView.text = getString(R.string.right_knee, "NA")
        }
    }

    fun updateUserState(state: String) {
        var userStateTextView = findViewById<TextView>(R.id.state)
        if (state == PushUpLogic.PushupState.INVALID_POSITION) {
            userStateTextView.setBackgroundResource(R.drawable.rounded_corner_red)
        } else if (state == PushUpLogic.PushupState.NO_PERSON_FOUND) {
            userStateTextView.setBackgroundResource(R.drawable.rounded_corner_grey)
        } else {
            userStateTextView.setBackgroundResource(R.drawable.rounded_corner)
        }
        userStateTextView.text = getString(R.string.user_state_text, state)
    }

    fun updateTimerUI(secondsLeft: Long) {
        // Calculate how many minutes left. This is more for future proofing if we want to take more than a minute
        var minutesLeftString = floor((secondsLeft / 60).toDouble()).toInt().toString()
        // Get the remaining seconds
        var secondsLeftString = (secondsLeft % 60).toString()
        // Add a zero in front if `secondsleft` are in the single digits
        if (secondsLeftString.length < 2) {
            secondsLeftString = "0" + secondsLeftString
        }

        //Update the UI
//        var timerText = findViewById<TextView>(R.id.countdown_Timer)
//        timerText.text = getString(R.string.timer_text, minutesLeftString, secondsLeftString)

    }

    fun updatePushUpCounter(count: Int) {
        var countText = findViewById<TextView>(R.id.pushup_counter)
        countText.text = getString(R.string.count_text, count)
    }

    override fun onBackPressed() {
        finish()
    }
}