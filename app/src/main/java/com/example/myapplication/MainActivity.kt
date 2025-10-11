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
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.layout.ContentScale
import kotlin.math.round


data class Follower(
    val id: Int,
    val name: String,
    val handle: String,
    val image: Int,
    val isMe: Boolean = false
)

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
    var followers by rememberSaveable { mutableIntStateOf(5) }
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
                    onNavigateToProgress = { currentScreen = "progress" },
                    snackbarHostState = snackbarHostState
                )
            } else {
                ProgressScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCard(
    isFollowed: Boolean,
    followers: Int,
    onFollowClick: () -> Unit,
    onNavigateToProgress: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {

    var rainbowActive by rememberSaveable { mutableStateOf(false) }

    val rainbowColors = listOf(
        Color(243,190,190),
        Color(244,214,198),
        Color(251,243,209),
        Color(208,243,205),
        Color(201,229,255),
    )

    var follewerList by rememberSaveable {
        mutableStateOf(listOf(
            Follower(1, "Біреубева Біреу", "@bireubayev1" ,R.drawable.profile),
            Follower(2, "Кеткенбаев Кеткен", "@ketken" ,R.drawable.profile2),
            Follower(3, "Барғанбаев Барған", "@barganbayev1987" ,R.drawable.profile),
            Follower(4, "Анаубаев Анау", "@anaubaev2" ,R.drawable.profile2),
            Follower(5, "Мынаубаев Мынау", "@mynau" ,R.drawable.profile),
        ))
    }

    val scope = rememberCoroutineScope()

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

    LaunchedEffect(isFollowed) {
        if (isFollowed) {
            if (follewerList.none { it.isMe }) {
                follewerList = listOf(
                    Follower(999, "You", "@yourhandle", R.drawable.profile, isMe = true)
                ) + follewerList
            }
        } else {
            // ДОБАВИЛ: удаляем себя из списка когда отписываемся
            follewerList = follewerList.filter { !it.isMe }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Card(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clickable { onNavigateToProgress() },
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

        // STORIES SECTION
        Text(
            text = "Stories",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp, 16.dp, 0.dp, 8.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(5) { index ->
                Card(
                    modifier = Modifier
                        .width(80.dp)
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = listOf(
                            Color(0xFFFF6B6B),
                            Color(0xFF4ECDC4),
                            Color(0xFFFFE66D),
                            Color(0xFF95E1D3),
                            Color(0xFFA8E6CF)
                        )[index]
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Story ${index + 1}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // FOLLOWERS SECTION
        Text(
            text = "Followers (${follewerList.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp, 16.dp, 0.dp, 8.dp)
        )

        val displayList = if (isFollowed) follewerList else follewerList.filter { !it.isMe }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(12.dp)),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(displayList, key = { it.id }) { follower ->
                var isFollowingThisUser by rememberSaveable(follower.id) { mutableStateOf(false) }
                val currentItem by rememberUpdatedState(follower)
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart && !follower.isMe) {
                            val removedName = follower.name
                            val removedFollower = currentItem
                            follewerList = follewerList.filter { it.id != follower.id }

                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "$removedName removed from feed",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    follewerList = (follewerList + removedFollower).sortedBy { it.id }
                                }
                            }
                            return@rememberSwipeToDismissBoxState true
                        }
                        return@rememberSwipeToDismissBoxState false
                    },
                    positionalThreshold = { it * 0.25f }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    modifier = Modifier.padding(vertical = 4.dp),
                    enableDismissFromEndToStart = !follower.isMe,
                    backgroundContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Gray.copy(alpha = 0.7f))
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.CenterEnd,

                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    content = {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                // Аватар
                                Surface(
                                    modifier = Modifier.size(50.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .2f),
                                ) {
                                    Image(
                                        painter = painterResource(id = follower.image),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                // Имя и хендл
                                Column(
                                    modifier = Modifier
                                        .padding(start = 12.dp)
                                        .weight(1f)
                                ){
                                    Text(
                                        text = follower.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = if (follower.isMe) "You" else follower.handle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (follower.isMe) Color(0xFF2196F3) else Color.Gray,
                                        fontWeight = if (follower.isMe) FontWeight.Bold else FontWeight.Normal
                                    )
                                }

                                // Follow кнопка для каждого фоллоуера (кроме себя)
                                if (!follower.isMe) {
                                    Button(
                                        onClick = {
                                            isFollowingThisUser = !isFollowingThisUser
                                        },
                                        shape = RoundedCornerShape(20.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isFollowingThisUser) Color.Gray else MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier.height(35.dp)
                                    ) {
                                        Text(
                                            text = if (isFollowingThisUser) "Following" else "Follow",
                                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                            color = if (isFollowingThisUser) Color.Black else Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
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