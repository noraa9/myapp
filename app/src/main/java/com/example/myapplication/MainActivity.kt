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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class Follower(
    val id: Int,
    val name: String,
    val handle: String,
    val image: Int,
    val isMe: Boolean = false
)

data class ProfileUiState(
    val name: String = "Aron Nurgaliyev",
    val bio: String = "IT student",
    val followers: Int = 5,
    val isFollowed: Boolean = false,
    val followersList: List<Follower> = listOf(
        Follower(1, "Біреубева Біреу", "@bireubayev1", 0),
        Follower(2, "Кеткенбаев Кеткен", "@ketken", 0),
        Follower(3, "Барғанбаев Барған", "@barganbayev1987", 0),
        Follower(4, "Анаубаев Анау", "@anaubaev2", 0),
        Follower(5, "Мынаубаев Мынау", "@mynau", 0),
    )
)


class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateBio(bio: String) {
        _uiState.update { it.copy(bio = bio) }
    }

    fun follow() {
        _uiState.update {
            it.copy(
                isFollowed = true,
                followers = it.followers + 1,
                followersList = listOf(
                    Follower(999, "You", "@yourhandle", 0, isMe = true)
                ) + it.followersList
            )
        }
    }

    fun unfollow() {
        _uiState.update {
            it.copy(
                isFollowed = false,
                followers = maxOf(0, it.followers - 1),
                followersList = it.followersList.filter { f -> !f.isMe }
            )
        }
    }

    fun removeFollower(follower: Follower) {
        _uiState.update {
            it.copy(followersList = it.followersList.filter { f -> f.id != follower.id })
        }
    }

    fun addFollowerBack(follower: Follower) {
        _uiState.update {
            it.copy(followersList = (it.followersList + follower).sortedBy { f -> f.id })
        }
    }
}


object Routes {
    const val HOME = "home"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                AppNavigation()
            }
        }
    }
}


@Composable
fun AppNavigation(viewModel: ProfileViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(navController)
        }
        composable(Routes.PROFILE) {
            MainApp(navController, viewModel)
        }
        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(navController, viewModel)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to Profile App",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { navController.navigate(Routes.PROFILE) }) {
                Text("Go to Profile")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    navController: NavHostController,
    viewModel: ProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showUnfollowDialog by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (showUnfollowDialog) {
        AlertDialog(
            onDismissRequest = { showUnfollowDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.unfollow()
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
                title = { Text("Profile Screen") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.EDIT_PROFILE) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        ProfileCard(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            onFollowClick = {
                if (uiState.isFollowed) {
                    showUnfollowDialog = true
                } else {
                    viewModel.follow()
                    scope.launch {
                        snackbarHostState.showSnackbar("You are now following!")
                    }
                }
            },
            onRemoveFollower = { follower ->
                val removed = follower
                viewModel.removeFollower(follower)
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "${follower.name} removed",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.addFollowerBack(removed)
                    }
                }
            },
            snackbarHostState = snackbarHostState
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCard(
    modifier: Modifier = Modifier,
    uiState: ProfileUiState,
    onFollowClick: () -> Unit,
    onRemoveFollower: (Follower) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var rainbowActive by rememberSaveable { mutableStateOf(false) }

    val rainbowColors = listOf(
        Color(243,190,190),
        Color(244,214,198),
        Color(251,243,209),
        Color(208,243,205),
        Color(201,229,255),
    )

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
        targetValue = if (uiState.isFollowed) Color.Gray else MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 200),
        label = "btnColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (uiState.isFollowed) Color.Black else Color.White,
        animationSpec = tween(durationMillis = 200)
    )

    val animatedFollowers by animateIntAsState(
        targetValue = uiState.followers,
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Card(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
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
                        painter = painterResource(id = if (uiState.isFollowed) R.drawable.profile2 else R.drawable.profile),
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
                        text = uiState.name,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = uiState.bio,
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
                    onClick = {
                        onFollowClick()
                        rainbowActive = !uiState.isFollowed
                    },
                    shape = RoundedCornerShape(20.dp),
                    border = border,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor
                    )
                ) {
                    Text(
                        text = if (uiState.isFollowed) "Followed" else "Follow",
                        color = textColor
                    )
                }
            }
        }

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

        Text(
            text = "Followers (${uiState.followersList.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp, 16.dp, 0.dp, 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.followersList, key = { it.id }) { follower ->
                var isFollowingThisUser by rememberSaveable(follower.id) { mutableStateOf(false) }
                val currentItem by rememberUpdatedState(follower)
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart && !follower.isMe) {
                            onRemoveFollower(currentItem)
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
                            contentAlignment = Alignment.CenterEnd
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
                                Surface(
                                    modifier = Modifier.size(50.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .2f),
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.profile),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }

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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf(uiState.name) }
    var bio by remember { mutableStateOf(uiState.bio) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Edit your profile information",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.updateName(name)
                    viewModel.updateBio(bio)
                    scope.launch {
                        snackbarHostState.showSnackbar("Profile updated!")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        AppNavigation()
    }
}