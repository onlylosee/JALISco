package com.team.jalisco.activities.contents

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import com.team.jalisco.R
import com.team.jalisco.domain.CustomMenuIcon
import com.team.jalisco.domain.model.CustomDrawerState
import com.team.jalisco.domain.model.opposite
import com.team.jalisco.domain.util.supabaseCreate
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
@Composable
fun SellerContent(
    modifier: Modifier = Modifier,
    drawerState: CustomDrawerState,
    onDrawerClick: (CustomDrawerState) -> Unit
) {
    var client: SupabaseClient = supabaseCreate()
    var focus = LocalFocusManager.current


    Scaffold(
        modifier = modifier
            .clickable(enabled = drawerState == CustomDrawerState.Opened) {
                onDrawerClick(CustomDrawerState.Closed)
            },
        topBar = {
            IconButton(onClick = {
                focus.clearFocus()
                onDrawerClick(drawerState.opposite())
            }) {
                Icon(
                    painter = CustomMenuIcon("menu"),
                    contentDescription = "Menu Icon"
                )
            }
        }

    ) {
        var isTrue by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(key1 = Unit) {
            val user = client.auth.currentSessionOrNull()?.user
            if (user != null) {
                val userId = user.id
                Log.d("userId", userId)

                try {
                    val response = client.postgrest["profile"]
                        .select(columns = Columns.list("phone")) {
                            filter { eq("user_id", userId) }
                            limit(1)
                        }

                    val phone = response.data[0].toString() ?: ""
                    isTrue = phone.isNotBlank()
                    Log.d("Phone check", "Phone exists: $isTrue")
                } catch (e: Exception) {
                    Log.e("SupabaseError", "Error checking phone: ${e.localizedMessage}")
                }
            }
        }
        if (isTrue) {
            var isSheetVisible by remember { mutableStateOf(false) }

            // Анимация высоты для выдвижного мини-экрана
            val sheetHeight by animateDpAsState(targetValue = if (isSheetVisible) 300.dp else 0.dp)

            Box(modifier = Modifier.fillMaxSize()) {
                // Контент основной страницы
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(text = "Основной контент", modifier = Modifier.weight(1f))

                    // Показываем кнопку только если мини-экран скрыт
                    if (!isSheetVisible) {
                        Button(
                            onClick = { isSheetVisible = true },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(text = "Показать мини-экран")
                        }
                    }
                }

                if (isSheetVisible) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(indication = null) {
                                isSheetVisible = false
                            }
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(sheetHeight)
                        .background(Color.Gray)
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        }
                ) {
                    if (isSheetVisible) {
                        Text(text = "Мини экран с элементами", color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Элемент 1", color = Color.White)
                        Text(text = "Элемент 2", color = Color.White)
                        Text(text = "Элемент 3", color = Color.White)

                        // Кнопка для скрытия мини-экрана
                        IconButton(
                            onClick = { isSheetVisible = false },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "Hide Bottom Sheet",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
            ) {
                Text(
                    text = "Please add and confirm\n your phone in profile",
                    fontFamily = FontFamily(Font(R.font.flameregular)),
                    fontSize = 36.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
