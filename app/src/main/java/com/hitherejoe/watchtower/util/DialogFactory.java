package com.hitherejoe.watchtower.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.text.InputType;
import android.widget.EditText;

import com.google.gson.Gson;
import com.hitherejoe.watchtower.R;
import com.hitherejoe.watchtower.data.model.ErrorResponse;

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

    public static Dialog createSimpleErrorDialog(Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.dialog_error_title))
                .setMessage(context.getString(R.string.dialog_general_error_Message))
                .setNeutralButton(R.string.dialog_action_ok, null);
        return alertDialog.create();
    }

    public static Dialog createRetrofitErrorDialog(Context context, RetrofitError error) {
        ErrorResponse errorResponse = DataUtils.parseRetrofitError(error);
        return createSimpleOkErrorDialog(context, "Error: " + errorResponse.error.code, errorResponse.error.message);
    }

    public static ProgressDialog createProgressDialog(Context context, String message) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        return progressDialog;
    }

    public static Dialog createInputDialog(Context context, String title, String message, final DialogInputCallback inputCallback) {
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(context.getString(R.string.dialog_hint_namespaced_type));
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setView(input)
                .setPositiveButton(context.getString(R.string.dialog_action_delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        inputCallback.onInputSubmitted(input.getText().toString());
                    }
                })
                .setNegativeButton(context.getString(R.string.dialog_action_cancel), null);
        return alertDialog.create();
    }

    public static Dialog createAuthErrorDialog(Context context, DialogInterface.OnClickListener onPositiveClick, DialogInterface.OnClickListener onNegativeClick) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.dialog_error_title))
                .setMessage(context.getString(R.string.dialog_error_unauthorised_response))
                .setPositiveButton(R.string.dialog_action_authorise, onPositiveClick)
                .setNegativeButton(R.string.dialog_action_exit, onNegativeClick);
        return alertDialog.create();
    }

    public static ProgressDialog createProgressDialog(Context context, @StringRes int messageResoruce) {
        return createProgressDialog(context, context.getString(messageResoruce));
    }

    public interface DialogInputCallback {
        String onInputSubmitted(String input);
    }
}
