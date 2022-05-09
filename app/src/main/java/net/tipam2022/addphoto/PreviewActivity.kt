package net.tipam2022.addphoto

import android.Manifest
import android.R.attr
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.ListenableFuture
import net.tipam2022.addphoto.databinding.ActivityPreviewBinding
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class PreviewActivity: AppCompatActivity() {
    lateinit var binding: ActivityPreviewBinding
    lateinit var close: TextView
    lateinit var takePhoto: Button
    lateinit var photoLibrary: Button
    private val IMAGE_PICK_CODE = 2
    private val PERMISSION_CODE = 1001;
    lateinit var mode: ImageButton
    lateinit var preview: PreviewView
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector:CameraSelector
    private var imageCapture: ImageCapture? = null
    private var currentImage: Uri? = null
    private lateinit var imgCaptureExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        close = binding.close
        takePhoto = binding.takePhoto
        photoLibrary = binding.photoLibrary
        preview = binding.preview
        mode = binding.changeMode

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        //To switch between font and back camera
        mode.setOnClickListener {
            if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            }
            else
                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            startCamera()
        }

        startCamera()

        cameraProviderResult.launch(android.Manifest.permission.CAMERA)

        imgCaptureExecutor = Executors.newSingleThreadExecutor()

        photoLibrary.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED){
                    //permission denied
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                    //show popup to request runtime permission
                    requestPermissions(permissions, PERMISSION_CODE);
                }
                else{
                    //permission already granted
                    openGallery();
                }
            }
            else{
                //system OS is < Marshmallow
                openGallery();
            }
        }
        takePhoto.setOnClickListener{
            takePhoto()
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                animateFlash()
            }
        }
        close.setOnClickListener { finish()}

    }

    private fun startCamera(){
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // connecting a preview use case to the preview in the xml file.
            val preview = Preview.Builder().build().also{
                it.setSurfaceProvider(binding.preview.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()
            try{
                // clear all the previous use cases first.
                cameraProvider.unbindAll()
                // binding the lifecycle of the camera to the lifecycle of the application.
                cameraProvider.bindToLifecycle(this,cameraSelector,preview, imageCapture)
            } catch (e: Exception) {
                Log.d(TAG, "Use case binding failed")
            }

        },ContextCompat.getMainExecutor(this))
    }

    //Waiting for camera access permission
    private val cameraProviderResult = registerForActivityResult(ActivityResultContracts.RequestPermission()){ permissionGranted->
        if(permissionGranted){
            // cut and paste the previous startCamera() call here.
            startCamera()
        }else {
            Snackbar.make(binding.root,"The camera permission is required", Snackbar.LENGTH_INDEFINITE).show()
        }
    }

    companion object{
        val TAG = "PreviewActivity"
    }

    private fun takePhoto(){
        imageCapture?.let{
            //Create a storage location whose fileName is timestamped in milliseconds.
            val fileName = "JPEG_${System.currentTimeMillis()}"
            val file = File(externalMediaDirs[0],fileName)

            // Save the image in the above file
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()

            /* pass in the details of where and how the image is taken.(arguments 1 and 2 of takePicture)
            pass in the details of what to do after an image is taken.(argument 3 of takePicture) */

            it.takePicture(
                outputFileOptions,
                imgCaptureExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults){
                        Log.i(TAG,"The image has been saved in ${file.toUri()}")
                        currentImage = file.toUri()
                        editPhoto()
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(
                            binding.root.context,
                            "Error taking photo",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.d(TAG, "Error taking photo:$exception")
                    }

                })
        }
    }

    private fun openGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun animateFlash() {
        binding.root.postDelayed({
            binding.root.foreground = ColorDrawable(Color.WHITE)
            binding.root.postDelayed({
                binding.root.foreground = null
            }, 50)
        }, 100)
    }

    private fun editPhoto(){
        var intent = Intent(this, ChangeActivity::class.java)
        intent.putExtra("currentPhoto", currentImage)
        startActivity(intent)
    }

    //handle requested permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size >0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted
                    openGallery()
                }
                else{
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
                currentImage = data?.data
                editPhoto()
        }
    }
}