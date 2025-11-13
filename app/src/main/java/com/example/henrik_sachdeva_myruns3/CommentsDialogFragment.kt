package com.example.henrik_sachdeva_myruns3

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

/**
 * Dialog fragment for collecting or displaying user comments.
 */
class CommentsDialogFragment : DialogFragment(), DialogInterface.OnClickListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val dialogView = requireActivity()
            .layoutInflater
            .inflate(R.layout.fragment_comments_dialog, null)

        builder.setView(dialogView)
            .setTitle("Comments")
            .setPositiveButton("OK", this)
            .setNegativeButton("CANCEL", this)

        return builder.create()
    }

    override fun onClick(dialogInterface: DialogInterface, id: Int) {
        when (id) {
            DialogInterface.BUTTON_POSITIVE -> {
                Toast.makeText(activity, "Comment saved", Toast.LENGTH_SHORT).show()
            }
            DialogInterface.BUTTON_NEGATIVE -> {
                Toast.makeText(activity, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
