package com.example.henrik_sachdeva_myruns4

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class CommentsDialogFragment : DialogFragment(), DialogInterface.OnClickListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_comments_dialog, null)

        return AlertDialog.Builder(requireActivity())
            .setTitle("Comments")
            .setView(view)
            .setPositiveButton("OK", this)
            .setNegativeButton("CANCEL", this)
            .create()
    }

    override fun onClick(dialogInterface: DialogInterface, id: Int) {
        when (id) {
            DialogInterface.BUTTON_POSITIVE ->
                Toast.makeText(activity, "Comment saved", Toast.LENGTH_SHORT).show()

            DialogInterface.BUTTON_NEGATIVE ->
                Toast.makeText(activity, "Cancelled", Toast.LENGTH_SHORT).show()
        }
    }
}
