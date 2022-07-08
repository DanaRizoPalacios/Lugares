package com.lugares.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.ktx.Firebase
import com.lugares.model.Lugar

class LugarDao {

    private val coleccion1 = "lugaresApp"
    private val usuario = Firebase.auth.currentUser?.email.toString()
    private val coleccion2 = "misLugares"
    private  var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    init{
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
    }

    fun getAllData() : MutableLiveData<List<Lugar>>{
        val listaLugares = MutableLiveData<List<Lugar>>()
        firestore.collection(coleccion1).document(usuario).collection(coleccion2)
            .addSnapshotListener { instantanea, e ->
                if(e != null){ //se valida si hay algune error en la captura de documentos
                    return@addSnapshotListener
                }
                if(instantanea != null){ //Si hay info recuperada...
                    //Recorro la instantanea (documentos) para crear la lista de lugares
                    val lista =  ArrayList<Lugar>()
                    instantanea.documents.forEach {
                        val lugar = it.toObject(Lugar::class.java)
                        if (lugar != null) { lista.add(lugar) }
                    }
                    listaLugares.value=lista
                }
            }
        return listaLugares
    }

    fun saveLugar(lugar : Lugar){
        val documento: DocumentReference
        if(lugar.id.isEmpty()){ //si id no tiene valor entonces es un documento nuevo
            documento = firestore.collection(coleccion1).document(usuario).collection(coleccion2).document()
            lugar.id = documento.id
        }else{ // si el id tiene valor... el documento existe y recupero la info de el
            documento = firestore.collection(coleccion1).document(usuario)
                .collection(coleccion2).document(lugar.id)
        }
        documento.set(lugar)
            .addOnSuccessListener { Log.d("saveLugar", "Se creo o modifico un lugar") }
            .addOnCanceledListener { Log.d("saveLugar", "No se creo o modifico un lugar") }
    }

    fun deleteLugar(lugar : Lugar){
        if(lugar.id.isNotEmpty()) {  //si el id tiene valor entonces podenis eliminar lugar porque existe
             firestore.collection(coleccion1).document(usuario)
                .collection(coleccion2).document(lugar.id).delete()
                 .addOnSuccessListener { Log.d("deleteLugar", "Se elimino un lugar") }
                 .addOnCanceledListener { Log.d("deleteLugar", "No se elimino un lugar") }
        }
    }


}