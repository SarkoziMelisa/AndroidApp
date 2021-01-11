package myapp.todo.history.data.remote

import retrofit2.http.*
import myapp.core.Api
import myapp.todo.history.data.Actor

object ActorApi {
    interface Service {
        @GET("/api/actor")
        suspend fun find(): List<Actor>

        @GET("/api/actor/{id}")
        suspend fun read(@Path("id") actorId: String): Actor;

        @Headers("Content-Type: application/json")
        @POST("/api/actor")
        suspend fun create(@Body actor: Actor): Actor

        @Headers("Content-Type: application/json")
        @PUT("/api/actor/{id}")
        suspend fun update(@Path("id") actorId: String, @Body actor: Actor): Actor

        @Headers("Content-Type: application/json")
        @DELETE("/api/actor/{id}")
        suspend fun delete(@Path("id") actorId: String): Actor
    }

    val service: Service = Api.retrofit.create(Service::class.java)
}