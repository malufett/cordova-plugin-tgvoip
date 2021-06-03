package org.tgvoip;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class TGVoipPlugin extends CordovaPlugin {

    protected static final String TAG = "TGVoipPlugin";
    protected CallbackContext context;
    

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        context = callbackContext;
        boolean result = true;
        try {
            if (action.equals("test")) {
                String output = "";
                callbackContext.success(output);
            } else {
                handleError("Invalid action");
                result = false;
            }
        } catch (Exception e) {
            handleException(e);
            result = false;
        }
        return result;
    }


    /**
     * Handles an error while executing a plugin API method.
     * Calls the registered Javascript plugin error handler callback.
     *
     * @param errorMsg Error message to pass to the JS error handler
     */
    private void handleError(String errorMsg) {
        try {
            Log.e(TAG, errorMsg);
            context.error(errorMsg);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private void handleException(Exception exception) {
        handleError(exception.toString());
    }
}
