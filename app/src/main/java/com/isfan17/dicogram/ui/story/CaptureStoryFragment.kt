package com.isfan17.dicogram.ui.story

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.isfan17.dicogram.R
import com.isfan17.dicogram.databinding.FragmentCaptureStoryBinding
import com.isfan17.dicogram.utils.Helper
import com.isfan17.dicogram.utils.Helper.uriToFile

class CaptureStoryFragment : Fragment() {

    private var _binding: FragmentCaptureStoryBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaptureStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { activity?.finish() }
        binding.btnSwitch.setOnClickListener {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA
            else CameraSelector.DEFAULT_BACK_CAMERA
            startCamera()
        }
        binding.btnGallery.setOnClickListener { startGallery() }
        binding.btnShutter.setOnClickListener { takePhoto() }

        startCamera()
    }

    private fun takePhoto() {
        binding.progressBar.visibility = View.VISIBLE

        val imageCapture = imageCapture ?: return
        val photoFile = Helper.createFile(requireActivity().application)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireActivity()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(requireActivity(), getString(R.string.photo_error_choose_picture), Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val isBackCamera: Int = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) 1 else -1
                    val action = CaptureStoryFragmentDirections.actionCaptureStoryFragmentToCreateStoryFragment(
                        imageFile = photoFile,
                        isBackCamera =  isBackCamera,
                        imageUri = null
                    )
                    this@CaptureStoryFragment.findNavController().navigate(action)
                    binding.progressBar.visibility = View.GONE
                }
            }
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
            imageCapture = ImageCapture.Builder().build()
            val viewPort =  binding.viewFinder.viewPort
            val useCaseGroup = viewPort?.let {
                UseCaseGroup.Builder()
                    .addUseCase(preview)
                    .addUseCase(imageCapture!!)
                    .setViewPort(it)
                    .build()
            }

            try {
                cameraProvider.unbindAll()
                if (useCaseGroup != null) {
                    cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        useCaseGroup
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(requireActivity(), getString(R.string.photo_error_show_camera), Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireActivity()))
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, getString(R.string.fixed_text_choose_picture))
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, requireActivity())
            val action = CaptureStoryFragmentDirections.actionCaptureStoryFragmentToCreateStoryFragment(
                imageFile = myFile,
                imageUri = selectedImg
            )
            this@CaptureStoryFragment.findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}