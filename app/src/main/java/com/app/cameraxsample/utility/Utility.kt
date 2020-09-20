package com.app.cameraxsample.utility

import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider


/** Returns true if the device has an available back camera. False otherwise */
fun ProcessCameraProvider.hasBackCamera(): Boolean {
    return hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
}

/** Returns true if the device has an available front camera. False otherwise */
fun ProcessCameraProvider.hasFrontCamera(): Boolean {
    return hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
}