package com.hitherejoe.proximityapidemo.android.data.remote;

import com.google.gson.GsonBuilder;
import com.hitherejoe.proximityapidemo.android.ProximityApiApplication;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class RetrofitHelper {

    public ProximityApiService setupProximityApiService() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ProximityApiService.ENDPOINT)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(new GsonBuilder().create()))
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestInterceptor.RequestFacade request) {
                        String accessToken = ProximityApiApplication.get().getDataManager().getPreferencesHelper().getToken();

                        if (accessToken != null) {
                            request.addHeader("Authorization", addBearerToken(accessToken));
                        }
                    }
                })
                .build();
        return restAdapter.create(ProximityApiService.class);
    }

    public static String addBearerToken(String authToken) {
        return "Bearer " + authToken;
    }

}
