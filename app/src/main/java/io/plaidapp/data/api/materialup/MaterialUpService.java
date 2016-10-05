package io.plaidapp.data.api.materialup;

import java.util.List;

import io.plaidapp.data.api.materialup.model.Comment;
import io.plaidapp.data.api.materialup.model.Post;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Models the MaterialUp API.
 * <p>
 * v1 https://material.uplabs.com/users/api
 */
public interface MaterialUpService {

    String ENDPOINT = "https://material.uplabs.com/api/";

    @GET("v1/posts")
    Call<List<Post>> getPosts(@Query("days_ago") Integer page);

    //@GET("v1/posts/{post_id}") //return same detail
    //Call<Post> getPost(@Path("post_id") long postId);

    @GET("v1/posts/{post_id}/comments")
    Call<List<Comment>> getComments(@Path("post_id") Integer postId);

}


