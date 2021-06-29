package org.parler;

import org.parler.messenger.ApplicationLoader;
import org.parler.messenger.voip.Instance;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.parler.messenger.Utilities;
import org.json.JSONException;
import org.parler.tgnet.TLRPC;
import java.util.ArrayList;
import org.json.JSONObject;
import org.json.JSONArray;
import android.os.Build;

import android.util.Log;

public class TGVoipPlugin extends CordovaPlugin {
    protected static final String TAG = "TGVoipPlugin";
    protected TGVoipJni jni;
    protected int testCounter = 0;

    public TGVoipPlugin(){
        super();        
    }

    public byte[] JSONArray2Bytes(JSONArray jarray) throws JSONException {
        byte[] retval = new byte[jarray.length()];
        for(int i = 0; i < jarray.length(); i++)
            retval[i] = (byte) jarray.getInt(i);
        return retval;
    }

    public JSONArray Bytes2JSONArray(byte[] data) throws JSONException {
        JSONArray retval = new JSONArray();
        for(int i = 0; i < data.length; i++)
            retval.put((int)data[i]&0xFF);
        return retval;
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
        ApplicationLoader.applicationContext = Build.VERSION.SDK_INT >= 21 ? cordova.getActivity().getWindow().getContext() : cordova.getActivity().getApplicationContext();
        Log.d(TAG, "executed 'excute' function");
        Log.d(TAG, "execute action:" + action);

        if (action.equals("test")) { 
            try{
                testCounter++;
                JSONObject retval = new JSONObject();
                retval.put("counter", testCounter);
                Log.d(TAG, "test counter:" + testCounter);
                callbackContext.success(retval);
            } catch(Exception e) {
                Log.e(TAG, "exeption:" + e.getMessage());
                callbackContext.error(action + ": Error encountered: " + e.getMessage());
                return false;
            }
        } else if (action.equals("generateFingerprint")) {
            try{
                JSONArray jGB = args.getJSONArray(0);
                JSONArray jAB = args.getJSONArray(1);
                byte[] g_b = JSONArray2Bytes(jGB);
                byte[] a_or_b = JSONArray2Bytes(jGB);

                long fingerprint = TGVoipJni.generateFingerprint(g_b, a_or_b);
                callbackContext.success("" + fingerprint);
            } catch(Exception e) {
                Log.e(TAG, "exeption:" + e.getMessage());
                callbackContext.error(action + ": Error encountered: " + e.getMessage());
                return false;
            }
        } else if (action.equals("generateG_A")) {
            try{
                JSONArray jRandom = args.getJSONArray(0);
                byte[] random = JSONArray2Bytes(jRandom);           
                byte[] a_or_b = TGVoipJni.generateSalt(random);
                byte[] ga = TGVoipJni.generateG_A(a_or_b);
                byte[] hash = Utilities.computeSHA256(ga, 0, ga.length);

                JSONObject retval = new JSONObject();
                JSONArray retvalA = Bytes2JSONArray(ga);
                JSONArray retvalHash = Bytes2JSONArray(hash);
                JSONArray retvalAB = new JSONArray(a_or_b);

                retval.put("g_a", retvalA);
                retval.put("g_a_hash", retvalHash);
                retval.put("a_or_b", retvalAB);

                callbackContext.success(retval);
            } catch(Exception e) {
                Log.e(TAG, "exeption:" + e.getMessage());
                callbackContext.error(action + ": Error encountered: " + e.getMessage());
                return false;
            }
        } else if (action.equals("setGlobalServerConfig")) {
            try {
                JSONObject config = args.getJSONObject(0);
                Instance.setGlobalServerConfig(config.toString());
                callbackContext.success(1);
            } catch (Exception e) {                
                Log.e(TAG, "exeption:" + e.getMessage());
                callbackContext.error(action + ": Error encountered: " + e.getMessage());
                return false;
            }
        }else if (action.equals("generateG_B")) {
            try {
                JSONArray jRandom = args.getJSONArray(0);
                byte[] random = JSONArray2Bytes(jRandom);
                byte[] gb = TGVoipJni.generateG_B(random);
                JSONArray retval = Bytes2JSONArray(gb);
                    
                callbackContext.success(retval);
            } catch(Exception e) {
                Log.e(TAG, "exeption:" + e.getMessage());
                callbackContext.error(action + ": Error encountered: " + e.getMessage());
                return false;
            }
        } else if (action.equals("receiveSignalingData")) {
            try {
                long call_id = args.getLong(0);
                JSONArray array = args.getJSONArray(1);
                byte[] data = JSONArray2Bytes(array);

                jni.receiveSignalingData(call_id, data);
            } catch(Exception e) {
                Log.e(TAG, "exeption:" + e.getMessage());
                callbackContext.error(action + ": Error encountered: " + e.getMessage());
                return false;
            }
        } else if(action.equals("createCall")) {
            try {
                JSONObject phoneCall = args.getJSONObject(0);
                JSONArray jGB = args.getJSONArray(1);
                JSONArray jAORB = args.getJSONArray(2);
                JSONArray GAHASH = args.getJSONArray(3);
                boolean isOutgoing = args.getBoolean(4);

                TLRPC.TL_phoneCall temp = new TLRPC.TL_phoneCall();
                byte[] g_b = JSONArray2Bytes(jGB);
                byte[] a_or_b = JSONArray2Bytes(jAORB);
                byte[] g_a_hash = JSONArray2Bytes(GAHASH);

                temp.flags = phoneCall.getInt("flags");
                temp.p2p_allowed = (temp.flags & 32) != 0;                
                temp.video = (temp.flags & 64) != 0;  
                temp.id = phoneCall.getLong("id");
                temp.access_hash = phoneCall.getLong("access_hash");
                temp.date = phoneCall.getInt("date");
                temp.admin_id = phoneCall.getInt("admin_id");
                temp.participant_id = phoneCall.getInt("participant_id");

                JSONArray tempArr = phoneCall.getJSONArray("g_a_or_b");
                
                temp.g_a_or_b = JSONArray2Bytes(tempArr);                
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
                jni.createCall(temp, g_b, a_or_b, g_a_hash, isOutgoing);
                jni.setCallbackContext(callbackContext);
            } catch(Exception e) {                        
                Log.e(TAG, "exeption:" + e.getMessage());
                callbackContext.error(action + ": Error encountered: " + e.getMessage());
                return false;
            }
            if(!action.equals("createCall")) {
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
                callbackContext.sendPluginResult(pluginResult);
            }
        }
        return true;
    }
    
}