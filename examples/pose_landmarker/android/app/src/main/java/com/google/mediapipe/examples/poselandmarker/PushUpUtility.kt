package com.google.mediapipe.examples.poselandmarker

object PushUpUtility {
    private var kneeLowerThreshold: Double = 160.0
    private var hipLowerThreshold: Double = 160.0

    fun isLegStraight(angle: Double): Boolean {
        return (angle > kneeLowerThreshold)
    }

    fun isHipStraight(angle : Double): Boolean {
        return angle > hipLowerThreshold
    }
}