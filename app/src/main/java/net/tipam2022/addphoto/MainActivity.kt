package net.tipam2022.addphoto

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import net.tipam2022.addphoto.databinding.ActivityMainBinding
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    lateinit var addPhoto: Button
    lateinit var binding: ActivityMainBinding
    var sharedPrefFile: String = "sharedPrefFile"
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        addPhoto = binding.addPhoto
        sharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)

        addPhoto.setOnClickListener {
            try{
                val intent: Intent = Intent(this, PreviewActivity::class.java)
                startActivity(intent)
            }catch (e: Exception){
                Toast.makeText(this, "${e.message}", Toast.LENGTH_LONG*20).show()
            }
        }
    }

}