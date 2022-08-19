package com.lugares.ui.lugar

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.lugares.R
import com.lugares.databinding.FragmentAddLugarBinding
import com.lugares.databinding.FragmentLugarBinding
import com.lugares.model.Lugar
import com.lugares.utiles.AudioUtiles
import com.lugares.utiles.ImagenUtiles
import com.lugares.viewmodel.LugarViewModel

class AddLugarFragment : Fragment() {
    private var _binding: FragmentAddLugarBinding? = null
    private val binding get() = _binding!!
    private lateinit var lugarViewModel: LugarViewModel

    private  lateinit var audioUtiles: AudioUtiles

    private lateinit var imagenUtiles: ImagenUtiles
    private lateinit var tomarFotoActivity: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        lugarViewModel =
            ViewModelProvider(this)[LugarViewModel::class.java]
        _binding = FragmentAddLugarBinding.inflate(inflater, container, false)
        binding.btAgregar.setOnClickListener {
            binding.progressBar.visibility = ProgressBar.VISIBLE
            binding.msgMensaje.text = getString(R.string.msg_subiendo_audio)
            binding.msgMensaje.visibility = TextView.VISIBLE
            subeAudio()
        }

        ubicaGPS()

        audioUtiles = AudioUtiles(
            requireActivity(),
            requireContext(),
            binding.btAccion,
            binding.btPlay,
            binding.btDelete,
            getString(R.string.msg_graba_audio),
            getString(R.string.msg_detener_audio))

        tomarFotoActivity = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){
            if (it.resultCode == Activity.RESULT_OK){
                imagenUtiles.actualizaFoto()
            }
        }
        imagenUtiles = ImagenUtiles(
            requireContext(),
            binding.btPhoto,
            binding.btRotaL,
            binding.btRotaR,
            binding.imagen,
            tomarFotoActivity)


        return binding.root
    }

    private fun subeAudio() {
        val audioFile = audioUtiles.audioFile
        if(audioFile != null && audioFile.exists() && audioFile.isFile && audioFile.canRead()){
        //Si entra al if, podemos subir el audio a la nube
            var usuario = Firebase.auth.currentUser?.email
            val rutaNube = "lugaresApp/${usuario}/audios/${audioFile.name}"

            val  rutaLocal = Uri.fromFile(audioFile)

            var referencia: StorageReference = Firebase.storage.reference.child(rutaNube)

            referencia.putFile(rutaLocal)
                .addOnSuccessListener {
                    referencia.downloadUrl.addOnSuccessListener {
                        val rutaAudio = it.toString()
                        subeImagen(rutaAudio)
                    }
                }
                .addOnFailureListener{
                    subeImagen("")
                }

        }else{
            //No hay audio, no se sube
            subeImagen("")
        }

    }

    private fun subeImagen(rutaAudio: String) {
        if (imagenUtiles.getFotoTomada()) {
            binding.msgMensaje.text = getString(R.string.msg_subiendo_imagen)
            val imagenFile = imagenUtiles.imagenFile
            if (imagenFile != null && imagenFile.exists() && imagenFile.isFile && imagenFile.canRead()) {
                // Si entra al if, podemos subir la imagen a la nube
                var usuario = Firebase.auth.currentUser?.email
                val rutaNube = "lugaresApp/${usuario}/imagenes/${imagenFile.name}"

                val rutaLocal = Uri.fromFile(imagenFile)

                var referencia: StorageReference = Firebase.storage.reference.child(rutaNube)

                referencia.putFile(rutaLocal)
                    .addOnSuccessListener {
                        referencia.downloadUrl.addOnSuccessListener {
                            val rutaImagen = it.toString()
                            addLugar(
                                rutaAudio,
                                rutaImagen
                            )   //Finalmente se graba la info del lugar
                        }
                    }
                    .addOnFailureListener {
                        addLugar(rutaAudio, "")
                    }
            } else {
                //No hay imagen... no se sube...
                addLugar(rutaAudio, "")
            }
        } else {
            addLugar(rutaAudio, "")
        }
    }

    private fun ubicaGPS() {
        val ubicacion: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        if(ActivityCompat.checkSelfPermission(requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ){//sin permiso, solicita
            ActivityCompat.requestPermissions(requireActivity(),
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION), 105)
        }else{
            ubicacion.lastLocation.addOnSuccessListener { location: Location? ->
                if(location!=null){
                    binding.tvLatitud.text = "${location.latitude}"
                    binding.tvLongitud.text = "${location.longitude}"
                    binding.tvAltura.text = "${location.altitude}"

                }else{
                    binding.tvLatitud.text = "0.00"
                    binding.tvLongitud.text = "0.00"
                    binding.tvAltura.text = "0.00"
                }
            }

        }


    }

    private fun addLugar(rutaAudio: String,  rutaImagen: String) {
        val nombre = binding.etNombre.text.toString()
        val correo = binding.etCorreo.text.toString()
        val telefono = binding.etTelefono.text.toString()
        val web = binding.etWeb.text.toString()
        val latitud = binding.tvLatitud.text.toString().toDouble()
        val longitud = binding.tvLongitud.text.toString().toDouble()
        val altura = binding.tvAltura.text.toString().toDouble()
        if (nombre.isNotEmpty()) { //Si puedo crear un lugar
            val lugar= Lugar("",nombre,correo,telefono,web,latitud,
                longitud,altura,rutaAudio, rutaImagen)
            lugarViewModel.saveLugar(lugar)
            Toast.makeText(requireContext(),getString(R.string.msg_lugar_added),Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_addLugarFragment_to_nav_lugar)
        } else {  //Mensaje de error...
            Toast.makeText(requireContext(),getString(R.string.msg_data),Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}