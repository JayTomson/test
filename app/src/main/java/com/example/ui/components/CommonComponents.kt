package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.CoroutineScope
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Velocity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppSettings
import com.example.ui.theme.LocalReadTrackerColors
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.RadiusLarge
import com.example.ui.theme.RadiusMedium
import com.example.ui.theme.RadiusSmall
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    val colors = LocalReadTrackerColors.current
    Text(
        text = text.uppercase(),
        style = TextStyle(
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.6.sp,
            color = colors.textSecondary
        ),
        modifier = modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun CardGroup(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = LocalReadTrackerColors.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(RadiusMedium),
        color = colors.cardBg
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun CardGroupDivider() {
    val colors = LocalReadTrackerColors.current
    HorizontalDivider(
        modifier = Modifier.padding(start = 16.dp),
        thickness = 1.dp,
        color = colors.dividerColor
    )
}

@Composable
fun TagBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(5.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = color
            )
        )
    }
}

@Composable
fun RatingBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalReadTrackerColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colors.accent.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp,
                color = colors.accent
            )
        )
    }
}

@Composable
fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalReadTrackerColors.current
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            uncheckedThumbColor = Color.White,
            checkedTrackColor = colors.accent,
            uncheckedTrackColor = if (colors.screenBg == Color.Black || colors.cardBg == Color(0xFF1C1C1E) || colors.cardBg == Color(0xFF141414)) Color.White.copy(alpha = 0.24f) else Color.Black.copy(alpha = 0.26f)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerBottomSheet(
    title: String,
    initialColor: Int,
    customColors: List<Int>,
    onColorSelected: (Int) -> Unit,
    onAddCustomColor: (Int) -> Unit,
    onDeleteCustomColor: (Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    val colors = LocalReadTrackerColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var hexInput by remember { mutableStateOf("") }
    var hexError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = colors.cardBg,
        shape = RoundedCornerShape(topStart = RadiusLarge, topEnd = RadiusLarge)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .imePadding()
        ) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(colors.textSecondary.copy(alpha = 0.3f))
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = TextStyle(
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = colors.textFg
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            SectionLabel("СТАНДАРТНЫЕ")
            val standardColors = AppSettings.defaultStandardColors
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 44.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.height(180.dp)
            ) {
                items(standardColors) { argb ->
                    val isSelected = (argb == initialColor)
                    ColorSwatch(
                        argb = argb,
                        isSelected = isSelected,
                        onClick = {
                            onColorSelected(argb)
                            onDismissRequest()
                        }
                    )
                }
            }

            if (customColors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                SectionLabel("СВОИ · долгое нажатие — удалить")
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 44.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.height(100.dp)
                ) {
                    items(customColors) { argb ->
                        val isSelected = (argb == initialColor)
                        ColorSwatch(
                            argb = argb,
                            isSelected = isSelected,
                            onClick = {
                                onColorSelected(argb)
                                onDismissRequest()
                            },
                            onLongClick = {
                                onDeleteCustomColor(argb)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { input ->
                        val cleaned = input.removePrefix("#").take(8).uppercase()
                        hexInput = cleaned
                        hexError = null
                    },
                    prefix = { Text("#", style = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold, color = colors.textFg)) },
                    placeholder = { Text("FF9F0A", color = colors.textSecondary) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(RadiusMedium),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.dividerColor,
                        focusedContainerColor = colors.cardBg,
                        unfocusedContainerColor = colors.cardBg,
                        focusedTextColor = colors.textFg,
                        unfocusedTextColor = colors.textFg
                    )
                )

                Button(
                    onClick = {
                        val fullHex = if (hexInput.length == 6) "FF$hexInput" else hexInput
                        try {
                            val parsedArgb = fullHex.toLong(16).toInt()
                            onAddCustomColor(parsedArgb)
                            onColorSelected(parsedArgb)
                            onDismissRequest()
                        } catch (_: Exception) {
                            hexError = "Неверный HEX — например FF9F0A"
                        }
                    },
                    shape = RoundedCornerShape(RadiusMedium),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    modifier = Modifier.height(52.dp)
                ) {
                    Text(
                        text = "Добавить",
                        style = TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            color = colors.accentOnColor
                        )
                    )
                }
            }

            hexError?.let { err ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = err, color = colors.cDropped, fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColorSwatch(
    argb: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val swatchColor = Color(argb)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(RadiusMedium))
            .background(swatchColor)
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(RadiusMedium)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun rememberBouncyOverscrollState(
    listState: LazyListState = rememberLazyListState()
): BouncyOverscrollState {
    val coroutineScope = rememberCoroutineScope()
    val animOffset = remember { Animatable(0f) }

    val connection = remember(listState) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val current = animOffset.value
                if (current != 0f) {
                    val delta = available.y
                    if ((current > 0 && delta < 0) || (current < 0 && delta > 0)) {
                        val newOffset = if (current > 0) {
                            (current + delta).coerceAtLeast(0f)
                        } else {
                            (current + delta).coerceAtMost(0f)
                        }
                        val consumedY = newOffset - current
                        coroutineScope.launch { animOffset.snapTo(newOffset) }
                        return Offset(0f, consumedY)
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y != 0f && source == NestedScrollSource.UserInput) {
                    val current = animOffset.value
                    val delta = available.y * 0.35f
                    val newOffset = (current + delta).coerceIn(-140f, 140f)
                    coroutineScope.launch { animOffset.snapTo(newOffset) }
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (animOffset.value != 0f) {
                    coroutineScope.launch {
                        animOffset.animateTo(
                            0f,
                            spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                    }
                }
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val current = animOffset.value
                if (current != 0f || available.y != 0f) {
                    coroutineScope.launch {
                        if (available.y != 0f && current == 0f) {
                            val bounceTarget = (available.y * 0.04f).coerceIn(-80f, 80f)
                            animOffset.animateTo(
                                bounceTarget,
                                spring(stiffness = Spring.StiffnessHigh)
                            )
                        }
                        animOffset.animateTo(
                            0f,
                            spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                    }
                }
                return Velocity.Zero
            }
        }
    }

    return remember(listState, connection) {
        BouncyOverscrollState(listState, animOffset, connection)
    }
}

class BouncyOverscrollState(
    val listState: LazyListState,
    val animOffset: Animatable<Float, AnimationVector1D>,
    val connection: NestedScrollConnection
) {
    val modifier: Modifier
        @Composable get() {
            return Modifier
                .offset { IntOffset(0, animOffset.value.roundToInt()) }
                .nestedScroll(connection)
        }
}
