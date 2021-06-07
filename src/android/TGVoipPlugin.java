package org.parler;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class TGVoipPlugin extends CordovaPlugin {
    protected static final String TAG = "TGVoipPlugin";
  
    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
        Log.d(TAG, "executed 'excute' function");
        Log.d(TAG, "execute action:" + action);
        if (action.equals("test")) {
            try {
                JSONObject options = args.getJSONObject(0);
                String message = options.getString("message");
                String duration = options.getString("duration");

                Log.d(TAG, "result:" + (message + duration));
                
                TGVoipJni jni = new TGVoipJni();
                jni.initiateActualEncryptedCall();
                callbackContext.success(message + duration);
            } catch (JSONException e) {                
                Log.e(TAG, "exeption:" + e.getMessage());
                callbackContext.error("Error encountered: " + e.getMessage());
                return false;
            }
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
            callbackContext.sendPluginResult(pluginResult);
        }
        return true;
    }
    
}