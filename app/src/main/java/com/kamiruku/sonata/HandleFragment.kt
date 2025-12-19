package com.kamiruku.sonata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment

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

        if (currentNode != null) {
            val composeView = view.findViewById<ComposeView>(R.id.handle_fragment_compose_view)
            composeView?.setContent {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(currentNode!!.children.toMutableList()) {
                        ListItem(it)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val activityComposeView = requireActivity().findViewById<ComposeView>(R.id.compose_view)
        if (requireActivity().supportFragmentManager.backStackEntryCount <= 1) {
            activityComposeView?.visibility = View.VISIBLE
        }
    }

    @Composable
    fun ListItem(node: FileNode, modifier: Modifier = Modifier) {
        Row(
            modifier
                .fillMaxSize()
                .padding(vertical = 12.dp)
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        handleClick(node)
                    }
            ) {
                Text(
                    text = if (!node.isFolder) {
                        node.song?.title ?: "Unknown"
                    } else {
                        "📁 ${node.name}"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .basicMarquee(animationMode = androidx.compose.foundation.MarqueeAnimationMode.Immediately),
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
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
                )
            }
        }
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