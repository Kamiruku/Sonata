package com.kamiruku.sonata

import android.content.ContentUris
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import coil.compose.AsyncImage


class HandleFragment : Fragment(R.layout.fragment_handle) {
    private var currentNode: FileNode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentNode = arguments?.getParcelable("node")
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

        currentNode?.let {
            composeView?.setContent {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        currentNode!!.children,
                        key = { it.song?.path ?: "" }
                    ) { node ->
                        ListItem(node)
                    }
                }
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
    fun ListItem(node: FileNode, modifier: Modifier = Modifier) {
        Row(
            modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clickable {
                    handleClick(node)
                },
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val albumId = if (!node.isFolder) {
                node.song?.albumId
            } else {
                findClosestSong(node)?.albumId
            }
            AsyncImage(
                model = getAlbumArt(albumId),
                contentDescription = "Album art",
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            androidx.compose.foundation.layout.Column(
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
                        .focusable()
                        .basicMarquee(animationMode = MarqueeAnimationMode.Immediately),
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    //TODO remove hardcoded color
                    color = Color.White
                )

                Text(
                    text = if (!node.isFolder) {
                        val extension = node.song?.path?.substring(
                            node.song.path?.lastIndexOf('.')?.plus(1) ?: 0
                        )
                        "${extension?.uppercase()} | ${node.song?.duration?.toTime()}"
                    } else {
                        "${node.musicTotal} | ${node.durationTotal.toTime()}"
                    },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    //TODO remove hardcoded color
                    color = Color.White
                )
            }
        }
    }

    private fun getAlbumArt(albumId: Long?): Uri? {
        if (albumId == null || albumId == 0L) return null

        return ContentUris.withAppendedId(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            albumId)
    }

    fun findClosestSong(node: FileNode): Song? {
        if (!node.isFolder && node.song != null) {
            return node.song
        }
        for (child in node.children) {
            val result = findClosestSong(child)
            if (result != null) {
                return result
            }
        }
        return null
    }

    fun handleClick(node: FileNode) {
        if (node.isFolder && node.children.isNotEmpty()) {
            val fragment = HandleFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("node", node)
                }
            }

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
}