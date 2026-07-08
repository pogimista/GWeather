package com.mista.weather.base

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.zhuinden.simplestackextensions.fragments.DefaultFragmentKey
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.zhuinden.simplestack.History
import com.zhuinden.simplestack.StateChange
import kotlinx.coroutines.launch

abstract class BaseFragment<VM : BaseViewModel> : Fragment() {

    protected abstract val viewModel: VM

    private var loadingDialog: Dialog? = null

    protected open fun provideFragmentKey(): DefaultFragmentKey? =
        (activity as? BaseActivity)?.backstack
            ?.getHistory<BaseKey>()
            ?.firstOrNull { it.fragmentTag == tag }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fragmentKey = provideFragmentKey()
        collectToastMessages()
        collectLoadingDialog()
        collectNavigationEvents()
    }

    override fun onDestroyView() {
        loadingDialog?.dismiss()
        loadingDialog = null
        super.onDestroyView()
    }

    private fun collectToastMessages() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.toastMessage.collect { resourceId ->
                    Toast.makeText(requireContext(), resourceId, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun collectNavigationEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect { event ->
                    handleNavigationEvent(event)
                }
            }
        }
    }

    private fun handleNavigationEvent(event: NavigationEvent) {
        val baseActivity = activity as? BaseActivity ?: return
        val backstack = baseActivity.backstack
        when (event) {
            is NavigationEvent.To -> backstack.goTo(event.key)
            is NavigationEvent.Back -> backstack.goBack()
            is NavigationEvent.BackTo -> {
                val history = backstack.getHistory<BaseKey>()
                val index = history.indexOfFirst { it == event.key }
                if (index >= 0) {
                    val newHistory = if (event.inclusive) history.take(index) else history.take(index + 1)
                    if (newHistory.isNotEmpty()) {
                        backstack.setHistory(History.of(*newHistory.toTypedArray()), StateChange.BACKWARD)
                    }
                }
            }
            is NavigationEvent.BackToRoot -> {
                val root = backstack.getHistory<BaseKey>().firstOrNull() ?: return
                backstack.setHistory(History.of(root), StateChange.BACKWARD)
            }
            is NavigationEvent.ReplaceAll -> {
                backstack.setHistory(History.of(event.key), StateChange.FORWARD)
            }
        }
    }

    private fun collectLoadingDialog() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadingDialog.collect { state ->
                    if (state.isVisible) showLoadingDialog(state.message, state.cancellable)
                    else dismissLoadingDialog()
                }
            }
        }
    }

    private fun showLoadingDialog(message: String, cancellable: Boolean) {
        if (loadingDialog?.isShowing == true) return
        val context = requireContext()

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(64, 48, 64, 48)
        }
        container.addView(ProgressBar(context))
        if (message.isNotBlank()) {
            container.addView(TextView(context).apply {
                text = message
                setPadding(40, 0, 0, 0)
            })
        }

        loadingDialog = Dialog(context).apply {
            setContentView(container)
            setCancelable(cancellable)
            setCanceledOnTouchOutside(cancellable)
            setOnCancelListener { viewModel.hideLoading() }
            show()
        }
    }

    private fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }
}
