package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameConstants

@Composable
fun GameHud(
    score: Int,
    level: Int,
    lives: Int,
    showGreatShot: Boolean,
    showDuckEscaped: Boolean,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // Top HUD Bar: Score (Top-Left), Pause Button (Center), Lives (Top-Right)
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- TOP LEFT: Score & Level Badges ---
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Score Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xCC000000),
                    shadowElevation = 4.dp,
                    modifier = Modifier.testTag("score_badge")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Score: ",
                            color = Color(0xFFFFF176),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$score",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Level Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xAA1E88E5),
                    modifier = Modifier.testTag("level_badge")
                ) {
                    Text(
                        text = "Level $level",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            // --- TOP CENTER: Small Pause Button ---
            IconButton(
                onClick = onPauseClick,
                modifier = Modifier
                    .size(48.dp)
                    .shadow(4.dp, CircleShape)
                    .background(Color(0xCCFFFFFF), CircleShape)
                    .border(1.5.dp, Color(0xFF1976D2), CircleShape)
                    .testTag("pause_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause Game",
                    tint = Color(0xFF1565C0),
                    modifier = Modifier.size(26.dp)
                )
            }

            // --- TOP RIGHT: Lives (Hearts) ---
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xCC000000),
                shadowElevation = 4.dp,
                modifier = Modifier.testTag("lives_indicator")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    for (i in 1..GameConstants.INITIAL_LIVES) {
                        val hasLife = (i <= lives)
                        Icon(
                            imageVector = if (hasLife) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (hasLife) "Active Life" else "Lost Life",
                            tint = if (hasLife) Color(0xFFFF1744) else Color(0x88FFFFFF),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // --- Center Notifications & Banners ---
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // "Great Shot!" milestone banner
            AnimatedVisibility(
                visible = showGreatShot,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .shadow(12.dp, RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFFFF8F00), Color(0xFFFFD54F), Color(0xFFFF8F00))
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .border(2.5.dp, Color.White, RoundedCornerShape(24.dp))
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                        .testTag("great_shot_banner")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🎯 GREAT SHOT! 🎯",
                            color = Color(0xFF3E2723),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // "Duck Escaped!" life lost warning banner
            AnimatedVisibility(
                visible = showDuckEscaped,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .shadow(10.dp, RoundedCornerShape(20.dp))
                        .background(Color(0xDDE53935), RoundedCornerShape(20.dp))
                        .border(2.dp, Color(0xFFFFCDD2), RoundedCornerShape(20.dp))
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                        .testTag("duck_escaped_banner")
                ) {
                    Text(
                        text = "💨 Duck Escaped! -1 ❤️",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
