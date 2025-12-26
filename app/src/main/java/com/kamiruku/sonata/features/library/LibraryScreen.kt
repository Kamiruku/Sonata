package com.kamiruku.sonata.features.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LibraryScreen(
    onAllSongsClick: () -> Unit,
    onFolderClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(25.dp)
    ) {
        item {
            Text(
                text = "Library",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 30.dp),
                fontSize = 22.sp
            )
        }

        item {
            Text(
                text = "All Songs",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAllSongsClick() }
                    .padding(vertical = 22.dp),
                fontSize = 18.sp)
        }

        item {
            Text(
                text = "Folders Hierarchy",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFolderClick() }
                    .padding(vertical = 22.dp),
                fontSize = 18.sp)
        }
    }
}