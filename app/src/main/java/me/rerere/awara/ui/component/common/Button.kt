package me.rerere.awara.ui.component.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse


@Composable
fun Button(
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    enabled: Boolean = true,
    type: ButtonType = ButtonType.Default,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    when (type) {
        ButtonType.Default -> {
            androidx.compose.material3.Button(
                modifier = modifier,
                onClick = onClick,
                enabled = enabled,
            ) {
                AnimatedVisibility(
                    visible = loading,
                ) {
                    val textStyle = LocalTextStyle.current
                    CircularProgressIndicator(
                        color = textStyle.color.takeOrElse {
                            LocalContentColor.current
                        },
                        strokeWidth = 2.dp,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(horizontal = 6.dp)
                            .size(
                                textStyle.lineHeight.takeOrElse {
                                    MaterialTheme.typography.labelLarge.lineHeight
                                }.value.dp
                            )
                    )
                }
                content()
            }
        }

        ButtonType.Outlined -> {
            OutlinedButton(
                modifier = modifier,
                onClick = onClick,
                enabled = enabled,
            ) {
                AnimatedVisibility(
                    visible = loading
                ) {
                    val textStyle = LocalTextStyle.current
                    CircularProgressIndicator(
                        color = textStyle.color.takeOrElse {
                            LocalContentColor.current
                        },
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(horizontal = 6.dp)
                            .size(
                                textStyle.lineHeight.takeOrElse {
                                    MaterialTheme.typography.labelLarge.lineHeight
                                }.value.dp
                            )
                    )
                }
                content()
            }
        }

        ButtonType.Text -> {
            TextButton(
                modifier = modifier,
                onClick = onClick,
                enabled = enabled,
            ) {
                AnimatedVisibility(
                    visible = loading
                ) {
                    val textStyle = LocalTextStyle.current
                    CircularProgressIndicator(
                        color = textStyle.color.takeOrElse {
                            LocalContentColor.current
                        },
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(horizontal = 6.dp)
                            .size(
                                textStyle.lineHeight.takeOrElse {
                                    MaterialTheme.typography.labelLarge.lineHeight
                                }.value.dp
                            )
                    )
                }
                content()
            }
        }

        ButtonType.Elevated -> {
            ElevatedButton(
                modifier = modifier,
                onClick = onClick,
                enabled = enabled,
            ) {
                AnimatedVisibility(
                    visible = loading
                ) {
                    val textStyle = LocalTextStyle.current
                    CircularProgressIndicator(
                        color = textStyle.color.takeOrElse {
                            LocalContentColor.current
                        },
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(horizontal = 6.dp)
                            .size(
                                textStyle.lineHeight.takeOrElse {
                                    MaterialTheme.typography.labelLarge.lineHeight
                                }.value.dp
                            )
                    )
                }
                content()
            }
        }

        ButtonType.FilledTonal -> {
            FilledTonalButton(
                modifier = modifier,
                onClick = onClick,
                enabled = enabled,
            ) {
                AnimatedVisibility(
                    visible = loading
                ) {
                    val textStyle = LocalTextStyle.current
                    CircularProgressIndicator(
                        color = textStyle.color.takeOrElse {
                            LocalContentColor.current
                        },
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(horizontal = 6.dp)
                            .size(
                                textStyle.lineHeight.takeOrElse {
                                    MaterialTheme.typography.labelLarge.lineHeight
                                }.value.dp
                            )
                    )
                }
                content()
            }
        }
    }
}

@Preview(
    showBackground = true,
    name = "Button Preview"
)
@Composable
fun ButtonPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ButtonType.values().forEach { btnType ->
            Button(
                type = btnType,
                onClick = {},
                loading = true
            ) {
                Text(text = "Button")
            }
        }
    }
}

enum class ButtonType {
    Default,
    Outlined,
    Text,
    Elevated,
    FilledTonal,
}
