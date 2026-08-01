package me.rerere.awara.ui.page.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import me.rerere.awara.data.repo.UserRepo
import me.rerere.awara.data.source.APIResult
import me.rerere.awara.data.source.onError
import me.rerere.awara.data.source.onException
import me.rerere.awara.data.source.onSuccess
import me.rerere.awara.data.source.runAPICatching

class LoginVM(private val userRepo: UserRepo) : ViewModel() {
    var state by mutableStateOf(LoginState())
        private set
    val events = MutableSharedFlow<LoginEvent>()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            state = state.copy(
                loading = true
            )
            runAPICatching {
                userRepo.login(username, password)
            }.onSuccess {
                events.emit(
                    LoginEvent.LoginSuccess(
                        token = it.token
                    )
                )
            }.onError {
                events.emit(LoginEvent.LoginFailed(it))
            }.onException {
                events.emit(LoginEvent.Exception(it.exception.message ?: "Unknown error"))
            }
            state = state.copy(
                loading = false
            )
        }
    }
}

data class LoginState(
    val loading: Boolean = false,
)

sealed class LoginEvent {
    class LoginSuccess(
        val token: String
    ) : LoginEvent()

    class LoginFailed(
        val error: APIResult.Error,
    ) : LoginEvent()

    class Exception(
        val message: String,
    ) : LoginEvent()
}
