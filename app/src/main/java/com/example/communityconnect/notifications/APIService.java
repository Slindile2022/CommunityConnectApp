package com.example.communityconnect.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAApwKJ6dk:APA91bH1OeCBs0jCypk5iuDAwO0Iaz3hmNbzyip8eyTwlk50700sE6Y7u-w4R_zR1CFCdzNsrfsGouSpTvkwMig6OV2R0B8B8z1_nbxgeI_KzmAbVEPB16HUYvhl1R-gab5j1GFLisIg"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
