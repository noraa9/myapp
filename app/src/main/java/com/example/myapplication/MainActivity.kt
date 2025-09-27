package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    color = Color.White
                ) {
                    ProfileCard()
                }
            }
        }
    }
}

@Composable
fun ProfileCard() {
    var isFollowed by rememberSaveable { mutableStateOf(false) }
    var followers by rememberSaveable { mutableIntStateOf(0) }

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

//    Кароче, контур
    val border = if (rainbowActive) {
        BorderStroke(3.dp, rainbowColor)
    } else null

    LaunchedEffect(rainbowActive) {
        if (rainbowActive) {
            kotlinx.coroutines.delay(1500)
            rainbowActive = false
        }
    }

    Box(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.DarkGray)
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
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

            androidx.compose.material3.Button(
                onClick = {
                    if (isFollowed) {
                        isFollowed = false
                        if (followers > 0) followers -= 1
                    } else {
                        isFollowed = true
                        followers += 1
                        rainbowActive = true
                    }
                },
                shape = RoundedCornerShape(20.dp),
                border = border,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
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




@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        ProfileCard()
    }
}
