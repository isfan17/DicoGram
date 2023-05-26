package com.isfan17.dicogram.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.isfan17.dicogram.R
import com.isfan17.dicogram.databinding.FragmentRegisterBinding
import com.isfan17.dicogram.ui.customview.LoadingDialog
import com.isfan17.dicogram.ui.viewmodels.AuthViewModel
import com.isfan17.dicogram.ui.viewmodels.ViewModelFactory
import com.isfan17.dicogram.utils.Constants.Companion.HTTP_400
import com.isfan17.dicogram.utils.Constants.Companion.HTTP_401
import com.isfan17.dicogram.utils.Helper
import com.isfan17.dicogram.utils.Result

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory: ViewModelFactory = ViewModelFactory.getInstance(requireActivity())
        val viewModel: AuthViewModel by viewModels { factory }

        val loadingDialog = LoadingDialog(requireActivity())

        viewModel.registerResult.observe(this.viewLifecycleOwner) { result ->
            if (result != null)
            {
                when (result)
                {
                    is Result.Loading -> {
                        loadingDialog.start()
                    }
                    is Result.Success -> {
                        loadingDialog.stop()
                        Toast.makeText(context, result.data, Toast.LENGTH_SHORT).show()
                        this.findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                    }
                    is Result.Error -> {
                        loadingDialog.stop()
                        when(result.error)
                        {
                            HTTP_400 -> Toast.makeText(context, getString(R.string.auth_error_invalid_email), Toast.LENGTH_LONG).show()
                            HTTP_401 -> Toast.makeText(context, getString(R.string.auth_error_invalid_credentials), Toast.LENGTH_LONG).show()
                            else -> Toast.makeText(context, getString(R.string.unexpected_error), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        binding.btnRegister.setOnClickListener {
            val nameEntry = binding.edRegisterName.text.toString()
            val emailEntry = binding.edRegisterEmail.text.toString()
            val passwordEntry = binding.edRegisterPassword.text.toString()

            when
            {
                nameEntry.isEmpty() -> binding.edRegisterName.error = getString(R.string.ed_validation_blank_error)
                emailEntry.isEmpty() -> binding.edRegisterEmail.error = getString(R.string.ed_validation_blank_error)
                !Helper.isValidEmail(emailEntry) -> binding.edRegisterEmail.error = getString(R.string.ed_validation_email_error)
                passwordEntry.isEmpty() -> binding.edRegisterPassword.error = getString(R.string.ed_validation_blank_error)
                passwordEntry.length < 8 -> binding.edRegisterPassword.error = getString(R.string.ed_validation_password_error)
                else -> viewModel.register(nameEntry, emailEntry, passwordEntry)
            }
        }

        binding.btnLogin.setOnClickListener {
            this.findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}