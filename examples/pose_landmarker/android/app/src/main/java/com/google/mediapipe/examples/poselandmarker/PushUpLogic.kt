package com.google.mediapipe.examples.poselandmarker

import android.util.Log
import com.google.mediapipe.formats.proto.LandmarkProto
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.collections.last
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.max
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
    private var angles: MutableList<Double> = mutableListOf<Double>()
    private val successFulPushUps = listOf<String>(UP_POSITION, DOWN_POSITION, UP_POSITION)

    companion object PushupState {
        const val NO_PERSON_FOUND = "No person found"
        const val UP_POSITION = "Up position"
        const val DOWN_POSITION = "Down position"
        const val INVALID_POSITION = "Invalid Position"
        const val TRANSITION = "Transition Position"

        const val NOSE = 0
        const val LEFT_EYE_INNER = 1
        const val LEFT_EYE = 2
        const val LEFT_EYE_OUTER = 3
        const val RIGHT_EYE_INNER = 4
        const val RIGHT_EYE = 5
        const val RIGHT_EYE_OUTER = 6
        const val LEFT_EAR = 7
        const val RIGHT_EAR = 8
        const val MOUTH_LEFT = 9
        const val MOUTH_RIGHT = 10
        const val LEFT_SHOULDER = 11
        const val RIGHT_SHOULDER = 12
        const val LEFT_ELBOW = 13
        const val RIGHT_ELBOW = 14
        const val LEFT_WRIST = 15
        const val RIGHT_WRIST = 16
        const val LEFT_PINKY = 17
        const val RIGHT_PINKY = 18
        const val LEFT_INDEX = 19
        const val RIGHT_INDEX = 20
        const val LEFT_THUMB = 21
        const val RIGHT_THUMB = 22
        const val LEFT_HIP = 23
        const val RIGHT_HIP = 24
        const val LEFT_KNEE = 25
        const val RIGHT_KNEE = 26
        const val LEFT_ANKLE = 27
        const val RIGHT_ANKLE = 28
        const val LEFT_HEEL = 29
        const val RIGHT_HEEL = 30
        const val LEFT_FOOT_INDEX = 31
        const val RIGHT_FOOT_INDEX = 32

    }

    class Coordinates(val x: Float, val y: Float)
    class Coordinates3D(val x: Float, val y: Float, val z: Float)

    fun startCounting() {
        isCounting = true
    }

    fun getCount(): Int {
        return count
    }

    fun getState(): String {
        return currState
    }

    fun getAngles(): MutableList<Double> {
        return angles
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
        val angleRadians = acos(dotProduct / max((magnitude1 * magnitude2), 0.0001F)).toDouble()

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
//        if (!isCounting) {
//            return
//        }
        val landmark = result.landmarks()

        if (landmark.isEmpty()) {
            currState = NO_PERSON_FOUND
            angles.clear()
            Log.d(TAG, currState)
        } else {
            try {
                Log.d(TAG, "Determining state of user")
                val normalizedLandmark = landmark.first()
                determineState(normalizedLandmark)
                Log.d(TAG, "Curr state: $currState")
                handleState()
                /* Vestigial
//                // if legs are not straight
//                if (leftKneeAngle <= 150 || rightKneeAngle <= 150 /* || */
////                    leftKneeAngle >= 210 || rightKneeAngle >= 210
//                ) {
//                    Log.d(TAG, "Legs not straight: LEFT: $leftKneeAngle, RIGHT: $rightKneeAngle")
//                    // Don't count
//                    currState = INVALID_POSITION
//                }
//                // If hips aren't straight
//                else if (leftHipAngle <= 160 || rightHipAngle <= 160 /* || */
////                    leftHipAngle >= 190 || rightHipAngle >= 190
//                ) {
//                    Log.d(TAG, "Hips not straight: LEFT: $leftHipAngle, RIGHT: $rightHipAngle")
//                    // Don't count
//                    currState = INVALID_POSITION
//                } else if (leftElbowAngle <= 90 && rightElbowAngle <= 90) {
//                    currState = DOWN_POSITION
//                } else if (leftElbowAngle >= 150 && rightElbowAngle >= 150) {
//                    currState = UP_POSITION
//                } else {
//                    Log.d(
//                        TAG,
//                        "Transition: LK:$leftKneeAngle, LH:$leftHipAngle, LE:$leftElbowAngle"
//                    )
//                    Log.d(
//                        TAG,
//                        "Transition: RK:$rightKneeAngle, RH:$rightHipAngle, RE:$rightElbowAngle"
//                    )
//
//                    return
//                }
//                Log.d(TAG, "Curr state: $currState")
//                updateState()

                 */
            } catch (exception: NullPointerException) {
                Log.e(TAG, "Land mark not in view; ${exception.message}")
            }
        }
    }

    private fun determineState(normalizedLandmark: List<NormalizedLandmark>) {
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
        val rightAnkle = Coordinates(normalizedLandmark[28].x(), normalizedLandmark[28].y())

        val leftElbowAngle = calculateAngle(leftWrist, leftElbow, leftShoulder)
        val rightElbowAngle = calculateAngle(rightWrist, rightElbow, rightShoulder)
        val leftHipAngle = calculateAngle(leftKnee, leftHip, leftShoulder)
        val rightHipAngle = calculateAngle(rightKnee, rightHip, rightShoulder)
        val leftKneeAngle = calculateAngle(leftAnkle, leftKnee, leftHip)
        val rightKneeAngle = calculateAngle(rightAnkle, rightKnee, rightHip)

        angles.clear()
        angles.add("%.2f".format(leftElbowAngle).toDouble())
        angles.add("%.2f".format(rightElbowAngle).toDouble())
        angles.add("%.2f".format(leftHipAngle).toDouble())
        angles.add("%.2f".format(rightHipAngle).toDouble())
        angles.add("%.2f".format(leftKneeAngle).toDouble())
        angles.add("%.2f".format(rightKneeAngle).toDouble())

        /* For 3D calculation. Doesn't seem to work yet... The z axis seems to inaccurate
        val leftWrist = Coordinates3D(
            normalizedLandmark[LEFT_WRIST].x(),
            normalizedLandmark[LEFT_WRIST].y(),
            normalizedLandmark[LEFT_WRIST].z()
        )
        val leftElbow = Coordinates3D(
            normalizedLandmark[LEFT_ELBOW].x(),
            normalizedLandmark[LEFT_ELBOW].y(),
            normalizedLandmark[LEFT_ELBOW].z()
        )
        val leftShoulder3D = Coordinates3D(
            normalizedLandmark[LEFT_SHOULDER].x(),
            normalizedLandmark[LEFT_SHOULDER].y(),
            normalizedLandmark[LEFT_SHOULDER].z()
        )
        val leftHip3D = Coordinates3D(
            normalizedLandmark[LEFT_HIP].x(),
            normalizedLandmark[LEFT_HIP].y(),
            normalizedLandmark[LEFT_HIP].z()
        )
        val leftKnee3D = Coordinates3D(
            normalizedLandmark[LEFT_KNEE].x(),
            normalizedLandmark[LEFT_KNEE].y(),
            normalizedLandmark[LEFT_KNEE].z()
        )
        val leftAnkle3D = Coordinates3D(
            normalizedLandmark[LEFT_ANKLE].x(),
            normalizedLandmark[LEFT_ANKLE].y(),
            normalizedLandmark[LEFT_ANKLE].z()
        )

        val rightWrist = Coordinates3D(
            normalizedLandmark[RIGHT_WRIST].x(),
            normalizedLandmark[RIGHT_WRIST].y(),
            normalizedLandmark[RIGHT_WRIST].z()
        )
        val rightElbow = Coordinates3D(
            normalizedLandmark[RIGHT_ELBOW].x(),
            normalizedLandmark[RIGHT_ELBOW].y(),
            normalizedLandmark[RIGHT_ELBOW].z()
        )
        val rightShoulder = Coordinates3D(
            normalizedLandmark[RIGHT_SHOULDER].x(),
            normalizedLandmark[RIGHT_SHOULDER].y(),
            normalizedLandmark[RIGHT_SHOULDER].z()
        )
        val rightHip = Coordinates3D(
            normalizedLandmark[RIGHT_HIP].x(),
            normalizedLandmark[RIGHT_HIP].y(),
            normalizedLandmark[RIGHT_HIP].z()
        )
        val rightKnee = Coordinates3D(
            normalizedLandmark[RIGHT_KNEE].x(),
            normalizedLandmark[RIGHT_KNEE].y(),
            normalizedLandmark[RIGHT_KNEE].z()
        )
        val rightAnkle = Coordinates3D(
            normalizedLandmark[RIGHT_ANKLE].x(),
            normalizedLandmark[RIGHT_ANKLE].y(),
            normalizedLandmark[RIGHT_ANKLE].z()
        )

        val leftElbowAngle = calculateAngle3D(leftWrist, leftElbow, leftShoulder3D)
        val rightElbowAngle = calculateAngle3D(rightWrist, rightElbow, rightShoulder)
        val leftHipAngle = calculateAngle3D(leftKnee, leftHip, leftShoulder)
        val rightHipAngle = calculateAngle3D(rightKnee, rightHip, rightShoulder)
        val leftKneeAngle = calculateAngle3D(leftAnkle3D, leftKnee3D, leftHip3D)
        val rightKneeAngle = calculateAngle3D(rightAnkle, rightKnee, rightHip)
        // TODO: Check for visibility and confidence
        */
        var isValidForm = true

        // Check LEFT HIP visibility then degree
        if (normalizedLandmark[LEFT_HIP].visibility().isPresent &&
            normalizedLandmark[LEFT_HIP].visibility().get() > 0.8
        ) {
            if (leftHipAngle < 160 || leftHipAngle > 190) {
                var leftHipVisibility = normalizedLandmark[LEFT_HIP].visibility().get()
                Log.d(
                    TAG,
                    "Left Hip not straight:$leftHipAngle, visibility: $leftHipVisibility"
                )
                isValidForm = false
            }
        }
        if (normalizedLandmark[RIGHT_HIP].visibility().isPresent &&
            normalizedLandmark[RIGHT_HIP].visibility().get() > 0.8
        ) {
            if (rightHipAngle < 160 || rightHipAngle > 190) {
                Log.d(TAG, "Right Hip not straight:$rightHipAngle")
                isValidForm = false
            }
        }
        if (normalizedLandmark[LEFT_KNEE].visibility().isPresent &&
            normalizedLandmark[LEFT_KNEE].visibility().get() > 0.8
        ) {
            if (leftKneeAngle < 160 || leftKneeAngle > 190) {
                Log.d(TAG, "Left Knee not straight:$leftKneeAngle")
                isValidForm = false
            }
        }
        if (normalizedLandmark[RIGHT_KNEE].visibility().isPresent &&
            normalizedLandmark[RIGHT_KNEE].visibility().get() > 0.8
        ) {
            if (rightKneeAngle < 160 || rightKneeAngle > 190) {
                Log.d(TAG, "Right Knee not straight:$rightKneeAngle")
                isValidForm = false
            }
        }

        if (isValidForm) {
            if (leftElbowAngle <= 90 && rightElbowAngle <= 90) {
                currState = DOWN_POSITION
            } else if (leftElbowAngle >= 150 && rightElbowAngle >= 150) {
                currState = UP_POSITION
            } else {
                currState = TRANSITION
            }
        } else {
            currState = INVALID_POSITION
        }
    }


    /*
        Basically determines if a push up is completed successfully.
        In the future, we should add a part where if the user breaks form for a single frame,
        it should not be counted as `breaking form` it is more likely to be an error from the
        mediapipe pose tracker model
    */
    private fun handleState() {
        if (currState == TRANSITION) {
            return
        }
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
