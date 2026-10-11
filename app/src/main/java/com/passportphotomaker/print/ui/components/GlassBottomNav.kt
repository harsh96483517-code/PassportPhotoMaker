package com.passportphotomaker.print.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class NavDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val bottomDestinations = listOf(
    NavDestination("home", "Home", Icons.Filled.Home),
    NavDestination("myphotos", "My Photos", Icons.Filled.PhotoLibrary),
    NavDestination("print", "Print", Icons.Filled.Print),
    NavDestination("settings", "More", Icons.Filled.MoreHoriz)
)

@Composable
fun GlassBottomNav(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(30.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    18.dp,
                    shape,
                    ambientColor = Color(0x331677FF),
                    spotColor = Color(0x331677FF)
                )
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xE6FFFFFF), Color(0xB8FFFFFF))
                    )
                )
                .border(1.dp, Color(0x66FFFFFF), shape)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomDestinations.forEach { dest ->
                val selected = currentRoute == dest.route
                val interaction = remember { MutableInteractionSource() }
                val scale by animateFloatAsState(
                    targetValue = if (selected) 1f else 0.96f,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
                    label = "navScale"
                )
                val tint by animateColorAsState(
                    targetValue = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "navTint"
                )
                val itemShape = RoundedCornerShape(22.dp)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .scale(scale)
                        .clip(itemShape)
                        .then(
                            if (selected) {
                                Modifier
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF2E8BFF), Color(0xFF1677FF))
                                        )
                                    )
                                    .shadow(8.dp, itemShape)
                            } else Modifier
                        )
                        .clickable(interactionSource = interaction, indication = null) {
                            onNavigate(dest.route)
                        }
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = dest.icon,
                        contentDescription = dest.label,
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                    if (selected) {
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = dest.label,
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
