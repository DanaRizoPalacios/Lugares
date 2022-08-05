package com.lugares.ui.lugar

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.lugares.R
import com.lugares.databinding.FragmentUpdateLugarBinding
import com.lugares.model.Lugar
import com.lugares.viewmodel.LugarViewModel

class UpdateLugarFragment : Fragment() {

    //se deciden los parametros por argumento
    private val args by navArgs<UpdateLugarFragmentArgs>()
    private var _binding: FragmentUpdateLugarBinding? = null
    private val binding get() = _binding!!
    private lateinit var lugarViewModel: LugarViewModel

    //Ojeto para escuchar audio almacenao en la nube
    private  lateinit var  mediaPlayer: MediaPlayer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        lugarViewModel =
            ViewModelProvider(this)[LugarViewModel::class.java]
        _binding = FragmentUpdateLugarBinding.inflate(inflater, container, false)

        //Coloco la info del lugar en los campos del fragmento para modificar
        binding.etNombre.setText(args.lugar.nombre)
        binding.etCorreo.setText(args.lugar.correo)
        binding.etTelefono.setText(args.lugar.telefono)
        binding.etWeb.setText(args.lugar.web)
        binding.tvAltura.text=args.lugar.altura.toString()
        binding.tvLatitud.text=args.lugar.latitud.toString()
        binding.tvLongitud.text=args.lugar.longitud.toString()

        if(args.lugar.rutaAudio?.isNotEmpty()==true) {
            //hay ruta de audio
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(args.lugar.rutaAudio)
            mediaPlayer.prepare()
            binding.btPlay.isEnabled=true
        }else{
            //no hay ruta de audio
            binding.btPlay.isEnabled=false
        }

        //hace que suene el audio
        binding.btPlay.setOnClickListener { mediaPlayer.start() }

        //se trabaja en la imagen
        if(args.lugar.rutaImagen?.isNotEmpty()==true){
            Glide.with(requireContext())
                .load(args.lugar.rutaImagen)
                .fitCenter()
                .into(binding.imagen)
        }

        binding.btUpdateLugar.setOnClickListener { updateLugar() }
        binding.btEmail.setOnClickListener { escribirCorreo() }
        binding.btPhone.setOnClickListener { realizarLlamada() }
        binding.btWeb.setOnClickListener { verWeb() }
        binding.btUpdateLugar.setOnClickListener {
            updateLugar()

        }
        setHasOptionsMenu(true)
        //esta pantalla tiene menu personalizadp

        return binding.root
    }

    private fun enviarWhatsApp() {
        val telefono = binding.etTelefono.text
        if (telefono.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = "whatsapp://send?phone=506$telefono&text="+
                    getString(R.string.msg_saludos)
            intent.setPackage("com.whatsapp")
            intent.data=Uri.parse(uri)
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(),getString(R.string.msg_datos),
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun verMapa() {
        val latitud=binding.tvLatitud.text.toString().toDouble()
        val longitud=binding.tvLongitud.text.toString().toDouble()
        if (latitud.isFinite() && longitud.isFinite()) {
            val location = Uri.parse("geo:$latitud,$longitud?z=18")
            val intent = Intent(Intent.ACTION_VIEW,location)
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(),getString(R.string.msg_datos),
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun verWeb() {
        val recurso = binding.etWeb.text.toString()
        if(recurso.isNotEmpty()){
            val accion = Intent(Intent.ACTION_VIEW, Uri.parse("http://$recurso"))
            startActivity(accion)//efectivamente se carga el app de correo
        }else{
            Toast.makeText(requireContext(),getString(R.string.msg_datos),
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun realizarLlamada() {
        val recurso = binding.etTelefono.text.toString()
        if(recurso.isNotEmpty()){
            val accion = Intent(Intent.ACTION_CALL)
           accion.data = Uri.parse("tel: $recurso")
            if(requireActivity().checkSelfPermission(Manifest.permission.CALL_PHONE) !=
                    PackageManager.PERMISSION_GRANTED){

                requireActivity()
                    .requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 105)

            }


        }else{
            Toast.makeText(requireContext(),getString(R.string.msg_datos),
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun escribirCorreo() {
val recurso = binding.etCorreo.text.toString()
        if(recurso.isNotEmpty()){
            val accion = Intent(Intent.ACTION_SEND)
            accion.type= "message/rfc822"
            accion.putExtra(Intent.EXTRA_EMAIL, arrayOf(recurso))
            accion.putExtra(Intent.EXTRA_SUBJECT,
            getString(R.string.msg_saludos)+" "+binding.etNombre.text)
            accion.putExtra(Intent.EXTRA_TEXT, getString((R.string.msg_mensaje_correo)))
        startActivity(accion)//efectivamente se carga el app de correo
        }else{
            Toast.makeText(requireContext(),getString(R.string.msg_datos),
            Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //consulto si se dio click en el icono de borrar
        if (item.itemId == R.id.delete_menu) {
            deleteLugar()


        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteLugar() {
        val pantalla = AlertDialog.Builder(requireContext())

        pantalla.setTitle(R.string.delete)
        pantalla.setMessage(getString(R.string.seguroBorrar) + "${args.lugar.nombre}?")

        pantalla.setPositiveButton(getString(R.string.si)) { _, _ ->

            lugarViewModel.deleteLugar(args.lugar)
            findNavController().navigate(R.id.action_updateLugarFragment_to_nav_lugar)
        }
        pantalla.setNegativeButton(getString(R.string.no)) { _, _ ->
            pantalla.create().show()

        }
    }

        //////
        private fun updateLugar() {
            val nombre = binding.etNombre.text.toString()
            val correo = binding.etCorreo.text.toString()
            val telefono = binding.etTelefono.text.toString()
            val web = binding.etWeb.text.toString()
            if (nombre.isNotEmpty()) { //Si puedo crear un lugar
                val lugar = Lugar(
                    args.lugar.id, nombre, correo, telefono, web, 0.0,
                    0.0, 0.0, "", ""
                )
                lugarViewModel.saveLugar(lugar)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.msg_lugar_updated),
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigate(R.id.action_updateLugarFragment_to_nav_lugar)
            } else {  //Mensaje de error...
                Toast.makeText(requireContext(), getString(R.string.msg_data), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }
