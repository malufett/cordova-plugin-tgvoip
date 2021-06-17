package org.parler;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.parler.messenger.Utilities;
import org.json.JSONException;
import org.parler.tgnet.TLRPC;
import org.json.JSONArray;
import java.util.ArrayList;
import org.json.JSONObject;

import android.util.Log;

public class TGVoipPlugin extends CordovaPlugin {
    protected static final String TAG = "TGVoipPlugin";
    protected TGVoipJni jni;

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
        Log.d(TAG, "executed 'excute' function");
        Log.d(TAG, "execute action:" + action);

        if (action.equals("generateFingerprint")) {
            try{
                JSONArray jGB = args.getJSONArray(0);
                JSONArray jAB = args.getJSONArray(1);
                byte[] g_b = new byte[jGB.length()];
                byte[] a_or_b = new byte[jGB.length()];

                for(int i = 0; i < jGB.length(); i++)
                    g_b[i] = (byte) jGB.getInt(i);
                for(int i = 0; i < jAB.length(); i++)
                    a_or_b[i] = (byte) jAB.getInt(i);

                long fingerprint = TGVoipJni.generateFingerprint(g_b, a_or_b);
                callbackContext.success("" + fingerprint);
            } catch(Exception e) {
                Log.e(TAG, "exeption:" + e.getMessage());
                callbackContext.error("Error encountered: " + e.getMessage());
                return false;
            }
        } else if (action.equals("generateG_A")) {
            try{
                JSONArray jRandom = args.getJSONArray(0);
                byte[] random = new byte[jRandom.length()];

                for(int i = 0; i < jRandom.length(); i++)
                    random[i] = (byte) jRandom.getInt(i);

                byte[] a_or_b = TGVoipJni.generateSalt(random);
                byte[] ga = TGVoipJni.generateG_A(a_or_b);
                byte[] hash = Utilities.computeSHA256(ga, 0, ga.length);

                JSONObject retval = new JSONObject();
                JSONArray retvalA = new JSONArray();
                JSONArray retvalHash = new JSONArray();
                JSONArray retvalAB = new JSONArray();

                for(int i = 0; i < ga.length; i++)
                    retvalA.put((int)ga[i]&0xFF);                    
                for(int i = 0; i < hash.length; i++)
                    retvalHash.put((int)hash[i]&0xFF);
                for(int i = 0; i < a_or_b.length; i++)
                    retvalAB.put((int)a_or_b[i]&0xFF);

                retval.put("g_a", retvalA);
                retval.put("g_a_hash", retvalHash);
                retval.put("a_or_b", retvalAB);

                callbackContext.success(retval);
            } catch(Exception e) {
                Log.e(TAG, "exeption:" + e.getMessage());
                callbackContext.error("Error encountered: " + e.getMessage());
                return false;
            }
        } else if(action.equals("generateG_B")) {
            try {
                JSONArray jRandom = args.getJSONArray(0);
                byte[] random = new byte[jRandom.length()];

                for(int i = 0; i < jRandom.length(); i++)
                    random[i] = (byte) jRandom.getInt(i);
                byte[] gb = TGVoipJni.generateG_B(random);

                JSONArray retval = new JSONArray();
                for(int i = 0; i < gb.length; i++)
                    retval.put((int)gb[i]&0xFF);
                    
                callbackContext.success(retval);
            } catch(Exception e) {
                Log.e(TAG, "exeption:" + e.getMessage());
                callbackContext.error("Error encountered: " + e.getMessage());
                return false;
            }
        } else if(action.equals("createCall")) {
            try {
                JSONObject phoneCall = args.getJSONObject(0);
                JSONArray GAHASH = args.getJSONArray(1);
                boolean isOutgoing = args.getBoolean(2);
                TLRPC.TL_phoneCall temp = new TLRPC.TL_phoneCall();
                byte[] g_a_hash = new byte[GAHASH.length()];

                for(int i = 0; i < GAHASH.length(); i ++){
                    g_a_hash[i] = (byte) GAHASH.getInt(i);
                }

                temp.flags = phoneCall.getInt("flags");
                temp.p2p_allowed = (temp.flags & 32) != 0;                
                temp.video = (temp.flags & 64) != 0;  
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
                temp.protocol.udp_p2p =  (temp.protocol.flags & 1) != 0;
                temp.protocol.udp_reflector = (temp.protocol.flags & 2) != 0;
                temp.protocol.min_layer = tempProtocol.getInt("min_layer");
                temp.protocol.max_layer = tempProtocol.getInt("max_layer");

                JSONArray library_versionsArr = tempProtocol.getJSONArray("library_versions");
                temp.protocol.library_versions = new ArrayList<>();
                for(int i = 0; i < library_versionsArr.length(); i++)
                    temp.protocol.library_versions.add(library_versionsArr.getString(i));
                
                JSONArray connectionsArr = phoneCall.getJSONArray("connections");
                temp.connections = new ArrayList<>();
                for(int i = 0; i < connectionsArr.length(); i++){
                    JSONObject conn = connectionsArr.getJSONObject(i);
                    String type = conn.getString("_");
                    TLRPC.PhoneConnection result = null; 

                    if (type == "phoneConnection") {
                        result = new TLRPC.TL_phoneConnection();
                            JSONArray perrArr = conn.getJSONArray("peer_tag");
                            result.peer_tag =  new byte[perrArr.length()];
                            for(int x = 0; x < perrArr.length(); x++) 
                                result.peer_tag[x] = (byte)perrArr.getInt(x);                              
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
                // temp.need_rating = phoneCall.getBoolean("need_rating");
                // temp.need_debug = phoneCall.getBoolean("need_debug");

                // JSONObject reasonObj = phoneCall.getJSONObject("reason");
                // temp.reason = null;
                // switch(reasonObj.getString("_")){
                //     case "phoneCallDiscardReasonHangup":
                //         temp.reason = new TLRPC.TL_phoneCallDiscardReasonHangup();
                //         break;
                //     case "phoneCallDiscardReasonBusy":
                //         temp.reason = new TLRPC.TL_phoneCallDiscardReasonBusy();
                //         break;
                //     case "phoneCallDiscardReasonMissed":
                //         temp.reason = new TLRPC.TL_phoneCallDiscardReasonMissed();
                //         break;
                //     case "phoneCallDiscardReasonDisconnect":
                //         temp.reason = new TLRPC.TL_phoneCallDiscardReasonDisconnect();
                // }

                // temp.duration = phoneCall.getInt("duration");

                // JSONArray tempArr2 = phoneCall.getJSONArray("g_a_hash");
                // temp.g_a_hash = new byte[tempArr2.length()];
                // for(int i = 0; i < tempArr2.length(); i++)
                //     temp.g_a_hash[i] = (byte)tempArr.getInt(i);   

                // JSONArray tempArr3 = phoneCall.getJSONArray("g_a_hash");
                // temp.g_b = new byte[tempArr3.length()];
                // for(int i = 0; i < tempArr3.length(); i++)
                //     temp.g_b[i] = (byte)tempArr3.getInt(i);   
        
                // temp.receive_date = phoneCall.getInt("receive_date");

                jni = new TGVoipJni();
                jni.createCall(temp, g_a_hash, isOutgoing);

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