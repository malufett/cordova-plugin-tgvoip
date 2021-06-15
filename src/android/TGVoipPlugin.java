package org.parler;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.parler.tgnet.TLRPC;
import org.json.JSONArray;
import java.util.ArrayList;
import org.json.JSONObject;

import android.util.Log;

public class TGVoipPlugin extends CordovaPlugin {
    protected static final String TAG = "TGVoipPlugin";
    protected final TGVoipJni jni;

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
                if(jni != null)
                    jni.initiateActualEncryptedCall();
                callbackContext.success(message + duration);
            } catch (JSONException e) {                
                Log.e(TAG, "exeption:" + e.getMessage());
                callbackContext.error("Error encountered: " + e.getMessage());
                return false;
            }
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
            callbackContext.sendPluginResult(pluginResult);
        } else if(action.equals("createCall")) {
            try {
                JSONObject phoneCall = args.getJSONObject(0);
                TLRPC.TL_phoneCall temp = new TLRPC.TL_phoneCall();

                temp.flags = phoneCall.getInt("flags");
                temp.p2p_allowed = phoneCall.getBoolean("p2p_allowed");
                temp.id = phoneCall.getLong("id");
                temp.access_hash = phoneCall.getLong("access_hash");
                temp.date = phoneCall.getInt("date");
                temp.admin_id = phoneCall.getInt("admin_id");
                temp.participant_id = phoneCall.getInt("participant_id");

                JSONArray tempArr = phoneCall.getJSONArray("g_a_or_b");
                temp.g_a_or_b = new byte[tempArr.length()];
                for(int i = 0; i < tempArr.length(); i++)
                    temp.g_a_or_b[i] = (byte)tempArr.getInt(i);                

                temp.key_fingerprint = phoneCall.getLong("key_fingerprint");

                JSONObject tempProtocol = phoneCall.getJSONObject("protocol");
                temp.protocol = new TLRPC.TL_phoneCallProtocol();

                temp.protocol.flags = tempProtocol.getInt("flags");
                temp.protocol.udp_p2p = tempProtocol.getBoolean("udp_p2p");
                temp.protocol.udp_reflector = tempProtocol.getBoolean("udp_reflector");
                temp.protocol.min_layer = tempProtocol.getInt("udp_reflector");
                temp.protocol.max_layer = tempProtocol.getInt("udp_reflector");

                JSONArray library_versionsArr = tempProtocol.getJSONArray("library_versions");
                temp.protocol.library_versions = new ArrayList<>();
                for(int i = 0; i < library_versionsArr.length(); i++){
                    temp.protocol.library_versions.add(library_versionsArr.getString(i));
                }

                JSONArray connectionsArr = tempProtocol.getJSONArray("connections");
                temp.connections = new ArrayList<>();
                for(int i = 0; i < connectionsArr.length(); i++){
                    JSONObject conn = connectionsArr.getJSONObject(i);
                    String type = conn.getString("_");
                    TLRPC.PhoneConnection result = null; 

                    if (type == "phoneConnection") {
                        result = new TLRPC.TL_phoneConnection();
                            JSONArray perrArr = conn.getJSONArray("peer_tag");
                            result.peer_tag =  new byte[perrArr.length()];
                            for(int i = 0; i < perrArr.length(); i++) 
                                result.peer_tag[i] = (byte)perrArr.getInt(i);                              
                    } else {
                        result = new TLRPC.TL_phoneConnectionWebrtc();
                            result.flags = conn.getInt("flags");
                            result.turn = (result.flags  & 1) != 0;
                            result.stun = (result.flags  & 2) != 0;                          
                            result.username = conn.getString("username");
                            result.password = conn.getString("password");
                    }

                    result.id = conn.getLong("id");
                    result.ip = conn.getString("ip");
                    result.ipv6 = conn.getString("ipv6");
                    result.port = conn.getInt("port");
                    temp.connections.add(result);
                }

                temp.start_date = phoneCall.getInt("start_date");
                temp.need_rating = phoneCall.getBoolean("need_rating");
                temp.need_debug = phoneCall.getBoolean("need_debug");
                temp.video = phoneCall.getBoolean("video");

                JSONObject reasonObj = phoneCall.getJSONObject("reason");
                temp.reason = null;
                switch(reasonObj.getString("_")){
                    case "phoneCallDiscardReasonHangup":
                        temp.reason = new TLRPC.TL_phoneCallDiscardReasonHangup();
                        break;
                    case "phoneCallDiscardReasonBusy":
                        temp.reason = new TLRPC.TL_phoneCallDiscardReasonBusy();
                        break;
                    case "phoneCallDiscardReasonMissed":
                        temp.reason = new TLRPC.TL_phoneCallDiscardReasonMissed();
                        break;
                    case "phoneCallDiscardReasonDisconnect":
                        temp.reason = new TLRPC.TL_phoneCallDiscardReasonDisconnect();
                }

                temp.duration = phoneCall.getInt("duration");

                JSONArray tempArr2 = phoneCall.getJSONArray("g_a_hash");
                temp.g_a_hash = new byte[tempArr2.length()];
                for(int i = 0; i < tempArr2.length(); i++)
                    temp.g_a_hash[i] = (byte)tempArr.getInt(i);   

                JSONArray tempArr3 = phoneCall.getJSONArray("g_a_hash");
                temp.g_b = new byte[tempArr3.length()];
                for(int i = 0; i < tempArr3.length(); i++)
                    temp.g_b[i] = (byte)tempArr3.getInt(i);   
        
                temp.receive_date = phoneCall.getInt("receive_date");

                jni = new TGVoipJni();
                jni.createCall(temp);

                callbackContext.success();
            } catch(Exception e) {                        
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