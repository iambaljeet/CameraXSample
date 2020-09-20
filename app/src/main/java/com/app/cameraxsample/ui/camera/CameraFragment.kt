package com.app.cameraxsample.ui.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.app.cameraxsample.R
import com.app.cameraxsample.utility.hasBackCamera
import com.app.cameraxsample.utility.hasFrontCamera
import com.app.cameraxsample.view.CameraButtonEvents
import kotlinx.android.synthetic.main.fragment_camera.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "CameraFragment"
class CameraFragment : Fragment(), CameraButtonEvents {
    private var displayId: Int = -1
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private lateinit var viewFinder: PreviewView
    private lateinit var cameraExecutor: ExecutorService

    private val result = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        var permissionsGranter = true
        permissions.entries.forEach {
            if (!it.value) {
                permissionsGranter = false
                Toast.makeText(context, "Storage permissions not granter", Toast.LENGTH_SHORT).show()
            }
        }
        if (permissionsGranter) {
            // Executor for camera
            cameraExecutor = Executors.newSingleThreadExecutor()

            // Wait for the views to be properly laid out
            viewFinder.post {

                // Keep track of the display in which this view is attached
                displayId = viewFinder.display.displayId

                // Set up the camera and its use cases
                setUpCamera()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        result.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))

        toggle_button_switch_camera.setOnClickListener {

            lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
            // Re-bind use cases to update selected camera
            bindCameraUseCases()
        }

        viewFinder = preview_view
    }

    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            lensFacing = when {
                cameraProvider?.hasBackCamera() ?: false -> { CameraSelector.LENS_FACING_BACK }
                cameraProvider?.hasFrontCamera() ?: false ->  { CameraSelector.LENS_FACING_FRONT }
                else -> throw IllegalStateException("Back and front camera are unavailable")
            }

            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        preview = Preview.Builder()
            .build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        videoCapture = VideoCapture.Builder()
            .build()

        cameraProvider.unbindAll()

        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, videoCapture)
        preview?.setSurfaceProvider(viewFinder.surfaceProvider)
    }

    override fun capturePicture() {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "capturedImage")
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")

        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
            requireContext().contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues).build()

        imageCapture?.takePicture(outputFileOptions, cameraExecutor, object: ImageCapture.OnImageCapturedCallback(),
            ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                Log.e(TAG, "onImageSaved outputFileResults: $outputFileResults")
            }

            override fun onError(exception: ImageCaptureException) {
                exception.printStackTrace()
                Log.e(TAG, "onError exception: $exception")
                super.onError(exception)
            }
        })
    }

    @SuppressLint("RestrictedApi")
    override fun startVideoCapture() {

        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "recordedVideo")
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")

        val outputFileOptions = VideoCapture.OutputFileOptions.Builder(
            requireContext().contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            contentValues).build()

        videoCapture?.startRecording(outputFileOptions, cameraExecutor, object: VideoCapture.OnVideoSavedCallback {
            override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                Log.e(TAG, "onVideoSaved outputFileResults: $outputFileResults")
            }

            override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                cause?.printStackTrace()
                Log.e(TAG, "onError message: $message cause: $cause")
            }
        })
    }

    @SuppressLint("RestrictedApi")
    override fun stopVideoCapture() {
        videoCapture?.stopRecording()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        @JvmStatic
        fun newInstance() = CameraFragment()
    }
}