package com.google.mediapipe.examples.poselandmarker

import android.util.Log
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.collections.last
import kotlin.math.abs
import kotlin.math.atan2


/*
    Handles the logic for the push up exercise

    Contains methods that calculates the angles and remembers the states of the user
 */

private const val TAG = "Push up Logic"

class PushUpLogic() {
    var count = 0
    var currState = INVALID_POSITION
    var stateKeeper: MutableList<String> = mutableListOf<String>()
    val successFulPushUps = listOf<String>(UP_POSITION, DOWN_POSITION, UP_POSITION)

    companion object PushupState {
        const val UP_POSITION = "Up position"
        const val DOWN_POSITION = "Down position"
        const val INVALID_POSITION = "Invalid Position"

    }

    class Coordinates(val x: Float, val y: Float) {

    }

    fun calculateAngle(
        landmark1: Coordinates,
        landmark2: Coordinates,
        landmark3: Coordinates,
    ): Double {
        val angle1 = atan2(
            (landmark1.y - landmark2.y).toDouble(),
            (landmark1.x - landmark2.x).toDouble()
        )
        val angle2 = atan2(
            (landmark3.y - landmark2.y).toDouble(),
            (landmark3.x - landmark2.x).toDouble()
        )

        var angle = Math.toDegrees(angle2 - angle1)
//        if (angle < 0) {
//            angle += 360
//        }
        angle = abs(angle)

        return angle
    }

    fun processResult(result: PoseLandmarkerResult) {

        val landmark = result.landmarks()

        if (landmark.isEmpty()) {
            Log.d(TAG, "No person found")
        } else {
            try {
                val normalizedLandmark = landmark.first()
                Log.d(TAG, "Try calculation")
                val leftWrist = Coordinates(normalizedLandmark[15].x(), normalizedLandmark[15].y())
                val leftElbow = Coordinates(normalizedLandmark[13].x(), normalizedLandmark[13].y())
                val leftShoulder =
                    Coordinates(normalizedLandmark[11].x(), normalizedLandmark[11].y())
                val leftHip = Coordinates(normalizedLandmark[23].x(), normalizedLandmark[23].y())
                val leftKnee = Coordinates(normalizedLandmark[25].x(), normalizedLandmark[25].y())
                val leftAnkle = Coordinates(normalizedLandmark[27].x(), normalizedLandmark[27].y())

                val rightWrist = Coordinates(normalizedLandmark[16].x(), normalizedLandmark[16].y())
                val rightElbow = Coordinates(normalizedLandmark[14].x(), normalizedLandmark[14].y())
                val rightShoulder =
                    Coordinates(normalizedLandmark[12].x(), normalizedLandmark[12].y())
                val rightHip = Coordinates(normalizedLandmark[24].x(), normalizedLandmark[24].y())
                val rightKnee = Coordinates(normalizedLandmark[26].x(), normalizedLandmark[26].y())
                val rigthAnkle = Coordinates(normalizedLandmark[28].x(), normalizedLandmark[28].y())

                val leftElbowAngle = calculateAngle(leftWrist, leftElbow, leftShoulder)
                val rightElbowAngle = calculateAngle(rightWrist, rightElbow, rightShoulder)
                val leftHipAngle = calculateAngle(leftKnee, leftHip, leftShoulder)
                val rightHipAngle = calculateAngle(rightKnee, rightHip, rightShoulder)
                val leftKneeAngle = calculateAngle(leftAnkle, leftKnee, leftHip)
                val rightKneeAngle = calculateAngle(rigthAnkle, rightKnee, rightHip)

//                Log.d(TAG, "Left elbow angle: $leftElbowAngle")
//                Log.d(TAG, "Right elbow angle: $rightElbowAngle")
//                Log.d(TAG, "Left hip angle: $leftHipAngle")
//                Log.d(TAG, "Right hip angle: $rightHipAngle")
//                Log.d(TAG, "Left knee angle: $leftKneeAngle")
//                Log.d(TAG, "Right knee angle: $rightKneeAngle")

                // if legs are not straight
                if (leftKneeAngle <= 150 || rightKneeAngle <= 150 ||
                    leftKneeAngle >= 210 || rightKneeAngle >= 210
                ) {
                    Log.d(TAG, "Legs not straight: LEFT: $leftKneeAngle, RIGHT: $rightKneeAngle")
                    // Don't count
                    currState = INVALID_POSITION
                }
                // If hips aren't straight
                else if (leftHipAngle <= 160 || rightHipAngle <= 160 ||
                    leftHipAngle >= 190 || rightHipAngle >= 190
                ) {
                    Log.d(TAG, "Hips not straight: LEFT: $leftHipAngle, RIGHT: $rightHipAngle")
                    // Don't count
                    currState = INVALID_POSITION
                } else if (leftElbowAngle <= 90 && rightElbowAngle <= 90) {
                    currState = DOWN_POSITION
                } else if (leftElbowAngle >= 150 && rightElbowAngle >= 150) {
                    currState = UP_POSITION
                } else {
                    return
                }
                Log.d(TAG, "Current state is $currState")
                updateState()
            } catch (exception: NullPointerException) {
                Log.e(TAG, "Land mark not in view; ${exception.message}")
            }
        }
    }

    fun updateState() {
        if (stateKeeper.isEmpty()) {
            // If state is empty, add state
            Log.d(TAG, "StateKeeper empty")
            stateKeeper.add(currState)
            return
        }
        if (currState == stateKeeper.last()) {
            // DO NOTHING - no change in state, no need to update
            return
        } else if (stateKeeper.size < 3) {
            stateKeeper.add(currState)
        } else if (stateKeeper.size == 3) {
            stateKeeper.removeAt(0)
            stateKeeper.add(currState)
        } else {
            Log.e(TAG, "State keeper size > 3")
            while (stateKeeper.size > 3) {
                stateKeeper.removeAt(0)
            }
            stateKeeper.add(currState)
        }
        if (stateKeeper == successFulPushUps) {
            count += 1
            Log.d(TAG, "Count: increment; COUNT: $count")
        }
    }
}
