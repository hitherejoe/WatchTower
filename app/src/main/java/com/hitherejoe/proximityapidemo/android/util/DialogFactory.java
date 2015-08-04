package com.hitherejoe.proximityapidemo.android.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.StringRes;

import com.google.gson.Gson;
import com.hitherejoe.proximityapidemo.android.R;
import com.hitherejoe.proximityapidemo.android.data.model.ErrorResponse;

import retrofit.RetrofitError;
import retrofit.mime.TypedByteArray;

public class DialogFactory {

    public static Dialog createSimpleOkErrorDialog(Context context, String title, String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(R.string.dialog_action_ok, null);
        return alertDialog.create();
    }

    public static Dialog createRetrofitErrorDialog(Context context, RetrofitError error) {
        String json = new String(((TypedByteArray) error.getResponse().getBody()).getBytes());
        ErrorResponse errorResponse = new Gson().fromJson(json, ErrorResponse.class);
        return createSimpleOkErrorDialog(context, "Error: " + errorResponse.error.code, errorResponse.error.message);
    }

    public static ProgressDialog createProgressDialog(Context context, String message) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        return progressDialog;
    }

    public static ProgressDialog createProgressDialog(Context context, @StringRes int messageResoruce) {
        return createProgressDialog(context, context.getString(messageResoruce));
    }
}
