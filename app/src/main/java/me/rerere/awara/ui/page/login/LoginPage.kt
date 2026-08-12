package me.rerere.awara.ui.page.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest
import me.rerere.awara.R
import me.rerere.awara.data.source.stringResourceOfError
import me.rerere.awara.ui.LocalMessageProvider
import me.rerere.awara.ui.LocalRouterProvider
import me.rerere.awara.ui.component.common.BackButton
import me.rerere.awara.ui.component.common.Button
import me.rerere.awara.ui.component.common.ButtonType
import me.rerere.awara.util.openUrl
import me.rerere.compose_setting.preference.mmkvPreference
import me.rerere.compose_setting.preference.rememberStringPreference
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginPage(
    vm: LoginVM = koinViewModel()
) {
    val context = LocalContext.current
    val messageProvider = LocalMessageProvider.current
    val router = LocalRouterProvider.current
    var username by rememberStringPreference(key = "login.username", default = "")
    var password by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        // Remove credentials persisted by older versions of the app.
        mmkvPreference.remove("login.password")
        vm.events.collectLatest {
            when (it) {
                is LoginEvent.LoginSuccess -> {
                    password = ""
                    messageProvider.success {
                        Text("登录成功")
                    }
                    mmkvPreference.putString("refresh_token", it.token)
                    router.navigate("index") {
                        popUpTo("login") {
                            inclusive = true
                        }
                    }
                }

                is LoginEvent.LoginFailed -> {
                    messageProvider.error {
                        Text(context.stringResourceOfError(it.error))
                    }
                }

                is LoginEvent.Exception -> {
                    messageProvider.error {
                        Text(it.message)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.login_title),
                    )
                },
                navigationIcon = { BackButton() }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .width(IntrinsicSize.Min),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = R.mipmap.ic_launcher_round,
                    contentDescription = "logo",
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                    },
                    label = {
                        Text(text = stringResource(R.string.login_user))
                    }
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                    },
                    label = {
                        Text(text = stringResource(R.string.login_password))
                    },
                    visualTransformation = PasswordVisualTransformation(),
                )

                Button(
                    onClick = {
                        vm.login(username, password)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    type = ButtonType.Default,
                    loading = vm.state.loading,
                    enabled = !vm.state.loading && username.isNotBlank() && password.isNotBlank()
                ) {
                    Text(text = stringResource(R.string.login_button))
                }

                Row {
                    var eulaVisible by remember { mutableStateOf(false) }
                    if(eulaVisible) {
                        ModalBottomSheet(
                            onDismissRequest = {
                                eulaVisible = false
                            }
                        ) {
                            UserEula()
                        }
                    }
                    TextButton(
                        onClick = {
                            eulaVisible = true
                        }
                    ) {
                        Text("用户协议")
                    }

                    TextButton(
                        onClick = {
                            context.openUrl("https://iwara.tv/register")
                        }
                    ) {
                        Text("注册")
                    }
                }
            }
        }
    }
}

@Composable
private fun UserEula() {
    LazyColumn(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(500.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            Text(
                text = "使用本软件，代表你已经同意以下协议:",
                style = MaterialTheme.typography.titleLarge
            )
        }

        item {
            Text("1. 本软件仅供学习交流使用，不得用于商业用途，不得二次开发植入收费内容，不得进行任何盈利行为，否则后果自负")
        }

        item {
            Text("2. 请勿在任何公开社交平台上发布本软件的相关内容")
        }

        item {
            Text("3. 使用本软件请遵守当地法律法规, 请勿使用本软件进行任何违法行为，本软件可能会根据你的设备语言进行内容限制")
        }

        item {
            Text("4. 本软件不会收集任何用户数据，仅会与iwara.tv进行必要信息交互")
        }

        item {
            Text("5. 本软件为Iwara客户端第三方客户端，与Iwara官方无关，且不提供任何在线服务，所有信息来自Iwara官方接口")
        }
    }
}
