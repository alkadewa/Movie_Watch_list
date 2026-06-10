package com.example.moviewatchlist;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TMDBService {
    @GET("trending/movie/day")
    Call<TMDBResponse> getTrendingMovies(@Query("api_key") String apiKey);
}
