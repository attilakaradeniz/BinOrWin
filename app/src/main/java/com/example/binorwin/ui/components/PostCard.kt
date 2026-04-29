package com.example.binorwin.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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

    // State to hold the selected image URI
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // The launcher that opens the Android Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

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
            // The Image Box with CUSTOM DRAWING (Canvas) AND PHOTO PICKER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E0E0))
                    // THIS IS THE CRITICAL PART WE ADDED: Make it clickable!
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    .drawWithContent {
                        // 1. Draw the actual content (the image/text) first
                        drawContent()

                        val w = size.width
                        val h = size.height

                        // 2. Draw RUST EFFECT if binVotes are higher
                        if (rustIntensity > 0f) {
                            val random = Random(42)
                            val spotCount = (30 * rustIntensity).toInt() + 10

                            for (i in 0 until spotCount) {
                                val edge = random.nextInt(4)
                                val cx = when (edge) {
                                    1 -> w
                                    3 -> 0f
                                    else -> random.nextFloat() * w
                                }
                                val cy = when (edge) {
                                    0 -> 0f
                                    2 -> h
                                    else -> random.nextFloat() * h
                                }

                                val maxRadius = 40f * rustIntensity
                                val radius = random.nextFloat() * maxRadius + 10f

                                val rustColor = listOf(
                                    Color(0xFF5D4037),
                                    Color(0xFF8D6E63),
                                    Color(0xFFA1887F),
                                    Color(0xFFBF360C)
                                ).random(random)

                                drawCircle(
                                    color = rustColor.copy(alpha = 0.7f),
                                    radius = radius,
                                    center = Offset(cx, cy)
                                )
                            }
                        }

                        // 3. Draw SHINE EFFECT if winVotes are higher
                        if (shineIntensity > 0f) {
                            val baseThickness = 10f * shineIntensity
                            for (i in 1..3) {
                                drawRect(
                                    color = Color(0xFFFFD54F).copy(alpha = 0.3f / i),
                                    topLeft = Offset.Zero,
                                    size = Size(w, h),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = baseThickness * i * 2)
                                )
                            }

                            drawRect(
                                color = Color(0xFFFFCA28),
                                topLeft = Offset.Zero,
                                size = Size(w, h),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = baseThickness)
                            )

                            val flareCenter = Offset(w - 20f, 20f)
                            val flareSize = 50f * shineIntensity

                            val path = Path().apply {
                                moveTo(flareCenter.x, flareCenter.y - flareSize)
                                quadraticTo(flareCenter.x, flareCenter.y, flareCenter.x + flareSize, flareCenter.y)
                                quadraticTo(flareCenter.x, flareCenter.y, flareCenter.x, flareCenter.y + flareSize)
                                quadraticTo(flareCenter.x, flareCenter.y, flareCenter.x - flareSize, flareCenter.y)
                                close()
                            }
                            drawPath(
                                path = path,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            drawCircle(color = Color.White, radius = 5f * shineIntensity, center = flareCenter)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // THIS IS THE SECOND CRITICAL PART: Show image if selected!
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected Item",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = "Tap to add an image",
                        color = Color.DarkGray
                    )
                }
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