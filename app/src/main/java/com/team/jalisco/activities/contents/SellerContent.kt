package com.team.jalisco.activities.contents

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.team.jalisco.R
import com.team.jalisco.domain.CustomButton
import com.team.jalisco.domain.CustomMenuIcon
import com.team.jalisco.domain.CustomTextFieldForProduct
import com.team.jalisco.domain.model.CustomDrawerState
import com.team.jalisco.domain.model.opposite
import com.team.jalisco.domain.util.supabaseCreate
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
@Composable
fun SellerContent(
    modifier: Modifier = Modifier,
    drawerState: CustomDrawerState,
    onDrawerClick: (CustomDrawerState) -> Unit
) {
    var context = LocalContext.current
    var client: SupabaseClient = supabaseCreate()
    var focus = LocalFocusManager.current
    var croppedImageUri by remember { mutableStateOf<Uri?>(null) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isConfirmed by remember { mutableStateOf(false) }

    val cropLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val resultUri = result.data?.let { com.yalantis.ucrop.UCrop.getOutput(it) }
                if (resultUri != null) {
                    croppedImageUri = resultUri
                    Log.d("ImageUpdate", "Cropped Image URI: $croppedImageUri")
                } else {
                    Log.e("CropError", "UCrop resultUri is null")
                    Toast.makeText(context, "Failed to retrieve cropped image", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { startImageCrop(it, context, cropLauncher) }
        }


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
            val sheetHeight by animateDpAsState(targetValue = if (isSheetVisible) 550.dp else 0.dp)

            Box(modifier = Modifier.fillMaxSize())
            {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (isSheetVisible) isSheetVisible = false
                        }

                ) {
                    Text(
                        text = "Your products",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 24.sp,
                        fontFamily = FontFamily(Font(R.font.flameregular))
                    )

                    if (!isSheetVisible) {
                        CustomButton(
                            height = 75.dp,
                            text = "Sell your product",
                            onClick = { isSheetVisible = !isSheetVisible }
                        )
                    }
                }

                Surface(
                    elevation = 16.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .height(sheetHeight),
                    shape = RoundedCornerShape(topStart = 64.dp, topEnd = 64.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(sheetHeight)
                            .background(
                                MaterialTheme.colorScheme.background.copy(0.1f),
                                RoundedCornerShape(topStart = 64.dp, topEnd = 64.dp)
                            )
                            .padding(16.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                            },
                    ) {
                        if (isSheetVisible) {
                            Box(
                                modifier = Modifier
                                    .padding(16.dp)
                            ) {
                                val painter: Painter = if (croppedImageUri != null) {
                                    rememberAsyncImagePainter(croppedImageUri)
                                } else {
                                    painterResource(id = R.drawable.sell)
                                }
                                Column {
                                    Row {
                                        Surface(
                                            elevation = 4.dp,
                                            shape = RoundedCornerShape(24.dp)
                                        ) {
                                            Image(
                                                painter = painter,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                alignment = Alignment.Center,
                                                modifier = Modifier
                                                    .height(150.dp)
                                                    .width(150.dp)
                                                    .fillMaxSize()
                                                    .border(
                                                        BorderStroke(
                                                            4.dp,
                                                            MaterialTheme.colorScheme.onPrimary
                                                        ),
                                                        shape = RoundedCornerShape(24.dp)
                                                    )
                                                    .clickable {
                                                        launcher.launch("image/*")
                                                    }
                                                    .clip(RoundedCornerShape(24.dp))
                                            )
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            CustomTextFieldForProduct(
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                textValue = name,
                                                onValueChange = { name = it },
                                                labelText = "Product name",
                                                outlinedTextFieldColors = TextFieldDefaults.colors(
                                                    focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                                                    unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                                                    unfocusedTextColor = Color.DarkGray,
                                                    focusedTextColor = Color.Black,
                                                    focusedIndicatorColor = Color.Gray,
                                                    unfocusedIndicatorColor = Color.Gray,
                                                    unfocusedLabelColor = Color.Black,
                                                    focusedLabelColor = Color.DarkGray,
                                                ),
                                                isValid = null,
                                                keyboardType = KeyboardType.Text
                                            )
                                            Spacer(Modifier.height(16.dp))
                                            CustomTextFieldForProduct(
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                textValue = amount,
                                                onValueChange = { amount = it },
                                                labelText = "Amount",
                                                outlinedTextFieldColors = TextFieldDefaults.colors(
                                                    focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                                                    unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                                                    unfocusedTextColor = Color.DarkGray,
                                                    focusedTextColor = Color.Black,
                                                    focusedIndicatorColor = Color.Gray,
                                                    unfocusedIndicatorColor = Color.Gray,
                                                    unfocusedLabelColor = Color.Black,
                                                    focusedLabelColor = Color.DarkGray,
                                                ),
                                                isValid = null,
                                                keyboardType = KeyboardType.Number
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    CustomTextFieldForProduct(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        textValue = description,
                                        onValueChange = { description = it },
                                        labelText = "Description",
                                        outlinedTextFieldColors = TextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                                            unfocusedTextColor = Color.DarkGray,
                                            focusedTextColor = Color.Black,
                                            focusedIndicatorColor = Color.Gray,
                                            unfocusedIndicatorColor = Color.Gray,
                                            unfocusedLabelColor = Color.Black,
                                            focusedLabelColor = Color.DarkGray,
                                        ),
                                        isValid = null,
                                        keyboardType = KeyboardType.Number
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        var isSelected by remember { mutableStateOf(false) }
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {},
                                            modifier = Modifier
                                                .scale(1.5f),
                                            interactionSource = remember { MutableInteractionSource() },
                                        )
                                        Text(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            text = "Is you're confirmed?",
                                            fontSize = 22.sp,
                                            fontFamily = FontFamily(Font(R.font.flamesans))
                                        )
                                    }
                                    Box(
                                        contentAlignment = Alignment.BottomCenter,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight()
                                    ) {
                                        CustomButton(
                                            text = "Try to create a product",
                                            onClick = {},
                                        )
                                    }
                                }
                            }
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
