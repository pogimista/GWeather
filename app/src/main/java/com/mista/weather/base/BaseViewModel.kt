package com.mista.weather.base

import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mista.weather.R
import com.mista.weather.base.error.AppError
import com.mista.weather.session.CacheSession
import com.mista.weather.session.TransientSession
import com.mista.weather.ui.components.LoadingDialogState
import com.squareup.moshi.Moshi
import com.zhuinden.simplestackextensions.fragments.DefaultFragmentKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

abstract class BaseViewModel(
    private val deps: BaseViewModelDeps,
) : AndroidViewModel(deps.application) {

    protected val cacheSession: CacheSession get() = deps.cacheSession
    protected val transientSession: TransientSession get() = deps.transientSession
    protected val moshi: Moshi get() = deps.moshi
    @PublishedApi internal val screenResultBus: ScreenResultBus get() = deps.screenResultBus

    internal var fragmentKey: DefaultFragmentKey? = null

    fun <T : DefaultFragmentKey?> getScreenParcel(): T {
        @Suppress("UNCHECKED_CAST")
        return fragmentKey as T
            ?: throw NullPointerException("The key provided as a fragment argument should not be null!")
    }

    private val _toastMessage = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val toastMessage = _toastMessage.asSharedFlow()

    private val _loadingDialog = MutableStateFlow(LoadingDialogState())
    val loadingDialog = _loadingDialog.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>(extraBufferCapacity = 1)
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun showLoading(message: String = "", cancellable: Boolean = true) {
        _loadingDialog.value = LoadingDialogState(isVisible = true, message = message, cancellable = cancellable)
    }

    fun showLoading(@StringRes messageRes: Int, cancellable: Boolean = true) {
        showLoading(message = getLocalString(messageRes), cancellable = cancellable)
    }

    fun hideLoading() {
        _loadingDialog.value = LoadingDialogState()
    }

    fun showToastMessage(@StringRes resource: Int) {
        viewModelScope.launch { _toastMessage.emit(resource) }
    }

    fun getLocalString(@StringRes resource: Int): String =
        deps.application.getString(resource)

    fun getLocalString(@StringRes resource: Int, vararg args: Any): String =
        deps.application.getString(resource, *args)

    fun navigateTo(key: BaseKey) {
        viewModelScope.launch { _navigationEvent.emit(NavigationEvent.To(key)) }
    }

    fun navigateBack() {
        viewModelScope.launch { _navigationEvent.emit(NavigationEvent.Back) }
    }

    fun navigateBackTo(key: BaseKey, inclusive: Boolean = false) {
        viewModelScope.launch { _navigationEvent.emit(NavigationEvent.BackTo(key, inclusive)) }
    }

    fun navigateBackToRoot() {
        viewModelScope.launch { _navigationEvent.emit(NavigationEvent.BackToRoot) }
    }

    /** Navigates to [key], clearing the whole back stack so [key] becomes the new (only) root. */
    fun navigateAsRoot(key: BaseKey) {
        viewModelScope.launch { _navigationEvent.emit(NavigationEvent.ReplaceAll(key)) }
    }

    fun <T : Any> navigateBackWithResult(data: T) {
        viewModelScope.launch {
            screenResultBus.emit(ScreenResult(data))
            _navigationEvent.emit(NavigationEvent.Back)
        }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> collectResult(crossinline onResult: (T) -> Unit) {
        viewModelScope.launch {
            screenResultBus.results
                .filter { it.data is T }
                .collect { onResult(it.data as T) }
        }
    }

    fun getErrorMessage(error: AppError): String = when (error) {
        is AppError.Network -> getLocalString(R.string.error_network)
        is AppError.Http -> error.toApiErrorBody(moshi)?.displayMessage ?: when (error.code) {
            404 -> getLocalString(R.string.error_not_found)
            401, 403 -> getLocalString(R.string.error_unauthorized)
            in 500..599 -> getLocalString(R.string.error_server)
            else -> getLocalString(R.string.error_http, error.code)
        }
        is AppError.Unexpected -> getLocalString(R.string.error_unexpected)
    }
}
