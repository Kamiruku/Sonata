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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.DraggableScrollbar
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.rememberDraggableScroller
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.scrollbarState
import com.kamiruku.sonata.SharedViewModel
import com.kamiruku.sonata.Song
import com.kamiruku.sonata.features.search.components.GroupListItem
import com.kamiruku.sonata.features.search.components.SongListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SharedViewModel,
    onClick: (Song) -> Unit,
    selectedItems: Set<String>,
    inSelectionMode: Boolean,
    onToggleSelect: (String) -> Unit,
    onToggleSelectGroup: (List<String>) -> Unit,
    onOpen: (String) -> Unit
) {
    var searchBarExpanded by rememberSaveable { mutableStateOf(false) }
    var dropDownExpanded by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    var type by rememberSaveable { mutableStateOf("all") }
    val searchResults by viewModel.filteredSongs.collectAsState()
    val query by viewModel.query.collectAsState()
    
    Box(Modifier.fillMaxSize()) {
        SearchBar(
            modifier = Modifier.align(Alignment.TopCenter),
            inputField = {
                SearchBarDefaults.InputField(
                    query = query.second,
                    onQueryChange = {
                        viewModel.clearSelected()
                        viewModel.setQuery(type to it)
                    },
                    onSearch = { },
                    expanded = searchBarExpanded,
                    onExpandedChange = { searchBarExpanded = it },
                    placeholder = { Text("Search") },
                    leadingIcon = {
                        IconButton({ dropDownExpanded = !dropDownExpanded }) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        DropdownMenu(
                            expanded = dropDownExpanded,
                            onDismissRequest = { dropDownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All") },
                                onClick = {
                                    type = "all"
                                    viewModel.clearSelected()
                                    viewModel.setQuery(type to query.second)
                                    dropDownExpanded = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Artist") },
                                onClick = {
                                    type = "artist"
                                    viewModel.clearSelected()
                                    viewModel.setQuery(type to query.second)
                                    dropDownExpanded = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Album") },
                                onClick = {
                                    type = "album"
                                    viewModel.clearSelected()
                                    viewModel.setQuery(type to query.second)
                                    dropDownExpanded = false
                                }
                            )
                        }
                    },
                    trailingIcon = {
                        if (query.second.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    if (!query.second.isEmpty()) {
                                        viewModel.clearSelected()
                                        viewModel.setQuery("" to "")
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
            expanded = searchBarExpanded,
            onExpandedChange = { searchBarExpanded = it },
        ) {
            Box(Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 25.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        val resultAmount =  if (searchResults["all"] != null)
                            searchResults["all"]?.size ?: 0
                        else {
                            searchResults.size
                        }
                        Text(
                            "$resultAmount results",
                            Modifier.padding(horizontal = 25.dp)
                        )
                    }

                    if (searchResults["all"] != null) {
                        val allItems = searchResults["all"] ?: return@LazyColumn
                        items(
                            items = allItems,
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
                    } else {
                        items(
                            items = searchResults.entries.toList(),
                            key = { it.key }
                        ) { entry ->
                            val key = entry.key
                            val value = entry.value

                            if (key == "all") return@items

                            val flat = remember(key, value) {
                                value.map { it.path }
                            }

                            val isSelected = selectedItems.containsAll(flat)

                            GroupListItem(
                                isSelected = isSelected,
                                inSelectionMode = inSelectionMode,
                                title = key,
                                list = value,
                                onClick = {
                                    if (inSelectionMode) onToggleSelectGroup(flat)
                                    else onOpen(key)
                                },
                                onLongClick = {
                                    if (!inSelectionMode) onToggleSelectGroup(flat)
                                }
                            )
                        }
                    }

                    item {
                        Spacer(Modifier.padding(100.dp))
                    }
                }

                val totalItems =
                    if (type == "all") searchResults["all"]?.size ?: 0
                    else searchResults.size

                if (totalItems > 25) {
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