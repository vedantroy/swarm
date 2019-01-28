package rstudio.vedantroy.swarm

data class Post(val content: String, val votes: Int)

class ComparePosts {
    companion object : Comparator<Post> {
        override fun compare(post1: Post, post2: Post) = post1.votes - post2.votes
    }
}