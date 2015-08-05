package com.hitherejoe.watchtower.data.remote;

import com.google.gson.GsonBuilder;
import com.hitherejoe.watchtower.WatchTowerApplication;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class RetrofitHelper {

    public WatchTowerService setupProximityApiService() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(WatchTowerService.ENDPOINT)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(new GsonBuilder().create()))
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestInterceptor.RequestFacade request) {
                        String accessToken = WatchTowerApplication.get().getDataManager().getPreferencesHelper().getToken();

                        if (accessToken != null) {
                            request.addHeader("Authorization", addBearerToken(accessToken));
                        }
                    }
                })
                .build();
        return restAdapter.create(WatchTowerService.class);
    }

    public static String addBearerToken(String authToken) {
        return "Bearer " + authToken;
    }

}
