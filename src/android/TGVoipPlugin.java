package org.parler;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import org.parler.messenger.voip.Instance;
import org.parler.messenger.voip.NativeInstance;
import org.parler.messenger.ApplicationLoader;
import org.parler.tgnet.TLRPC;
import org.webrtc.VideoSink;
import org.webrtc.VideoFrame;
import android.telephony.TelephonyManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;


public class TGVoipPlugin extends CordovaPlugin {

    protected static final String TAG = "TGVoipPlugin";
    protected CallbackContext context;
    protected NativeInstance tgVoip;    
    protected TLRPC.PhoneCall privateCall;
    private byte[] authKey = new byte[] {66, 47, 57, 44, -102, 122, -14, -47, 45, 16, -123, -61, 127, -33, -124, -18, -36, -9, 127, 104, 27, 17, 67, -17, 103, 32, 113, -121, 3, -16, 124, 39, -124, -124, -125, -61, 126, -103, 31, -118, -31, 44, 94, 127, 1, 15, -28, 20, -119, -106, 42, 33, -67, 53, -117, 113, -55, -1, 15, 12, 76, -65, -107, 98, 31, -4, -100, -79, -112, 5, -79, -115, 20, 99, -113, -16, 42, -16, -107, 86, -99, 51, -110, 43, 107, 123, -63, 73, 0, 7, -28, 101, 105, -100, 1, 58, -64, -118, 94, -22, 48, -68, 37, -50, -108, 28, -68, -108, -21, -42, 120, -119, 93, -21, 17, -44, 40, 40, -100, -58, 15, 10, 1, -31, -66, -77, 116, -23, 53, -83, 73, -40, 125, 46, 37, -63, 125, -55, -108, 62, -32, 70, -17, -54, -20, 72, -30, 73, 116, 81, -62, 61, 74, -17, -61, -57, -88, -40, -70, 90, -34, -128, -23, 123, 108, 58, -41, -118, 120, 72, -33, -109, -5, -100, 44, 17, -65, -110, -48, -73, -108, -49, 49, 29, 16, 96, 2, -40, 114, 2, -60, 70, -83, -111, 27, -86, -2, 92, 37, -100, 3, -50, -30, 78, 34, -12, -22, -39, -120, 14, -122, 99, 57, 79, -24, 87, 105, -111, 21, -113, -111, -127, 8, -17, 7, 73, 116, 75, -110, -109, 20, 59, 11, 117, 15, -27, -80, 64, -52, 8, -39, 80, -2, 3, 2, 64, 49, -1, 64, 83, 80, -3, -124, -67, -98, -57};
    protected long videoCapturer;
    private ProxyVideoSink localSink;
	private ProxyVideoSink remoteSink;
    private boolean micMute = false;
	protected NetworkInfo lastNetInfo;

    private int convertDataSavingMode(int mode) {
		if (mode != Instance.DATA_SAVING_ROAMING) {
			return mode;
		}
		return ApplicationLoader.isRoaming() ? Instance.DATA_SAVING_MOBILE : Instance.DATA_SAVING_NEVER;
	}

	private static class ProxyVideoSink implements VideoSink {
		private VideoSink target;
		private VideoSink background;

		@Override
		synchronized public void onFrame(VideoFrame frame) {
			if (target == null) {
				return;
			}

			target.onFrame(frame);
			if (background != null) {
				background.onFrame(frame);
			}
		}

		synchronized public void setTarget(VideoSink target) {
			this.target = target;
		}

		synchronized public void setBackground(VideoSink background) {
			this.background = background;
		}

