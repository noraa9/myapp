package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    var currentScreen by rememberSaveable { mutableStateOf("profile") }
    val snackbarHostState = remember { SnackbarHostState() }

    var isFollowed by rememberSaveable { mutableStateOf(false) }
    var followers by rememberSaveable { mutableIntStateOf(0) }
    var showUnfollowDialog by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (showUnfollowDialog) {
        AlertDialog(
            onDismissRequest = { showUnfollowDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    isFollowed = false
                    if (followers > 0) followers -= 1
                    showUnfollowDialog = false
                }) {
                    Text("Unfollow")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnfollowDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Unfollow?") },
            text = { Text("Are you sure you want to unfollow?") }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (currentScreen == "profile") "Profile Screen" else "Course Progress") },
                navigationIcon = {
                    if (currentScreen == "progress") {
                        IconButton(onClick = { currentScreen = "profile" }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (currentScreen == "profile") {
                ProfileCard(
                    isFollowed = isFollowed,
                    followers = followers,
                    onFollowClick = {
                        if (isFollowed) {
                            showUnfollowDialog = true
                        } else {
                            isFollowed = true
                            followers += 1
                            scope.launch {
                                snackbarHostState.showSnackbar("You are now following!")
                            }
                        }
                    },
                    onNavigateToProgress = { currentScreen = "progress" }
                )
            } else {
                ProgressScreen()
            }
        }
    }
}

@Composable
fun ProfileCard(
    isFollowed: Boolean,
    followers: Int,
    onFollowClick: () -> Unit,
    onNavigateToProgress: () -> Unit = {}
) {

    var rainbowActive by rememberSaveable { mutableStateOf(false) }

    val rainbowColors = listOf(
        Color(243,190,190),
        Color(244,214,198),
        Color(251,243,209),
        Color(208,243,205),
        Color(201,229,255),
    )

    val infiniteTransition = rememberInfiniteTransition(label = "rainbow")
    val rainbowColor by infiniteTransition.animateColor(
        initialValue = rainbowColors.first(),
        targetValue = rainbowColors.last(),
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 750
                rainbowColors.forEachIndexed { index, color ->
                    color at (index * 150)
                }
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "rainbowAnimate"
    )

    val buttonColor by animateColorAsState(
        targetValue = if (isFollowed) Color.Gray else MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 200),
        label = "btnColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (isFollowed) Color.Black else Color.White,
        animationSpec = tween(durationMillis = 200)
    )

    val animatedFollowers by animateIntAsState(
        targetValue = followers,
        animationSpec = tween(200)
    )

    val border = if (rainbowActive) {
        BorderStroke(3.dp, rainbowColor)
    } else null

    LaunchedEffect(rainbowActive) {
        if (rainbowActive) {
            kotlinx.coroutines.delay(1500)
            rainbowActive = false
        }
    }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onNavigateToProgress() }, // ДОБАВИЛ: клик для навигации
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            ) {
                Image(
                    painter = painterResource(id = if (isFollowed) R.drawable.profile2 else R.drawable.profile),
                    contentDescription = null,
                    modifier = Modifier.clip(CircleShape)
                )
            }
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = "Aron Nurgaliyev",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "IT student",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$animatedFollowers followers",
                    color = Color.White.copy(alpha = .7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = onFollowClick,
                shape = RoundedCornerShape(20.dp),
                border = border,
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor
                )
            ) {
                Text(
                    text = if (isFollowed) "Followed" else "Follow",
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun ProgressScreen() {
    var courseUnderstanding by rememberSaveable { mutableStateOf(0.7f) }
    var selfStudy by rememberSaveable { mutableStateOf(0.5f) }
    var youtube by rememberSaveable { mutableStateOf(0.3f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Course Learning Progress",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Course Understanding
        Text("Course Understanding: ${(courseUnderstanding * 100).toInt()}%")
        Slider(
            value = courseUnderstanding,
            onValueChange = { courseUnderstanding = it },
            valueRange = 0f..1f
        )
        LinearProgressIndicator(
            progress = courseUnderstanding,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .padding(vertical = 8.dp),
            color = Color(0xFF4CAF50)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Self Study
        Text("Self Study: ${(selfStudy * 100).toInt()}%")
        Slider(
            value = selfStudy,
            onValueChange = { selfStudy = it },
            valueRange = 0f..1f
        )
        LinearProgressIndicator(
            progress = selfStudy,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .padding(vertical = 8.dp),
            color = Color(0xFF2196F3)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // YouTube
        Text("YouTube Tutorials: ${(youtube * 100).toInt()}%")
        Slider(
            value = youtube,
            onValueChange = { youtube = it },
            valueRange = 0f..1f
        )
        LinearProgressIndicator(
            progress = youtube,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .padding(vertical = 8.dp),
            color = Color(0xFFFF5722)
        )

        Spacer(modifier = Modifier.height(24.dp))

        CircularProgressIndicator(
            progress = (courseUnderstanding + selfStudy + youtube) / 3,
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        MainApp()
    }
}