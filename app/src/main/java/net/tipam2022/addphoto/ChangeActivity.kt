package net.tipam2022.addphoto

import android.Manifest
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.canhub.cropper.CropImageView
import net.tipam2022.addphoto.databinding.ActivityChangeBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer


class ChangeActivity: AppCompatActivity() {
    lateinit var binding: ActivityChangeBinding
    lateinit var close: TextView
    lateinit var retake: TextView
    lateinit var useThisPhoto: Button
    lateinit var cropImageView: CropImageView
    lateinit var image: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        close = binding.close
        retake = binding.retake
        useThisPhoto = binding.save
        cropImageView = binding.cropImageView

        retake.setOnClickListener { finish() }
        useThisPhoto.setOnClickListener {save() }

        if(intent.extras!=null){
            image = intent!!.extras!!.get("currentPhoto") as Uri
            cropImageView.setImageUriAsync(image)
            cropImageView.cropShape = CropImageView.CropShape.OVAL
        }
    }

    private fun save(){
        val sharedPref = getSharedPreferences("name", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        val imageString = cropImageView.croppedImage?.toBase64()
        editor.putString("currentBadge", "$imageString")
        editor.commit()

        val intent = Intent(this, FinalActivity::class.java)
        intent.putExtra("currentBadge", imageString)
        startActivity(intent)
    }

    private fun saveMediaToStorage(bitmap: Bitmap) {
        val filename = "${System.currentTimeMillis()}.jpg"

        var fos: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this , "Captured View and saved to Gallery" , Toast.LENGTH_SHORT).show()
        }
    }

    fun Bitmap.toBase64(): String? {
        if(this!=null){
            ByteArrayOutputStream().apply {
                compress(Bitmap.CompressFormat.JPEG,60,this)
                var byteArray = this.toByteArray()
                return Base64.encodeToString(byteArray, Base64.DEFAULT)
            }
        }
        return null
    }
}