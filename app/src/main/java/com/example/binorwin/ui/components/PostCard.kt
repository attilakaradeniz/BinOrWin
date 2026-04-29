package com.example.binorwin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.binorwin.ui.theme.BinOrWinTheme
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCard() {
    var showBottomSheet by remember { mutableStateOf(false) }
    var currentAction by remember { mutableStateOf("") }
    var argumentText by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState()

    var binVotes by remember { mutableIntStateOf(0) }
    var winVotes by remember { mutableIntStateOf(0) }

    val totalVotes = binVotes + winVotes
    val winRatio = if (totalVotes > 0) winVotes.toFloat() / totalVotes else 0.5f

    // Calculate intensity from 0.0 to 1.0 for the dominant side
    val rustIntensity = if (winRatio < 0.5f) (0.5f - winRatio) * 2 else 0f
    val shineIntensity = if (winRatio > 0.5f) (winRatio - 0.5f) * 2 else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // The Image Box with CUSTOM DRAWING (Canvas)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E0E0))
                    .drawWithContent {
                        // 1. Draw the actual content (the image/text) first
                        drawContent()

                        val w = size.width
                        val h = size.height

                        // 2. Draw RUST EFFECT if binVotes are higher
                        if (rustIntensity > 0f) {
                            // Seeded random so spots don't jitter on every frame
                            val random = Random(42)
                            val spotCount = (30 * rustIntensity).toInt() + 10

                            for (i in 0 until spotCount) {
                                // Randomly pick an edge (0: Top, 1: Right, 2: Bottom, 3: Left)
                                val edge = random.nextInt(4)
                                val cx = when (edge) {
                                    1 -> w // Right edge
                                    3 -> 0f // Left edge
                                    else -> random.nextFloat() * w
                                }
                                val cy = when (edge) {
                                    0 -> 0f // Top edge
                                    2 -> h // Bottom edge
                                    else -> random.nextFloat() * h
                                }

                                // Random radius that scales with intensity
                                val maxRadius = 40f * rustIntensity
                                val radius = random.nextFloat() * maxRadius + 10f

                                // Random rust colors (Dark browns, oranges)
                                val rustColor = listOf(
                                    Color(0xFF5D4037),
                                    Color(0xFF8D6E63),
                                    Color(0xFFA1887F),
                                    Color(0xFFBF360C)
                                ).random(random)

                                // Draw irregular circles that bleed inside and outside
                                drawCircle(
                                    color = rustColor.copy(alpha = 0.7f),
                                    radius = radius,
                                    center = Offset(cx, cy)
                                )
                            }
                        }

                        // 3. Draw SHINE EFFECT if winVotes are higher
                        if (shineIntensity > 0f) {
                            // Draw multiple semi-transparent strokes for a "Glow"
                            val baseThickness = 10f * shineIntensity
                            for (i in 1..3) {
                                drawRect(
                                    color = Color(0xFFFFD54F).copy(alpha = 0.3f / i),
                                    topLeft = Offset.Zero,
                                    size = Size(w, h),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = baseThickness * i * 2)
                                )
                            }

                            // Draw a sharp inner gold border
                            drawRect(
                                color = Color(0xFFFFCA28),
                                topLeft = Offset.Zero,
                                size = Size(w, h),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = baseThickness)
                            )

                            // Draw a "Light Flare" (Star shape) at the top right corner
                            val flareCenter = Offset(w - 20f, 20f)
                            val flareSize = 50f * shineIntensity

                            val path = Path().apply {
                                moveTo(flareCenter.x, flareCenter.y - flareSize) // Top
                                quadraticTo(flareCenter.x, flareCenter.y, flareCenter.x + flareSize, flareCenter.y) // Right
                                quadraticTo(flareCenter.x, flareCenter.y, flareCenter.x, flareCenter.y + flareSize) // Bottom
                                quadraticTo(flareCenter.x, flareCenter.y, flareCenter.x - flareSize, flareCenter.y) // Left
                                close()
                            }
                            drawPath(
                                path = path,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            // Core of the flare
                            drawCircle(color = Color.White, radius = 5f * shineIntensity, center = flareCenter)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Item Image Placeholder", color = Color.DarkGray)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Score: $binVotes Bin - $winVotes Win",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Trash or Treasure?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        currentAction = "Bin"
                        showBottomSheet = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Bin")
                }

                Text(
                    text = "or",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Button(
                    onClick = {
                        currentAction = "Win"
                        showBottomSheet = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                ) {
                    Text("Win")
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                argumentText = ""
            },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val sheetTitle = if (currentAction == "Bin") "Why bin it?" else "How win it?"

                Text(
                    text = sheetTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = argumentText,
                    onValueChange = { argumentText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Write your argument here...") },
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (currentAction == "Bin") {
                            binVotes += 1
                        } else if (currentAction == "Win") {
                            winVotes += 1
                        }
                        showBottomSheet = false
                        argumentText = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Submit Argument")
                }
            }
        }
    }
}