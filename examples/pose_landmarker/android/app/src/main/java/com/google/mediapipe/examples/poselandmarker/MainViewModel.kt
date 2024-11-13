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

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

/**
 *  This ViewModel is used to store pose landmarker helper settings
 */

private const val TAG = "MainViewModel"

class MainViewModel : ViewModel() {


    private var _model = PoseLandmarkerHelper.MODEL_POSE_LANDMARKER_FULL
    private var _delegate: Int = PoseLandmarkerHelper.DELEGATE_CPU
    private var _minPoseDetectionConfidence: Float =
        PoseLandmarkerHelper.DEFAULT_POSE_DETECTION_CONFIDENCE
    private var _minPoseTrackingConfidence: Float = PoseLandmarkerHelper
        .DEFAULT_POSE_TRACKING_CONFIDENCE
    private var _minPosePresenceConfidence: Float = PoseLandmarkerHelper
        .DEFAULT_POSE_PRESENCE_CONFIDENCE

    val currentDelegate: Int get() = _delegate
    val currentModel: Int get() = _model
    val currentMinPoseDetectionConfidence: Float
        get() =
            _minPoseDetectionConfidence
    val currentMinPoseTrackingConfidence: Float
        get() =
            _minPoseTrackingConfidence
    val currentMinPosePresenceConfidence: Float
        get() =
            _minPosePresenceConfidence

    var is3D : Boolean = false


    /* Timer variables */
    // Timer stuff
    enum class TimerState {
        STOPPED, PAUSED, RUNNING
    }

    private lateinit var timer: CountDownTimer
    private var timerState: TimerState = TimerState.STOPPED
    private var _secondsRemaining = MutableLiveData<Long>(60L) // IN SECONDS
    var secondsRemaing: LiveData<Long> = _secondsRemaining

    /* Push up variables */
    private var pushUpLogic: PushUpLogic = PushUpLogic()
    private val _currUserState = MutableLiveData<String>()
    val currUserState = _currUserState
    private val _count = MutableLiveData<Int>()
    val count: LiveData<Int> = _count
    private val _angles = MutableLiveData<List<Double>>()
    val angles: LiveData<List<Double>> =  _angles

    fun setDelegate(delegate: Int) {
        _delegate = delegate
    }

    fun setMinPoseDetectionConfidence(confidence: Float) {
        _minPoseDetectionConfidence = confidence
    }

    fun setMinPoseTrackingConfidence(confidence: Float) {
        _minPoseTrackingConfidence = confidence
    }

    fun setMinPosePresenceConfidence(confidence: Float) {
        _minPosePresenceConfidence = confidence
    }

    fun setModel(model: Int) {
        _model = model
    }

    fun setAngleCalculationMode(isChecked : Boolean) {
        is3D = isChecked
    }

    // Timer and push up related stuff
    private fun startTimer() {
        timerState = TimerState.RUNNING
        timer = object : CountDownTimer(60 * 1000, 1000) {
            override fun onFinish() {
                pushUpLogic.stopCounting()
            }

            override fun onTick(millisUntilFinished: Long) {
                _secondsRemaining.value = millisUntilFinished / 1000
//                updateCountDownTimer()
            }
        }.start()
    }

    fun pauseTimer() {
        timerState = TimerState.PAUSED
        timer.cancel()
    }

    fun continueTimer() {
        Log.d(TAG, "Continuing timer")
        assert(timerState == TimerState.PAUSED)
        timerState = TimerState.RUNNING
        val timeLeftInMillis = secondsRemaing.value?.times(1000)
        if (timeLeftInMillis != null) {
            timer = object : CountDownTimer(timeLeftInMillis, 1000) {
                override fun onFinish() {
                    pushUpLogic.stopCounting()
                }
                override fun onTick(millisUntilFinished: Long) {
                    _secondsRemaining.value = millisUntilFinished / 1000
                }
            }.start()
        }
    }

    fun resetTimer() {
        Log.d(TAG, "Reset Timer")
        if (timerState == TimerState.RUNNING || timerState == TimerState.PAUSED) {
            timer.cancel()
        }
        timerState = TimerState.STOPPED
        pushUpLogic.resetCounter()
        _secondsRemaining.value = 60L
    }


    fun startStopTimer() {
        if (timerState == TimerState.STOPPED) {
            Log.d(TAG, "Start timer")
            pushUpLogic.startCounting()
            startTimer()
        } else if (timerState == TimerState.RUNNING) {
            Log.d(TAG, "Stop timer")
            pushUpLogic.stopCounting()
            pauseTimer()
        } else if (timerState == TimerState.PAUSED) {
            Log.d(TAG, "Continue timer")
            pushUpLogic.startCounting()
            continueTimer()
        }
    }

    fun updateModel(result: PoseLandmarkerResult) {
        pushUpLogic.processResult(result, is3D)
        Log.d(TAG, "Processed Result")
        _count.value = pushUpLogic.getCount()
        _angles.value = pushUpLogic.getAngles()
        _currUserState.value = pushUpLogic.getState()
    }
}