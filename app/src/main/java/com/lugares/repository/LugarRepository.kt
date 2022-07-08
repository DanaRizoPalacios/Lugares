package com.lugares.repository

import androidx.lifecycle.MutableLiveData
import com.lugares.data.LugarDao
import com.lugares.model.Lugar

class LugarRepository (private val lugarDao: LugarDao){

    fun saveLugar(lugar : Lugar){
        lugarDao.saveLugar(lugar)
    }

    fun deleteLugar(lugar : Lugar){
        lugarDao.deleteLugar(lugar)
    }

    val getAllData : MutableLiveData<List<Lugar>> = lugarDao.getAllData()
}