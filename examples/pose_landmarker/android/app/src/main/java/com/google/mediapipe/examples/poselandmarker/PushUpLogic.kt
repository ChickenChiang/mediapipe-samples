package com.google.mediapipe.examples.poselandmarker

import android.util.Log
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.collections.get
import kotlin.collections.last
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sqrt


/*
    Handles the logic for the push up exercise

    Contains methods that calculates the angles and remembers the states of the user
 */

private const val TAG = "Push up Logic"

class PushUpLogic() {

    private var isCounting = false
    private var count = 0
    private var currState = INVALID_POSITION
    private var stateKeeper: MutableList<String> = mutableListOf<String>()
    private val successFulPushUps = listOf<String>(UP_POSITION, DOWN_POSITION, UP_POSITION)

    companion object PushupState {
        const val UP_POSITION = "Up position"
        const val DOWN_POSITION = "Down position"
        const val INVALID_POSITION = "Invalid Position"

    }



    class Coordinates(val x: Float, val y: Float)
    class Coordinates3D(val x: Float, val y: Float, val z: Float)

    fun startCounting() {
        isCounting = true
    }

    fun getCount(): Int {
        return count
    }

    fun stopCounting() {
        isCounting = false
    }

    fun resetCounter() {
        isCounting = false
        count = 0
        stateKeeper.clear()
    }

    private fun calculateAngle3D(
        landmark1: Coordinates3D,
        landmark2: Coordinates3D,
        landmark3: Coordinates3D,
    ): Double {
        val vector1 = Coordinates3D(
            landmark1.x - landmark2.x,
            landmark1.y - landmark2.y,
            landmark1.z - landmark2.z
        )
        val vector2 = Coordinates3D(
            landmark3.x - landmark2.x,
            landmark3.y - landmark2.y,
            landmark3.z - landmark2.z
        )

        // Calculate dot product
        val dotProduct = vector1.x * vector2.x + vector1.y * vector2.y + vector1.z * vector2.z

        // Calculate magnitudes
        val magnitude1 = sqrt(vector1.x * vector1.x + vector1.y * vector1.y + vector1.z * vector1.z)
        val magnitude2 = sqrt(vector2.x * vector2.x + vector2.y * vector2.y + vector2.z * vector2.z)

        // Calculate angle using arccosine
        val angleRadians = acos(dotProduct / (magnitude1 * magnitude2)).toDouble()

        // Convert to degrees
        val angleDegrees = Math.toDegrees(angleRadians)

        return angleDegrees
    }

    private fun calculateAngle(
        landmark1: Coordinates,
        landmark2: Coordinates,
        landmark3: Coordinates,
    ): Double {
        // vector 2->1 and vector 2->3
        val angle1 = atan2(
            (landmark1.y - landmark2.y).toDouble(),
            (landmark1.x - landmark2.x).toDouble()
        )
        val angle2 = atan2(
            (landmark3.y - landmark2.y).toDouble(),
            (landmark3.x - landmark2.x).toDouble()
        )
        // If angle is negative, the magnitude of the angle between the two vectors is the clockwise rotation
        // TODO: Figure out how to get the legs angle
        var angle = Math.toDegrees(angle2 - angle1)
        if (angle > 180) {
            angle -= 360
        } else if (angle < -180) {
            angle += 360
        }
        angle = abs(angle)


        return angle
    }

