package com.tgvoip.plugin;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import android.app.Activity;
import android.os.Bundle;

public class TGVOIP extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if (action.equals("greet")) {

            String jniString = TGVOIPJni.stringFromJNI();
            String name = data.getString(0);
            String message = "Hello, " + name + ". JNI says: " + jniString;
            callbackContext.success(message);

            return true;

        } else {
            
            return false;

        }
    }
}
