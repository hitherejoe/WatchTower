package com.hitherejoe.watchtower.data.remote;

import android.content.Context;

import com.google.gson.GsonBuilder;
import com.hitherejoe.watchtower.WatchTowerApplication;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class RetrofitHelper {

    public WatchTowerService newWatchTowerService(final Context context) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(WatchTowerService.ENDPOINT)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(new GsonBuilder().create()))
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestInterceptor.RequestFacade request) {
                        String accessToken = WatchTowerApplication.get(context).getComponent().dataManager().getPreferencesHelper().getToken();

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
