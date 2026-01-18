package com.kamiruku.sonata.features.search

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.DraggableScrollbar
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.rememberDraggableScroller
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.scrollbarState
import com.kamiruku.sonata.Song
import com.kamiruku.sonata.features.search.components.SongListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    textFieldState: TextFieldState,
    onQueryChange: () -> Unit,
    searchResults: List<Song>,
    onClick: (Song) -> Unit,
    selectedItems: Set<String>,
    inSelectionMode: Boolean,
    onToggleSelect: (String) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val listState = rememberLazyListState()
    
    Box(Modifier.fillMaxSize()) {
        SearchBar(
            modifier = Modifier.align(Alignment.TopCenter),
            inputField = {
                SearchBarDefaults.InputField(
                    query = textFieldState.text.toString(),
                    onQueryChange = {
                        textFieldState.edit { replace(0, length, it) }
                        onQueryChange()
                    },
                    onSearch = { },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    placeholder = { Text("Search") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Search, contentDescription = "search")
                    },
                    trailingIcon = {
                        if (textFieldState.text.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    if (!textFieldState.text.isEmpty()) {
                                        textFieldState.edit { replace(0, length, "") }
                                        onQueryChange()
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Outlined.Clear,
                                    contentDescription = "clear"
                                )
                            }
                        }
                    }
                )
            },
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            Box(Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 25.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            "${searchResults.size} results",
                            Modifier.padding(horizontal = 25.dp)
                        )
                    }

                    items(
                        items = searchResults,
                        key = { it.iD }
                    ) { song ->
                        val isSelected = song.path in selectedItems

                        SongListItem(
                            isSelected = isSelected,
                            inSelectionMode = inSelectionMode,
                            song = song,
                            onClick = {
                                if (inSelectionMode) {
                                    onToggleSelect(song.path)
                                } else {
                                    onClick(song)
                                }
                            },
                            onLongClick = {
                                if (!inSelectionMode) {
                                    onToggleSelect(song.path)
                                }
                            }
                        )
                    }

                    item {
                        Spacer(Modifier.padding(100.dp))
                    }
                }

                if (searchResults.size > 25) {
                    val scrollBarState = listState.scrollbarState(searchResults.size)
                    val onDrag = listState.rememberDraggableScroller(searchResults.size)

                    listState.DraggableScrollbar(
                        state = scrollBarState,
                        orientation = Orientation.Vertical,
                        onThumbMoved = { percent -> onDrag(percent) },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            //top padding is less here because lazy column starts with padding already
                            .padding(top = 25.dp, end = 4.dp, bottom = 150.dp)
                    )
                }
            }
        }
    }
}