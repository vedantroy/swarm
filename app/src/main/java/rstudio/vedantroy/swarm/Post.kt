package rstudio.vedantroy.swarm

data class Post(val postData: PostData, var isLiked : Boolean = false)

data class PostData(val immutablePostData: ImmutablePostData, var votes: Int)

data class ImmutablePostData(val content: String, val creator: String, var count: Int)

class ComparePosts {
    companion object : Comparator<Post> {
        override fun compare(post1: Post, post2: Post) = post1.postData.votes - post2.postData.votes
    }
}