		synchronized public void swap() {
			if (target != null && background != null) {
				target = background;
				background = null;
			}
		}
	}
	// protected NetworkInfo getActiveNetworkInfo() {
	// 	return ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
	// }
	protected int getNetworkType() {
        return Instance.NET_TYPE_WIFI;
		// final NetworkInfo info = lastNetInfo = getActiveNetworkInfo();
		// int type = Instance.NET_TYPE_UNKNOWN;
		// if (info != null) {
		// 	switch (info.getType()) {
		// 		case ConnectivityManager.TYPE_MOBILE:
		// 			switch (info.getSubtype()) {
		// 				case TelephonyManager.NETWORK_TYPE_GPRS:
		// 					type = Instance.NET_TYPE_GPRS;
		// 					break;
		// 				case TelephonyManager.NETWORK_TYPE_EDGE:
		// 				case TelephonyManager.NETWORK_TYPE_1xRTT:
		// 					type = Instance.NET_TYPE_EDGE;
		// 					break;
		// 				case TelephonyManager.NETWORK_TYPE_UMTS:
		// 				case TelephonyManager.NETWORK_TYPE_EVDO_0:
		// 					type = Instance.NET_TYPE_3G;
		// 					break;
		// 				case TelephonyManager.NETWORK_TYPE_HSDPA:
		// 				case TelephonyManager.NETWORK_TYPE_HSPA:
		// 				case TelephonyManager.NETWORK_TYPE_HSPAP:
		// 				case TelephonyManager.NETWORK_TYPE_HSUPA:
		// 				case TelephonyManager.NETWORK_TYPE_EVDO_A:
		// 				case TelephonyManager.NETWORK_TYPE_EVDO_B:
		// 					type = Instance.NET_TYPE_HSPA;
		// 					break;
		// 				case TelephonyManager.NETWORK_TYPE_LTE:
		// 					type = Instance.NET_TYPE_LTE;
		// 					break;
		// 				default:
		// 					type = Instance.NET_TYPE_OTHER_MOBILE;
		// 					break;
		// 			}
		// 			break;
		// 		case ConnectivityManager.TYPE_WIFI:
		// 			type = Instance.NET_TYPE_WIFI;
		// 			break;
		// 		case ConnectivityManager.TYPE_ETHERNET:
		// 			type = Instance.NET_TYPE_ETHERNET;
		// 			break;
		// 	}
		// }
		// return type;
	}

    TGVoipPlugin(){
        super();
        localSink = new ProxyVideoSink();
		remoteSink = new ProxyVideoSink();
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        context = callbackContext;
        boolean result = true;
        boolean forceTcp = false;
        try {
            // privateCall = new TLRPC.PhoneCall.TLdeserialize(,0x8742ae7f, true);
            if (action.equals("test")) {
                final double initializationTimeout = 30000 / 1000.0;
                final double receiveTimeout = 10000 / 1000.0;
                final int voipDataSaving = convertDataSavingMode(Instance.DATA_SAVING_NEVER);
                final String logFilePath = "";
                final String statisLogFilePath = "";
                final boolean isOutgoing = false;

                final Instance.Config config = new Instance.Config(initializationTimeout, receiveTimeout, voipDataSaving, true/*privateCall.p2p_allowed*/, true, true, true,
                    false, false, logFilePath, statisLogFilePath, 32/*privateCall.protocol.max_layer*/);

                final String persistentStateFilePath = new File(ApplicationLoader.applicationContext.getFilesDir(), "voip_persistent_state.json").getAbsolutePath();
                final int endpointType = forceTcp ? Instance.ENDPOINT_TYPE_TCP_RELAY : Instance.ENDPOINT_TYPE_UDP_RELAY;
                final Instance.Endpoint[] endpoints = new Instance.Endpoint[0/*privateCall.connections.size()*/];
                for (int i = 0; i < endpoints.length; i++) {
                    final TLRPC.PhoneConnection connection = privateCall.connections.get(i);
                    endpoints[i] = new Instance.Endpoint(connection instanceof TLRPC.TL_phoneConnectionWebrtc, connection.id, connection.ip, connection.ipv6, connection.port, 
                    endpointType, connection.peer_tag, connection.turn, connection.stun, connection.username, connection.password);
                }

                Instance.Proxy proxy = null;
                final Instance.EncryptionKey encryptionKey = new Instance.EncryptionKey(authKey, isOutgoing);

                videoCapturer = 0;

                tgVoip = Instance.makeInstance("2.4.4", config, persistentStateFilePath, endpoints, proxy, getNetworkType(), encryptionKey, remoteSink, videoCapturer);
                tgVoip.setMuteMicrophone(micMute);

                callbackContext.success("It works!");
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