    fun processResult(result: PoseLandmarkerResult) {
        if (!isCounting) {
            return
        }
        val landmark = result.landmarks()

        if (landmark.isEmpty()) {
            Log.d(TAG, "No person found")
        } else {
            try {
                val normalizedLandmark = landmark.first()
                Log.d(TAG, "Calculating")
//                val leftWrist = Coordinates(normalizedLandmark[15].x(), normalizedLandmark[15].y())
//                val leftElbow = Coordinates(normalizedLandmark[13].x(), normalizedLandmark[13].y())
//                val leftShoulder =
//                    Coordinates(normalizedLandmark[11].x(), normalizedLandmark[11].y())
//                val leftHip = Coordinates(normalizedLandmark[23].x(), normalizedLandmark[23].y())
//                val leftKnee = Coordinates(normalizedLandmark[25].x(), normalizedLandmark[25].y())
//                val leftAnkle = Coordinates(normalizedLandmark[27].x(), normalizedLandmark[27].y())
//
//                val rightWrist = Coordinates(normalizedLandmark[16].x(), normalizedLandmark[16].y())
//                val rightElbow = Coordinates(normalizedLandmark[14].x(), normalizedLandmark[14].y())
//                val rightShoulder =
//                    Coordinates(normalizedLandmark[12].x(), normalizedLandmark[12].y())
//                val rightHip = Coordinates(normalizedLandmark[24].x(), normalizedLandmark[24].y())
//                val rightKnee = Coordinates(normalizedLandmark[26].x(), normalizedLandmark[26].y())
//                val rigthAnkle = Coordinates(normalizedLandmark[28].x(), normalizedLandmark[28].y())
//                val leftElbowAngle = calculateAngle(leftWrist, leftElbow, leftShoulder)
//                val rightElbowAngle = calculateAngle(rightWrist, rightElbow, rightShoulder)
//                val leftHipAngle = calculateAngle(leftKnee, leftHip, leftShoulder)
//                val rightHipAngle = calculateAngle(rightKnee, rightHip, rightShoulder)
//                val leftKneeAngle = calculateAngle(leftAnkle, leftKnee, leftHip)
//                val rightKneeAngle = calculateAngle(rightAnkle, rightKnee, rightHip)

                val leftWrist = Coordinates3D(
                    normalizedLandmark[15].x(),
                    normalizedLandmark[15].y(),
                    normalizedLandmark[15].z()
                )
                val leftElbow = Coordinates3D(
                    normalizedLandmark[13].x(),
                    normalizedLandmark[13].y(),
                    normalizedLandmark[13].z()
                )
                val leftShoulder = Coordinates3D(
                    normalizedLandmark[11].x(),
                    normalizedLandmark[11].y(),
                    normalizedLandmark[11].z()
                )
                val leftHip = Coordinates3D(
                    normalizedLandmark[23].x(),
                    normalizedLandmark[23].y(),
                    normalizedLandmark[23].z()
                )
                val leftKnee = Coordinates3D(
                    normalizedLandmark[25].x(),
                    normalizedLandmark[25].y(),
                    normalizedLandmark[25].z()
                )
                val leftAnkle = Coordinates3D(
                    normalizedLandmark[27].x(),
                    normalizedLandmark[27].y(),
                    normalizedLandmark[27].z()
                )

                val rightWrist = Coordinates3D(
                    normalizedLandmark[16].x(),
                    normalizedLandmark[16].y(),
                    normalizedLandmark[16].z()
                )
                val rightElbow = Coordinates3D(
                    normalizedLandmark[14].x(),
                    normalizedLandmark[14].y(),
                    normalizedLandmark[14].z()
                )
                val rightShoulder = Coordinates3D(
                    normalizedLandmark[12].x(),
                    normalizedLandmark[12].y(),
                    normalizedLandmark[12].z()
                )
                val rightHip = Coordinates3D(
                    normalizedLandmark[24].x(),
                    normalizedLandmark[24].y(),
                    normalizedLandmark[24].z()
                )
                val rightKnee = Coordinates3D(
                    normalizedLandmark[26].x(),
                    normalizedLandmark[26].y(),
                    normalizedLandmark[26].z()
                )
                val rightAnkle = Coordinates3D(
                    normalizedLandmark[28].x(),
                    normalizedLandmark[28].y(),
                    normalizedLandmark[28].z()
                )

                val leftElbowAngle = calculateAngle3D(leftWrist, leftElbow, leftShoulder)
                val rightElbowAngle = calculateAngle3D(rightWrist, rightElbow, rightShoulder)
                val leftHipAngle = calculateAngle3D(leftKnee, leftHip, leftShoulder)
                val rightHipAngle = calculateAngle3D(rightKnee, rightHip, rightShoulder)
                val leftKneeAngle = calculateAngle3D(leftAnkle, leftKnee, leftHip)
                val rightKneeAngle = calculateAngle3D(rightAnkle, rightKnee, rightHip)
                // TODO: Check for visibility and confidence
                // if legs are not straight
                if (leftKneeAngle <= 150 || rightKneeAngle <= 150 /* || */
//                    leftKneeAngle >= 210 || rightKneeAngle >= 210
                ) {
                    Log.d(TAG, "Legs not straight: LEFT: $leftKneeAngle, RIGHT: $rightKneeAngle")
                    // Don't count
                    currState = INVALID_POSITION
                }
                // If hips aren't straight
                else if (leftHipAngle <= 160 || rightHipAngle <= 160 /* || */
//                    leftHipAngle >= 190 || rightHipAngle >= 190
                ) {
                    Log.d(TAG, "Hips not straight: LEFT: $leftHipAngle, RIGHT: $rightHipAngle")
                    // Don't count
                    currState = INVALID_POSITION
                } else if (leftElbowAngle <= 90 && rightElbowAngle <= 90) {
                    currState = DOWN_POSITION
                } else if (leftElbowAngle >= 150 && rightElbowAngle >= 150) {
                    currState = UP_POSITION
                } else {
                    Log.d(
                        TAG,
                        "Transition: LK:$leftKneeAngle, LH:$leftHipAngle, LE:$leftElbowAngle"
                    )
                    Log.d(
                        TAG,
                        "Transition: RK:$rightKneeAngle, RH:$rightHipAngle, RE:$rightElbowAngle"
                    )

                    return
                }
                Log.d(TAG, "Current state is $currState")
                updateState()
            } catch (exception: NullPointerException) {
                Log.e(TAG, "Land mark not in view; ${exception.message}")
            }
        }
    }

    private fun updateState() {
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
