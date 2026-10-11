package com.passportphotomaker.print.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passportphotomaker.print.ui.components.GlassPrimaryButton

@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1677FF), Color(0xFF0B5CD6), Color(0xFF3B4CFF))
                )
            )
            .systemBarsPadding()
    ) {
        // decorative glows
        Box(
            Modifier
                .size(300.dp)
                .align(Alignment.TopEnd)
                .background(Brush.radialGradient(listOf(Color(0x5538C6FF), Color.Transparent)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF))
                    .border(1.dp, Color(0x55FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Badge,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(Modifier.height(26.dp))

            Text(
                text = "Passport Photo Maker & Print",
                color = Color.White,
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Perfect Size. Clear Photos. Ready for Any Document.",
                color = Color(0xCCFFFFFF),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(34.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FeatureChip(Icons.Filled.Badge, "Passport", Modifier.weight(1f))
                FeatureChip(Icons.Filled.Public, "Visa", Modifier.weight(1f))
                FeatureChip(Icons.Filled.CreditCard, "ID Card", Modifier.weight(1f))
                FeatureChip(Icons.Filled.MoreHoriz, "More...", Modifier.weight(1f))
            }

            Spacer(Modifier.height(44.dp))

            GlassPrimaryButton(
                text = "Get Started",
                icon = Icons.Filled.ArrowForward,
                modifier = Modifier.fillMaxWidth(),
                onClick = onGetStarted
            )
        }
    }
}

@Composable
private fun FeatureChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0x22FFFFFF))
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(18.dp))
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(6.dp))
        Text(label, color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}
