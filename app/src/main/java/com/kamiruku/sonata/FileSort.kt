package com.kamiruku.sonata

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


data class FileNode(
    val name: String,
    val song: Song?,
    val isFolder: Boolean = true,
    val children: MutableList<FileNode> = mutableListOf(),
    var musicTotal: Int = 0,
    var durationTotal: Long = 0
)

data class Song (
    var iD: Long = 0,
    var title: String? = null,
    var artist: String? = null,
    var path: String? = null,
    var album: String? = null,
    var duration: Long? = null
)

object FileTreeBuilder {
    fun buildTree(audioList: List<Song>): FileNode {
        val root = FileNode("Music",null)

        for (song in audioList) {
            //placeholder
            val parts = song.path?.
                replace("storage/E58E-9E76/Music", "")?.
                split('/')?.filter { it.isNotEmpty() }
            var currentNode = root

            for ((index, part) in parts!!.withIndex()) {
                val isLast = (index == parts.lastIndex)
                val existingChild = currentNode.children.find { it.name == part }

                if (existingChild != null) {
                    currentNode = existingChild
                } else {
                    val newNode = FileNode(
                        name = part,
                        song = song,
                        isFolder = !isLast
                    )
                    currentNode.children.add(newNode)
                    currentNode = newNode
                }
            }
        }
        sortChildren(root)
        childrenTotalCountDuration(root)
        return root
    }

    private fun sortChildren(nodes: FileNode) {
        nodes.children.sortBy { it.name.lowercase() }
        for (child in nodes.children) {
            sortChildren(child)
        }
    }

    private fun childrenTotalCountDuration(nodes: FileNode) {
        var count = 0
        var duration = 0L

        nodes.children.forEach { child ->
            if (!child.isFolder) {
                count++
                duration += child.song?.duration ?: 0L
            } else {
                childrenTotalCountDuration(child)
                count += child.musicTotal
                duration += child.durationTotal
            }
        }
        nodes.musicTotal = count
        nodes.durationTotal = duration
    }
}

class FileRecyclerAdapter(
    private val nodes: MutableList<FileNode> = mutableListOf(),
    private val expandedNodes: MutableSet<String> = mutableSetOf()
): RecyclerView.Adapter<FileRecyclerAdapter.TreeViewHolder>() {
    private val visibleNodes = mutableListOf<Pair<FileNode, Int>>() // node and depth

    init {
        updateVisibleNodes()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateVisibleNodes() {
        visibleNodes.clear()
        for (node in nodes) {
            addNodeToVisible(node, 0)
        }
        notifyDataSetChanged()
    }

    private fun addNodeToVisible(node: FileNode, depth: Int) {
        visibleNodes.add(node to depth)
        if (node.isFolder && expandedNodes.contains(node.name) && node.children.isNotEmpty()) {
            for (child in node.children) {
                addNodeToVisible(child, depth + 1)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TreeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            android.R.layout.simple_list_item_1,
            parent,
            false
        )
        return TreeViewHolder(view)
    }

    override fun onBindViewHolder(holder: TreeViewHolder, position: Int) {
        val (node, depth) = visibleNodes[position]
        holder.bind(node, depth)
    }

    override fun getItemCount() = visibleNodes.size

    inner class TreeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(node: FileNode, depth: Int) {
            val padding = depth * 40
            textView.setPadding(padding, 8, 8, 8)

            textView.text = if (node.isFolder) {
                ""
            } else {
                val extension = node.song?.path?.substring(node.song.path?.lastIndexOf('.')?.plus(1) ?: 0)
                "${node.song?.title} " +
                        "\n" +
                "[${extension?.uppercase()}] | " +
                        node.song?.duration?.toTime()
            }

            if (node.isFolder && node.children.isNotEmpty()) {
                val arrow = if (expandedNodes.contains(node.song?.path)) "▼" else "▶"
                textView.text =
                    "$arrow 📁 ${node.name}" +
                            "\n" +
                            "${node.musicTotal} | ${node.durationTotal.toTime()}"

                textView.setOnClickListener {
                    if (expandedNodes.contains(node.name)) {
                        expandedNodes.remove(node.name)
                    } else {
                        expandedNodes.add(node.name)
                    }
                    updateVisibleNodes()
                }
            } else {
                textView.setOnClickListener(null)
            }
        }
    }

    private fun Long.toTime(): String {
        //ms -> s
        val seconds = this / 1000
        val minutes = seconds/ 60
        val hours = minutes/ 60

        val mins = minutes - hours * 60
        val secs = seconds - (hours * 3600 + mins * 60)

        return String.format("%02d:%02d:%02d", hours, mins, secs)
    }
}