package com.kamiruku.sonata

data class FileNode(
    val name: String,
    val path: String,
    val isFolder: Boolean = true,
    val children: MutableList<FileNode> = mutableListOf()
)

object FileTreeBuilder {
    fun buildTree(filePaths: List<String>): FileNode {
        val root = FileNode("", "", true)

        for (path in filePaths) {
            val parts = path.split('/').filter { it.isNotEmpty() }
            var currentNode = root

            for ((index, part) in parts.withIndex()) {
                val isLast = (index == parts.lastIndex)
                val existingChild = currentNode.children.find { it.name == part }

                if (existingChild != null) {
                    currentNode = existingChild
                } else {
                    val newNode = FileNode(
                        name = part,
                        path = currentNode.path + '/' + part,
                        isFolder = !isLast
                    )
                    currentNode.children.add(newNode)
                    currentNode = newNode
                }
            }
        }

        return root
    }
}