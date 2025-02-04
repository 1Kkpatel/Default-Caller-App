package com.mjakk.defaultcaller

import NumberSelectionDialog
import NumberSelectionDialog.Companion.REQUEST_CODE_CONTACTS_PERMISSION
import NumberSelectionDialog.Companion.REQUEST_CODE_PICK_CONTACT
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.mjakk.defaultcaller.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val REQUEST_PERMISSIONS = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check and request permissions
        checkAndRequestPermissions()

        // Set up RecyclerView for Call Logs
        val callLogs = getCallLogs()
        val callLogAdapter = CallLogAdapter(callLogs)
        binding.recyclerViewCallLogs.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewCallLogs.adapter = callLogAdapter

        // Set up RecyclerView for Contacts
        val contacts = getContacts()
        val contactAdapter = ContactAdapter(contacts)
        binding.recyclerViewContacts.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewContacts.adapter = contactAdapter

        // Handle Outgoing Call Button
        binding.btnCall.setOnClickListener {
            val dialog = NumberSelectionDialog()
            dialog.show(supportFragmentManager, "NumberSelectionDialog")
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        val requiredPermissions = arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS
        )

        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission)
            }
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), REQUEST_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_CONTACTS_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with contact selection
                val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                startActivityForResult(intent, REQUEST_CODE_PICK_CONTACT)
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Contacts permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCallLogs(): List<String> {
        val callLogs = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            val cursor = contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(CallLog.Calls.NUMBER),
                null,
                null,
                null
            )

            cursor?.use {
                val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
                if (numberIndex != -1) {
                    while (it.moveToNext()) {
                        val number = it.getString(numberIndex) ?: "Unknown"
                        callLogs.add(number)
                    }
                }
            }
        }

        return callLogs
    }

    private fun getContacts(): List<String> {
        val contacts = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            val cursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                arrayOf(ContactsContract.Contacts.DISPLAY_NAME),
                null,
                null,
                null
            )

            cursor?.use {
                val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                if (nameIndex != -1) {
                    while (it.moveToNext()) {
                        val name = it.getString(nameIndex) ?: "Unknown"
                        contacts.add(name)
                    }
                }
            }
        }

        return contacts
    }

    private fun getContactNumber(contactUri: Uri?): String? {
        if (contactUri == null) return null

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, // Fix: Query correct table
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactUri.lastPathSegment),
            null
        )

        cursor?.use {
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            if (numberIndex != -1 && it.moveToFirst()) {
                return it.getString(numberIndex)
            }
        }

        return null
    }
}
