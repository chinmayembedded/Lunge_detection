// Copyright (c) 2024 Magic Tech Ltd

package fit.magic.cv.repcounter

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import fit.magic.cv.PoseLandmarkerHelper

var progressBarValue = 0.0F // Progress bar value (0-1)
var lungeInProgress = false // Track if a lunge is currently in progress
var lungeCompleted = false // Track if a full lunge (down and up) has been completed

data class NormalizedLandmark(val x: Float, val y: Float, val z: Float)

// Helper function to calculate the Euclidean distance between two landmarks
fun distance(point1: NormalizedLandmark, point2: NormalizedLandmark): Float {
    return Math.sqrt(
        Math.pow((point1.x() - point2.x()).toDouble(), 2.0) +
                Math.pow((point1.y() - point2.y()).toDouble(), 2.0) +
                Math.pow((point1.z() - point2.z()).toDouble(), 2.0)
    ).toFloat()
}

// Helper function to check if a leg is bent by comparing relative positions of hip, knee, and ankle
fun isLegBent(hip: NormalizedLandmark, knee: NormalizedLandmark, ankle: NormalizedLandmark): Boolean {
    // Calculate approximate distances between hip, knee, and ankle
    val hipToKneeDist = distance(hip, knee)
    val kneeToAnkleDist = distance(knee, ankle)

    // A leg is considered bent if the knee is closer to the ankle than the hip (roughly 90 degrees)
    return kneeToAnkleDist < hipToKneeDist * 0.75
}

// Helper function to check if the given landmarks are valid (e.g., they are not empty)
fun areLandmarksValid(landmarks: List<NormalizedLandmark>): Boolean {
    return landmarks.all { it.x() != 0.0f || it.y() != 0.0f || it.z() != 0.0f }
}

class ExerciseRepCounterImpl : ExerciseRepCounter() {
    override fun setResults(resultBundle: PoseLandmarkerHelper.ResultBundle) {

        if (resultBundle.results.get(0).landmarks().size > 0) {
            // Define key landmarks for left and right legs
            val currentPose = resultBundle.results.get(0).landmarks()
            val leftHip = currentPose[0][23]
            val rightHip = currentPose[0][24]
            val leftKnee = currentPose[0][25]
            val rightKnee = currentPose[0][26]
            val leftAnkle = currentPose[0][27]
            val rightAnkle = currentPose[0][28]

            // Ensure all landmarks are detected (validity check)
            if (areLandmarksValid(listOf(leftHip, rightHip, leftKnee, rightKnee, leftAnkle, rightAnkle))) {

                // Calculate leg angles for both legs (using simple distance or angles)
                sendFeedbackMessage("Assessing activity")
                val leftLegBent = isLegBent(leftHip, leftKnee, leftAnkle)
                val rightLegBent = isLegBent(rightHip, rightKnee, rightAnkle)

                // A simple rule for detecting lunge: one leg should be bent (front leg), the other straight (back leg)
                val isLunge = (leftLegBent && !rightLegBent) || (!leftLegBent && rightLegBent)

                if (isLunge) {
                    // If lunge is detected and it's not in progress, start increasing the progress bar
                    if (!lungeInProgress) {
                        lungeInProgress = true
                        lungeCompleted = false // Reset the completion status
                    }
                    // Increase progress bar as the leg bends further (up to 100%)
                    if (progressBarValue < 1) {
                        progressBarValue += 0.2F // Adjust this increment as needed
                        sendProgressUpdate(progressBarValue)
                    }
                } else if (lungeInProgress) {
                    // If the leg is returning (no lunge detected) and progress was made, decrease the progress bar
                    if (progressBarValue > 0) {
                        progressBarValue -= 0.2F // Adjust this decrement as needed
                        sendProgressUpdate(progressBarValue)
                    } else {
                        // When the progress bar reaches 0 again, mark the lunge as completed
                        lungeInProgress = false
                        lungeCompleted = true
                        incrementRepCount()
                    }
                }

            }
            else{
                sendFeedbackMessage("Required body positions not detected.")
            }
        }
        else{
            sendFeedbackMessage("Person not detected!")
        }
    }
}
