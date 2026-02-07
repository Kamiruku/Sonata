package com.kamiruku.sonata.features.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kamiruku.sonata.FileNode
import com.kamiruku.sonata.features.library.components.FileListItem

@Composable
fun FileRootScreen(
    nodes: List<FileNode>,
    onOpen: (FileNode) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 50.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Folder Hierarchy",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp)
                    .padding(vertical = 30.dp),
                fontSize = 22.sp
            )
        }

        items(nodes) { node ->
            FileListItem(
                node = node,
                onClick = { onOpen(node) },
                onLongClick = {}
            )
        }
    }
}