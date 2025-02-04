package com.mjakk.defaultcaller

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mjakk.defaultcaller.databinding.ActivityIncomingCallBinding

class IncomingCallActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIncomingCallBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomingCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val incomingNumber = intent.getStringExtra("INCOMING_NUMBER")
        binding.tvIncomingNumber.text = incomingNumber

        binding.btnAccept.setOnClickListener {
            // Accept the call (you need to implement this logic)
            finish()
        }

        binding.btnReject.setOnClickListener {
            // Reject the call
            finish()
        }
    }
}