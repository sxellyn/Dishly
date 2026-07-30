package com.dishly.app.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MealServiceAPI {

    companion object {
        const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"
    }

    @GET("random.php")
    fun random(): Call<APIMealsResponse?>

    @GET("lookup.php")
    fun lookup(@Query("i") id: String): Call<APIMealsResponse?>

    @GET("search.php")
    fun search(@Query("s") query: String): Call<APIMealsResponse?>

    @GET("filter.php")
    fun filterByIngredient(@Query("i") ingredient: String): Call<APIMealsResponse?>

    @GET("list.php")
    fun ingredientList(@Query("i") list: String = "list"): Call<APIMealsResponse?>
}
