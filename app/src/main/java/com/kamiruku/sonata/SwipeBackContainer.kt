import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun SwipeBackContainer(
    onBack: () -> Unit,
    foreground: @Composable () -> Unit
) {
    //val offsetX = remember { Animatable(0f) }
    var floatOffsetX = remember { 0f }
    val screenWidth = LocalConfiguration.current.screenWidthDp.toFloat()
    val density = LocalDensity.current.density
    val screenWidthPx = screenWidth * density
    val dismissThreshold = screenWidthPx * 0.3
    //val scope = rememberCoroutineScope()
    val edgeWidthDp = 50.dp
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

                    /*
                    scope.launch {
                        offsetX.snapTo((offsetX.value + dragAmount).coerceIn(0f, screenWidthPx))
                    }
                    */
                    floatOffsetX = (floatOffsetX + dragAmount).coerceIn(0f, screenWidthPx)
                    event.changes.forEach { it.consume() }

                    dragging = event.changes.any { it.pressed }
                }

                if (floatOffsetX > dismissThreshold) {
                    /*
                    scope.launch {
                        offsetX.animateTo(screenWidthPx, tween(300))
                    }
                    */
                    onBack()
                } else {
                    /*
                    scope.launch {
                        offsetX.animateTo(0f, tween(300))
                    }
                    */
                }
            }
        }
    }

    Box(
    /*
        Modifier
            .graphicsLayer {
                translationX = offsetX.value
                alpha = 1f - (offsetX.value / screenWidthPx)
            }
            .then(swipeToClose)
    */
        Modifier.fillMaxSize().then(swipeToClose)
    ) {
        foreground()
    }
}
