import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun SwipeBackContainer(
    navController: NavController,
    foreground: @Composable () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val screenWidth = LocalConfiguration.current.screenWidthDp.toFloat()
    val density = LocalDensity.current.density
    val screenWidthPx = screenWidth * density
    val dismissThreshold = screenWidthPx * 0.5
    val scope = rememberCoroutineScope()
    val edgeWidthDp = 75.dp
    val edgeWidthPx = with(LocalDensity.current) { edgeWidthDp.toPx() }

    val swipeToClose = Modifier.pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown()
            //only accept gestures near edge of screen
            if (down.position.x <= edgeWidthPx) {
                var pastDrag = 0f
                var dragging = true
                while (dragging) {
                    val event = awaitPointerEvent()
                    val dragAmount = event.changes.sumOf { it.positionChange().x.toDouble() }.toFloat()
                    pastDrag += dragAmount

                    scope.launch {
                        offsetX.snapTo((offsetX.value + dragAmount).coerceIn(0f, screenWidthPx))
                    }
                    event.changes.forEach { it.consume() }

                    dragging = event.changes.any { it.pressed }
                }

                if (offsetX.value > dismissThreshold) {
                    scope.launch {
                        offsetX.animateTo(screenWidthPx, tween(300))
                        navController.popBackStack()
                    }
                } else {
                    scope.launch {
                        offsetX.animateTo(0f, tween(300))
                    }
                }
            }
        }
    }

    Box(
        Modifier
            .graphicsLayer {
                translationX = offsetX.value
                alpha = 1f - (offsetX.value / screenWidthPx)
            }
            .then(swipeToClose)
    ) {
        foreground()
    }
}
