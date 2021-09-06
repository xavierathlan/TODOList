package com.edson.todolist.views

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class CreateTaskViewModel : ViewModel() {
    private val mColor : MutableLiveData<Int> = MutableLiveData(2)
    private val mIcon : MutableLiveData<Int> = MutableLiveData(1)

    private val mDate : MutableLiveData<Date> = MutableLiveData()

    private val mDone: MutableLiveData<Boolean> = MutableLiveData(false)

    private val mInCalendar: MutableLiveData<Boolean> = MutableLiveData(false)
    private val mHasTime: MutableLiveData<Boolean> = MutableLiveData(false)

    private val mChangeDateFlag: MutableLiveData<Int> = MutableLiveData(0)

    fun getColor(): Int? = mColor.value
    fun getIcon(): Int? = mIcon.value

    fun getDate(): Date? = mDate.value

    fun getHour(): Int? {
        var ret: Int? = null
        if (mHasTime.value != null && mDate.value != null) {
            val calendar = Calendar.getInstance()
            calendar.time = mDate.value!!
            ret = calendar.get(Calendar.HOUR_OF_DAY)
        }
        return ret
    }

    fun getMinutes(): Int? {
        var ret: Int? = null
        if (mHasTime.value != null && mDate.value != null) {
            val calendar = Calendar.getInstance()
            calendar.time = mDate.value!!
            ret = calendar.get(Calendar.MINUTE)
        }
        return ret
    }

    fun getDone(): Boolean? = mDone.value!!
    fun getInCalendar(): Boolean? = mInCalendar.value!!

    fun getChangeDateFlag(): Int? = mChangeDateFlag.value!!

    fun setColor(value: Int) { mColor.value = value }
    fun setIcon(value: Int) { mIcon.value = value }

    fun setDate(value: Date) {
        mChangeDateFlag.value = 1
        mDate.value = value
    }

    fun setTime(hourOfDay: Int, minute: Int) {
        mDate.value?.let {
            val calendar = Calendar.getInstance()
            calendar.time = it
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            mChangeDateFlag.value = 2
            mDate.value = calendar.time
            mHasTime.value = true
        }
    }

    fun setDone(value: Boolean) {
        mDone.value = value
    }

    fun setInCalendar(value: Boolean) {
        mInCalendar.value = value
    }

    fun setChangeDateFlag(value: Int) {
        mChangeDateFlag.value = value
    }

    fun observeDate(callback: (Date) -> Unit) {
        mDate.observeForever(callback)
    }

    fun observeColor(callback: (Int) -> Unit) {
        mColor.observeForever(callback)
    }

    fun observeIcon(callback: (Int) -> Unit) {
        mIcon.observeForever(callback)
    }

    fun observeDone(callback: (Boolean) -> Unit) {
        mDone.observeForever(callback)
    }

    fun observeInCalendar(callback: (Boolean) -> Unit) {
        mInCalendar.observeForever(callback)
    }
}