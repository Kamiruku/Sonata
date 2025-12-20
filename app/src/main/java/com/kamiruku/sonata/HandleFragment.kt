package com.kamiruku.sonata

import android.content.ContentUris
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import coil.compose.AsyncImage
import coil.request.ImageRequest


class HandleFragment : Fragment(R.layout.fragment_handle) {
    private var currentNode: FileNode? = null
    private val viewModel: SharedViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentSortId = arguments?.getInt("nodeSortId") ?: 0
        currentNode = viewModel.findNode(currentSortId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_handle, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activityComposeView = requireActivity().findViewById<ComposeView>(R.id.compose_view)
        activityComposeView?.visibility = View.GONE

        val composeView = view.findViewById<ComposeView>(R.id.handle_fragment_compose_view)

        currentNode?.let { node ->
            composeView?.setContent {
                FolderScreen(node)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val activityComposeView = requireActivity().findViewById<ComposeView>(R.id.compose_view)
        if (requireActivity().supportFragmentManager.backStackEntryCount == 0) {
            activityComposeView?.visibility = View.VISIBLE
        }
    }

    @Composable
    fun FolderScreen(node: FileNode) {
        val listState = rememberLazyListState()
        val isScrolling by remember {
            derivedStateOf { listState.isScrollInProgress }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 50.dp,
                bottom = 50.dp,
                start = 25.dp,
                end = 25.dp
            )
        ) {
            item(key = node.sortId) {
                ListFolder(node)
            }
            items(
                node.children.values.toList(),
                key = { it.sortId }
            ) { child ->
                ListItem(
                    node = child,
                    isScrolling = isScrolling
                )
            }
        }
    }

    @Composable
    fun ListFolder(node: FileNode) {
        val context = LocalContext.current
        val imageRequest = remember(node.albumId, context) {
            ImageRequest.Builder(context)
                .data(getAlbumArt(albumId = node.albumId))
                .size(1200)
                .crossfade(true)
                .build()
        }

        Box(
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
                .background(Color.Black, RoundedCornerShape(8.dp))
        ) {
            if (node.albumId != 0L) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = "Top folder album art",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            RoundedCornerShape(4.dp)
                        )
                ) {
                    Text(
                        text = node.name,
                        color = Color.White,
                        fontSize = 22.sp,
                        modifier = Modifier
                            .padding(6.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            RoundedCornerShape(4.dp)
                        )
                ) {
                    Text(
                        text = "\uD834\uDD1E ${node.musicTotal} | ${node.durationTotal.toTime()}",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(6.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun ListItem(node: FileNode, modifier: Modifier = Modifier, isScrolling: Boolean) {
        val shouldMarquee = remember(node.name) { node.name.length > 25 }
        /*
        Row for individual file/folder
            Async on far left
            Column splits right into top and bottom
                top - name
                bottom - extension, length, etc
        */
        Row(
            modifier
                .fillMaxWidth()
                .combinedClickable(
                    onLongClick = { handleLongClick() },
                    onClick = { handleClick(node) }
                )
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val context = LocalContext.current
            val imageRequest = remember(node.albumId, context) {
                ImageRequest.Builder(context)
                    .data(getAlbumArt(albumId = node.albumId))
                    .size(128)
                    .crossfade(true)
                    .build()
            }

            AsyncImage(
                model = imageRequest,
                contentDescription = "Album art",
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (!node.isFolder) {
                        node.song?.title ?: "Unknown"
                    } else {
                        node.name
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (shouldMarquee && !isScrolling)
                                Modifier.basicMarquee()
                            else Modifier
                        ),
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    //TODO remove hardcoded color
                    color = Color.White
                )
                val subText = remember(node.sortId) {
                    if (!node.isFolder) {
                        val ext = node.song?.path?.substring(
                            node.song?.path?.lastIndexOf('.')?.plus(1) ?: 0
                        )
                        "${ext?.uppercase()} | ${node.song?.duration?.toTime()}"
                    } else {
                        "\uD834\uDD1E ${node.musicTotal} | ${node.durationTotal.toTime()}"
                    }
                }
                Text(
                    text = subText,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    //TODO remove hardcoded color
                    color = Color.White
                )
            }
        }
    }

    private fun getAlbumArt(albumId: Long): Uri =
        ContentUris.withAppendedId(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            albumId)

    fun handleClick(node: FileNode) {
        if (node.isFolder && node.children.isNotEmpty()) {
            val fragment = HandleFragment().apply {
                arguments = Bundle().apply {
                    putInt("nodeSortId", node.sortId)
                }
            }

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    fun handleLongClick() {
        println("LONG CLICK")
    }
}