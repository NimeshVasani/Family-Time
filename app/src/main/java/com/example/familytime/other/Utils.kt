package com.example.familytime.other

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import java.util.regex.Pattern

object Utils {

    var currentUserUID: String = ""

    fun validateEmail(email: String): Boolean {
        val emailPattern = Pattern.compile(
            "^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$"
        )
        return emailPattern.matcher(email).matches()
    }

    fun validatePassword(password: String): Boolean {
        return password.length >= 6
    }

    fun validateTwoPasswords(password1: String, password2: String): Boolean {
        return password1.length >= 6 && password1 == password2
    }


    private val dateFormat: SimpleDateFormat
        get() = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    private val timeFormat: SimpleDateFormat
        get() = SimpleDateFormat("hh:mm a", Locale.getDefault())

    private val currentDateLiveData: MutableLiveData<String> = MutableLiveData()
    private val currentTimeLiveData: MutableLiveData<String> = MutableLiveData()

    init {
        // Start updating date and time immediately
        updateDateAndTime()
    }

    fun getCurrentDateLiveData(): LiveData<String> = currentDateLiveData

    fun getCurrentTimeLiveData(): LiveData<String> = currentTimeLiveData

    private fun updateDateAndTime() {
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val currentDate = Calendar.getInstance().time
                val currentTime = Calendar.getInstance().time

                currentDateLiveData.postValue(dateFormat.format(currentDate))
                currentTimeLiveData.postValue(timeFormat.format(currentTime))
            }
        }, 0, 1000) // Update every second (adjust as needed)
    }
}