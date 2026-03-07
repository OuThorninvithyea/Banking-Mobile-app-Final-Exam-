package com.hustle.bankapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hustle.bankapp.theme.BackgroundBlack
import com.hustle.bankapp.theme.BinanceGreen
import com.hustle.bankapp.theme.SurfaceDark
import com.hustle.bankapp.theme.TextPrimary
import com.hustle.bankapp.theme.TextSecondary
import com.hustle.bankapp.theme.RobotoMono

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassmorphism()
            .padding(16.dp),
        content = content
    )
}

@Composable
fun BrandButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BinanceGreen,
            contentColor = BackgroundBlack // High contrast text on green
        ),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = BackgroundBlack,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text.uppercase(),
                fontFamily = RobotoMono,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                fontSize = 15.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlinedInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextSecondary) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedContainerColor = SurfaceDark.copy(alpha = 0.3f),
            unfocusedContainerColor = SurfaceDark.copy(alpha = 0.3f),
            focusedBorderColor = BinanceGreen,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = BinanceGreen,
            focusedLabelColor = BinanceGreen,
            unfocusedLabelColor = TextSecondary
        ),
        singleLine = true,
        trailingIcon = trailingIcon,
        // Using visual transformation for password would go here, 
        // keeping it simple for the layout foundation step.
    )
}
