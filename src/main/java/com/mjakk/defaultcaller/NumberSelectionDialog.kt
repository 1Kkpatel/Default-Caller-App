import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import android.Manifest
import com.mjakk.defaultcaller.R

class NumberSelectionDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_number_selection, null)

        val btnSelectFromContacts = view.findViewById<Button>(R.id.btnSelectFromContacts)
        val btnEnterManually = view.findViewById<Button>(R.id.btnEnterManually)
        val etManualNumber = view.findViewById<EditText>(R.id.etManualNumber)

        btnSelectFromContacts.setOnClickListener {
            checkContactsPermission()
        }

        btnEnterManually.setOnClickListener {
            val number = etManualNumber.text.toString()
            if (number.isNotEmpty()) {
                makeCall(number)
                dismiss()
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Select or Enter Number")
            .create()
    }

    private fun checkContactsPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CODE_CONTACTS_PERMISSION)
        } else {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivityForResult(intent, REQUEST_CODE_PICK_CONTACT)
            } else {
                Toast.makeText(requireContext(), "No app found to handle contacts", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_CONTACT && resultCode == RESULT_OK && data != null) {
            val contactUri: Uri? = data.data
            if (contactUri != null) {
                val number = getContactNumber(contactUri)
                if (number != null) {
                    makeCall(number)
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "No phone number found for this contact", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getContactNumber(contactUri: Uri): String? {
        var phoneNumber: String? = null
        val cursor = requireContext().contentResolver.query(
            contactUri,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            null,
            null,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                phoneNumber = it.getString(numberIndex)
            }
        }

        return phoneNumber
    }

    private fun makeCall(number: String) {
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$number")
        startActivity(callIntent)
    }

    companion object {
        const val REQUEST_CODE_PICK_CONTACT = 1001
        const val REQUEST_CODE_CONTACTS_PERMISSION = 1002
    }
}