import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue

class DirectionalLazyListState(
    private val lazyListState: LazyListState
) {
    private var lastIndex = lazyListState.firstVisibleItemIndex
    private var lastOffset = lazyListState.firstVisibleItemScrollOffset

    val scrollDirection by derivedStateOf {
        if (!lazyListState.isScrollInProgress) {
            ScrollDirection.None
        } else {
            val index = lazyListState.firstVisibleItemIndex
            val offset = lazyListState.firstVisibleItemScrollOffset

            val direction = when {
                index == lastIndex && offset > lastOffset -> ScrollDirection.Down
                index == lastIndex && offset < lastOffset -> ScrollDirection.Up
                index > lastIndex -> ScrollDirection.Down
                else -> ScrollDirection.Up
            }
            lastOffset = offset
            lastIndex = index
            direction
        }
    }
}

enum class ScrollDirection {
    Up, Down, None
}