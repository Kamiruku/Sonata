import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.DraggableScrollbar
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.rememberDraggableScroller
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.scrollbarState
import com.kamiruku.sonata.rememberDirectionalLazyListState
import com.kamiruku.sonata.state.ScrollDirection

@Composable
fun SwipeBackContainer(
    onBack: () -> Unit,
    listState: LazyListState,
    size: Int,
    onScrollDirectionChanged: (Boolean) -> Unit,
    foreground: @Composable () -> Unit,
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
        Modifier
            .fillMaxSize()
            .then(swipeToClose)
    ) {
        val directionState = rememberDirectionalLazyListState(listState)

        val atTop by remember {
            derivedStateOf {
                listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
            }
        }

        LaunchedEffect(directionState.scrollDirection, atTop) {
            val shouldShow = when {
                atTop -> true
                directionState.scrollDirection == ScrollDirection.Up -> true
                directionState.scrollDirection == ScrollDirection.Down -> false
                else -> return@LaunchedEffect
            }
            onScrollDirectionChanged(shouldShow)
        }

        foreground()

        val scrollBarState = listState.scrollbarState(size)
        val onDrag = listState.rememberDraggableScroller(size)

        if (size > 25) {
            listState.DraggableScrollbar(
                state = scrollBarState,
                orientation = Orientation.Vertical,
                onThumbMoved = { percent -> onDrag(percent) },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(top = 100.dp, end = 4.dp, bottom = 150.dp)
            )
        }
    }
}
