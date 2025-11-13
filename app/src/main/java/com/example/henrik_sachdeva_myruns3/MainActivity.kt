package com.example.henrik_sachdeva_myruns3

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Main activity for user profile setup and picture selection.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var imageProfile: ImageView
    private lateinit var photoButton: Button
    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editTextPhone: EditText
    private lateinit var radioGroupGender: RadioGroup
    private lateinit var editClass: EditText
    private lateinit var editMajor: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var tempImgUri: Uri
    private lateinit var newImgUri: Uri
    private lateinit var selectedImageUri: Uri
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    private var saveCheck = 0
    private var picOption = 0

    private val fileName = "profile_pic.jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        imageProfile = findViewById(R.id.imageProfile)
        photoButton = findViewById(R.id.photoButton)
        editName = findViewById(R.id.editName)
        editEmail = findViewById(R.id.editEmail)
        editTextPhone = findViewById(R.id.editTextPhone)
        radioGroupGender = findViewById(R.id.radioGroupGender)
        editClass = findViewById(R.id.editClass)
        editMajor = findViewById(R.id.editMajor)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
        sharedPreferences = getSharedPreferences("appPrefs", MODE_PRIVATE)

        Util.checkPermissions(this)

        // Prepare image storage and file URIs
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.apply {
            if (!exists()) mkdirs()
        }
        val profilePicFile = File(storageDir, fileName)
        val newImageFile = File(storageDir, "new_pic.jpg")

        tempImgUri = FileProvider.getUriForFile(this, "com.example.myruns3.fileprovider", profilePicFile)
        newImgUri = FileProvider.getUriForFile(this, "com.example.myruns3.fileprovider", newImageFile)

        // Load saved data if available
        saveCheck = sharedPreferences.getInt("saveCheck", 0)
        if (saveCheck == 1) loadSavedData()

        // Register camera and gallery launchers
        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val bitmap = Util.getBitmap(this, newImgUri)
                imageProfile.setImageBitmap(bitmap)
                picOption = 1
            }
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                selectedImageUri = data?.data ?: Uri.EMPTY
                val bitmap = Util.getBitmap(this, selectedImageUri)
                imageProfile.setImageBitmap(bitmap)
                picOption = 2
            }
        }

        // Allow user to choose between camera or gallery
        photoButton.setOnClickListener {
            val options = arrayOf("Open Camera", "Select from Gallery")
            AlertDialog.Builder(this)
                .setTitle("Pick Profile Picture")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> openCamera()
                        1 -> openGallery()
                    }
                }.show()
        }

        // Save profile data and image
        saveButton.setOnClickListener {
            when (picOption) {
                1 -> copyFile(newImgUri, profilePicFile) // Camera photo
                2 -> copyFile(selectedImageUri, profilePicFile) // Gallery photo
            }
            saveData()
            finish()
        }

        // Cancel and close activity
        cancelButton.setOnClickListener { finish() }
    }

    /**
     * Save user input and profile state to SharedPreferences.
     */
    private fun saveData() {
        saveCheck = 1
        val editor = sharedPreferences.edit()
        editor.putString("name", editName.text.toString())
        editor.putString("email", editEmail.text.toString())
        editor.putString("phone", editTextPhone.text.toString())
        editor.putString("class", editClass.text.toString())
        editor.putString("major", editMajor.text.toString())
        editor.putInt("saveCheck", saveCheck)
        editor.putString("selectedGender", getSelectedGender(radioGroupGender))
        editor.apply()
    }

    /**
     * Load stored user profile data and image.
     */
    private fun loadSavedData() {
        editName.setText(sharedPreferences.getString("name", ""))
        editEmail.setText(sharedPreferences.getString("email", ""))
        editTextPhone.setText(sharedPreferences.getString("phone", ""))
        editClass.setText(sharedPreferences.getString("class", ""))
        editMajor.setText(sharedPreferences.getString("major", ""))
        setRadioGroupSelection(radioGroupGender, sharedPreferences.getString("selectedGender", ""))

        val tempImgFile = File(tempImgUri.path)
        if (tempImgFile.exists()) {
            val bitmap = Util.getBitmap(this, tempImgUri)
            imageProfile.setImageBitmap(bitmap)
        }
    }

    /**
     * Copies an image from the given source URI to a destination file.
     */
    private fun copyFile(sourceUri: Uri, destinationFile: File) {
        contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(destinationFile).use { output ->
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }
            }
        }
    }

    private fun getSelectedGender(group: RadioGroup): String {
        return when (group.checkedRadioButtonId) {
            R.id.radioButtonMale -> "Male"
            R.id.radioButtonFemale -> "Female"
            else -> ""
        }
    }

    private fun setRadioGroupSelection(group: RadioGroup, gender: String?) {
        when (gender) {
            "Male" -> group.check(R.id.radioButtonMale)
            "Female" -> group.check(R.id.radioButtonFemale)
            else -> group.clearCheck()
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, newImgUri)
        cameraResult.launch(cameraIntent)
    }
}
