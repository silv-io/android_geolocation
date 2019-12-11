package at.tuwien.android_geolocation.view.ui.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import at.tuwien.android_geolocation.util.EventObserver
import at.tuwien.android_geolocation.util.setupSnackbar
import at.tuwien.android_geolocation.viewmodel.location.EnableSecurityViewModel
import com.google.android.material.snackbar.Snackbar
import com.tuwien.geolocation_android.R
import com.tuwien.geolocation_android.databinding.FragmentEnableSecurityBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class EnableSecurity : Fragment() {

    private val viewModel by viewModel<EnableSecurityViewModel>()

    private lateinit var viewDataBinding: FragmentEnableSecurityBinding

    companion object {
        fun newInstance() = EnableSecurity()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = FragmentEnableSecurityBinding.inflate(inflater, container, false).apply {
            vm = viewModel
        }

        val pwd = viewDataBinding.root.findViewById<EditText>(R.id.txt_password)
        val btn = viewDataBinding.root.findViewById<Button>(R.id.btn_security)

        btn.setOnClickListener { viewModel.secureDatabase(pwd.text) }

        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.setupSnackbar(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
        setupNavigation()
    }

    private fun setupNavigation() {
        viewModel.backEvent.observe(this, EventObserver {
            findNavController().navigate(R.id.action_enableSecurity_pop)
        })
    }

}
