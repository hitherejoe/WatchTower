package com.hitherejoe.watchtower.data.remote;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.GsonBuilder;
import com.hitherejoe.watchtower.WatchTowerApplication;
import com.hitherejoe.watchtower.data.BusEvent;

import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

public class RetrofitHelper {

    public WatchTowerService newWatchTowerService(final Context context) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(WatchTowerService.ENDPOINT)
                .setErrorHandler(new ResponseErrorHandler(context))
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

    private class ResponseErrorHandler implements ErrorHandler {

        private Context mContext;

        public ResponseErrorHandler(Context context) {
            mContext = context;
        }

        @Override public Throwable handleError(RetrofitError cause) {
            Response response = cause.getResponse();
            if (response != null && response.getStatus() == 401) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        WatchTowerApplication.get(mContext).getComponent().eventBus().post(new BusEvent.AuthenticationError());
                    }
                });
            }
            return cause;
        }
    }

}
