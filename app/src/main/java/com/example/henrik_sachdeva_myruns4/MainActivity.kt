package com.example.henrik_sachdeva_myruns4

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
    private var selectedImageUri: Uri? = null

    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    private var saveCheck = 0
    private var picOption = 0

    private val fileName = "profile_pic.jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        sharedPreferences = getSharedPreferences("appPrefs", MODE_PRIVATE)

        Util.checkPermissions(this)
        setupImageFiles()
        setupActivityResultLaunchers()

        if (sharedPreferences.getInt("saveCheck", 0) == 1) {
            loadSavedData()
        }

        photoButton.setOnClickListener { showPhotoOptions() }

        saveButton.setOnClickListener {
            handleSave()
            finish()
        }

        cancelButton.setOnClickListener { finish() }
    }

    private fun bindViews() {
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
    }

    private fun setupImageFiles() {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.apply {
            if (!exists()) mkdirs()
        }

        val profilePicFile = File(storageDir, fileName)
        val newImageFile = File(storageDir, "new_pic.jpg")

        tempImgUri = FileProvider.getUriForFile(
            this,
            "com.example.henrik_sachdeva_myruns4.fileprovider",
            profilePicFile
        )

        newImgUri = FileProvider.getUriForFile(
            this,
            "com.example.henrik_sachdeva_myruns4.fileprovider",
            newImageFile
        )
    }

    private fun setupActivityResultLaunchers() {
        cameraResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    imageProfile.setImageBitmap(Util.getBitmap(this, newImgUri))
                    picOption = 1
                }
            }

        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    selectedImageUri = result.data?.data
                    selectedImageUri?.let { uri ->
                        imageProfile.setImageBitmap(Util.getBitmap(this, uri))
                        picOption = 2
                    }
                }
            }
    }

    private fun showPhotoOptions() {
        val options = arrayOf("Open Camera", "Select from Gallery")

        AlertDialog.Builder(this)
            .setTitle("Pick Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun handleSave() {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val profilePicFile = File(storageDir, fileName)

        when (picOption) {
            1 -> copyFile(newImgUri, profilePicFile)
            2 -> selectedImageUri?.let { copyFile(it, profilePicFile) }
        }

        saveData()
    }

    private fun saveData() {
        saveCheck = 1
        sharedPreferences.edit().apply {
            putString("name", editName.text.toString())
            putString("email", editEmail.text.toString())
            putString("phone", editTextPhone.text.toString())
            putString("class", editClass.text.toString())
            putString("major", editMajor.text.toString())
            putString("selectedGender", getSelectedGender(radioGroupGender))
            putInt("saveCheck", saveCheck)
            apply()
        }
    }

    private fun loadSavedData() {
        editName.setText(sharedPreferences.getString("name", ""))
        editEmail.setText(sharedPreferences.getString("email", ""))
        editTextPhone.setText(sharedPreferences.getString("phone", ""))
        editClass.setText(sharedPreferences.getString("class", ""))
        editMajor.setText(sharedPreferences.getString("major", ""))

        setRadioGroupSelection(
            radioGroupGender,
            sharedPreferences.getString("selectedGender", "")
        )

        val tempImgFile = File(tempImgUri.path ?: "")
        if (tempImgFile.exists()) {
            imageProfile.setImageBitmap(Util.getBitmap(this, tempImgUri))
        }
    }

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

    private fun getSelectedGender(group: RadioGroup): String =
        when (group.checkedRadioButtonId) {
            R.id.radioButtonMale -> "Male"
            R.id.radioButtonFemale -> "Female"
            else -> ""
        }

    private fun setRadioGroupSelection(group: RadioGroup, gender: String?) {
        when (gender) {
            "Male" -> group.check(R.id.radioButtonMale)
            "Female" -> group.check(R.id.radioButtonFemale)
            else -> group.clearCheck()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, newImgUri)
        cameraResult.launch(intent)
    }
}
