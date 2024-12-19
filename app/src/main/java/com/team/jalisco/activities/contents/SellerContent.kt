package com.team.jalisco.activities.contents

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
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
    var isTrue by remember { mutableStateOf(false) }

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
        val user = client.auth.currentSessionOrNull()?.user ?: return@Scaffold
        val userId = user.id
        Log.d("userId", userId)
        CoroutineScope(Dispatchers.IO).launch {
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
        if (isTrue) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Phone is confirmed and added", fontSize = 20.sp)
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