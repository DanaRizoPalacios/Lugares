package com.lugares.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.lugares.data.LugarDao
import com.lugares.model.Lugar
import com.lugares.repository.LugarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LugarViewModel(application: Application) : AndroidViewModel(application) {
    //Atributo para obtener la lista de lugares en un ArrayList especial
    val getAllData: MutableLiveData<List<Lugar>>
    //Atributo para acceder al repositorio de Lugar
    private val repository: LugarRepository = LugarRepository(LugarDao())

    //Bloque de inicialización de los atributos
    init {  getAllData = repository.getAllData  }

    fun saveLugar(lugar: Lugar) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveLugar(lugar)
        }
    }

    fun deleteLugar(lugar: Lugar) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteLugar(lugar)
        }
    }
}