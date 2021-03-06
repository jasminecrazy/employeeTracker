package com.suong.Api

import com.suong.model.*
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit


interface ApiApp {
    @POST("admin/api/login")
    fun login(@Body employee: Employee): Observable<ResultInfoUser>

    @POST("admin/api/location")
    fun sendLocation(@Body location: sendLocation): Observable<ResponseBody>

    @POST("admin/api/history")
    fun sendLocationHistory(@Body location: sendLocation): Observable<ResponseBody>

    @POST("admin/api/absence")
    fun sendAbsense(@Body senabsense: sendAbsenseToSever): Observable<ResponseBody>

    @GET("admin/api/shiftwork")
    fun getshiftwork(): Observable<List<ResponseShiftWork>>

    @GET("admin/api/absenceForm/{id}")
    fun getListDayAbsence(@Path("id") id: String): Observable<List<ResponseAbsenceForm>>

    @GET("admin/api/employeeTask/{id}")
    fun getListWorkSchedule(@Path("id") id: String): Observable<List<ResponseWorkSchedule>>

    @GET("admin/api/getTask/{workday}/{id}")
    fun getListWorkSheculeFromDate(@Path("workday") workday: String, @Path("id") id: String): Observable<List<ResponseWorkSchedule>>

    companion object Factory {
        fun create(): ApiApp {
            val okhttp = OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build()
            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okhttp)
                    .baseUrl("https://trackemployee.herokuapp.com/")
                    .build()
            return retrofit.create(ApiApp::class.java)
        }
    }
}