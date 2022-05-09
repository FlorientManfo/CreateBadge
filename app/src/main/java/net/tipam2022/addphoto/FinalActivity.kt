package net.tipam2022.addphoto

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import net.tipam2022.addphoto.databinding.ActivityFinalBinding

class FinalActivity : AppCompatActivity() {
    lateinit var binding: ActivityFinalBinding
    lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityFinalBinding.inflate(layoutInflater)
        imageView = binding.currentBadge
        if(intent.extras!=null){
            val bitmapImage = intent!!.extras!!.getString("currentBadge")?.toBitmap()
            imageView.setImageBitmap(bitmapImage)
        }
        setContentView(binding.root)
    }

    fun String?.toBitmap(): Bitmap?{
        var imageBytes = Base64.decode(this, 0)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
}