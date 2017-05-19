package com.brg.roadtrip

import com.brg.roadtrip.model.LandmarkResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by johnnymoralez on 5/19/17.
 */
interface RoadTripNetworkingService {
    ///api.geckolandmarks.com/json?lat=33.9188739&lon=-84.4926791&api_key=EXAMPLE_KEY_3edaba1953abf86
    @GET("json?api_key=EXAMPLE_KEY_3edaba1953abf86")
    fun getLandmarks(@Query("lat") lat: Double, @Query("lon") lon: Double): Call<LandmarkResponse>
}