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
import com.isfan17.dicogram.databinding.FragmentLoginBinding
import com.isfan17.dicogram.ui.customview.LoadingDialog
import com.isfan17.dicogram.ui.viewmodels.AuthViewModel
import com.isfan17.dicogram.ui.viewmodels.ViewModelFactory
import com.isfan17.dicogram.utils.Constants.Companion.HTTP_400
import com.isfan17.dicogram.utils.Constants.Companion.HTTP_401
import com.isfan17.dicogram.utils.Constants.Companion.USER_PREF_DEFAULT_VALUE
import com.isfan17.dicogram.utils.Constants.Companion.USER_PREF_TOKEN_NAME
import com.isfan17.dicogram.utils.Helper
import com.isfan17.dicogram.utils.Result

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory: ViewModelFactory = ViewModelFactory.getInstance(requireActivity())
        val viewModel: AuthViewModel by viewModels { factory }

        val loadingDialog = LoadingDialog(requireActivity())

        viewModel.getUserPreferences(USER_PREF_TOKEN_NAME).observe(this.viewLifecycleOwner) { token ->
            if (token != USER_PREF_DEFAULT_VALUE) (activity as AuthActivity).moveToMain()
        }

        viewModel.loginResult.observe(this.viewLifecycleOwner) { result ->
            if (result != null)
            {
                when (result)
                {
                    is Result.Loading -> {
                        loadingDialog.start()
                    }
                    is Result.Success -> {
                        loadingDialog.stop()
                        viewModel.emailEntry.observe(this.viewLifecycleOwner) { email ->
                            viewModel.saveLoginSession(
                                token = result.data.token,
                                name = result.data.name,
                                email = email,
                            )
                        }
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

        binding.btnLogin.setOnClickListener {
            val emailEntry = binding.edLoginEmail.text.toString()
            val passwordEntry = binding.edLoginPassword.text.toString()

            when
            {
                emailEntry.isEmpty() -> binding.edLoginEmail.error = getString(R.string.ed_validation_blank_error)
                !Helper.isValidEmail(emailEntry) -> binding.edLoginEmail.error = getString(R.string.ed_validation_email_error)
                passwordEntry.isEmpty() -> binding.edLoginPassword.error = getString(R.string.ed_validation_blank_error)
                passwordEntry.length < 8 -> binding.edLoginPassword.error = getString(R.string.ed_validation_password_error)
                else -> viewModel.login(emailEntry, passwordEntry)
            }
        }

        binding.btnRegister.setOnClickListener {
            this.findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}