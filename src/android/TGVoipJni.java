package org.parler;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.telephony.TelephonyManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;
import org.parler.messenger.voip.Instance;
import org.parler.messenger.voip.NativeInstance;
import org.parler.messenger.ApplicationLoader;
import org.parler.tgnet.TLRPC;
import org.webrtc.VideoSink;
import org.webrtc.VideoFrame;
import java.io.File;
import java.math.*;
import java.util.*;
import org.parler.messenger.NativeLoader;
import org.parler.messenger.Utilities;


public class TGVoipJni {	
    protected static final String TAG = "TGVoipPlugin";
    protected CallbackContext context;
    protected NativeInstance tgVoip;    
    protected long videoCapturer;
	protected TLRPC.PhoneCall privateCall;	
    private ProxyVideoSink localSink;
	private ProxyVideoSink remoteSink;
    private boolean micMute = false;
	protected NetworkInfo lastNetInfo;
	private CallbackContext callbackContext;

	private byte[] g_a;
	private byte[] a_or_b;
	private byte[] authKey;
	private long keyFingerprint;

	private final static int LIB_VERSION = 35;
    private final static String LIB_NAME = "tmessages." + LIB_VERSION;
	private static byte[] secretPBytes = new byte[]{ 
		-57, 28, -82, -71, -58, -79, -55, 4, -114, 108, 82, 47, 112, -15, 63, 115, -104, 13, 64, 35, -114, 62, 33, -63, 73, 52, -48, 55, 86, 61, -109, 15, 72, 25, -118, 10,
		-89, -63, 64, 88, 34, -108, -109, -46, 37, 48, -12, -37, -6, 51, 111, 110, 10, -55, 37, 19, -107, 67, -82, -44, 76, -50, 124, 55, 32, -3, 81, -10, -108, 88, 112, 90,
		-58, -116, -44, -2, 107, 107, 19, -85, -36, -105, 70, 81, 41, 105, 50, -124, 84, -15, -113, -81, -116, 89, 95, 100, 36, 119, -2, -106, -69, 42, -108, 29, 91, -51, 29,
		74, -56, -52, 73, -120, 7, 8, -6, -101, 55, -114, 60, 79, 58, -112, 96, -66, -26, 124, -7, -92, -92, -90, -107, -127, 16, 81, -112, 126, 22, 39, 83, -75, 107, 15, 107,
		65, 13, -70, 116, -40, -88, 75, 42, 20, -77, 20, 78, 14, -15, 40, 71, 84, -3, 23, -19, -107, 13, 89, 101, -76, -71, -35, 70, 88, 45, -79, 23, -115, 22, -100, 107, -60,
		101, -80, -42, -1, -100, -93, -110, -113, -17, 91, -102, -28, -28, 24, -4, 21, -24, 62, -66, -96, -8, 127, -87, -1, 94, -19, 112, 5, 13, -19, 40, 73, -12, 123, -7, 89,
		-39, 86, -123, 12, -23, 41, -123, 31, 13, -127, 21, -10, 53, -79, 5, -18, 46, 78, 21, -48, 75, 36, 84, -65, 111, 79, -83, -16, 52, -79, 4, 3, 17, -100, -40, -29, -71, 47, -52, 91	
	};

    static {
		System.loadLibrary(LIB_NAME);		
    }

	TGVoipJni(){		
        localSink = new ProxyVideoSink();
		remoteSink = new ProxyVideoSink();
	}

	public static long generateFingerprint(byte[] g_b, byte[] a_or_b) throws Exception {
		byte[] authKey = TGVoipJni.generateAuthKey(g_b, a_or_b);
		byte[] authKeyHash = Utilities.computeSHA1(authKey);
		byte[] authKeyId = new byte[8];
		System.arraycopy(authKeyHash, authKeyHash.length - 8, authKeyId, 0, 8);
		
		return Utilities.bytesToLong(authKeyId);
	}

	public static byte[] generateAuthKey(byte[] g_b, byte[] a_or_b) throws Exception {
		BigInteger p = new BigInteger(1, secretPBytes);
		BigInteger i_authKey = new BigInteger(1, g_b);

		if (!Utilities.isGoodGaAndGb(i_authKey, p)) {
			throw new Exception("Not good g_a_b");
		}

		i_authKey = i_authKey.modPow(new BigInteger(1, a_or_b), p);

		byte[] authKey = i_authKey.toByteArray();
		if (authKey.length > 256) {
			byte[] correctedAuth = new byte[256];
			System.arraycopy(authKey, authKey.length - 256, correctedAuth, 0, 256);
			authKey = correctedAuth;
		} else if (authKey.length < 256) {
			byte[] correctedAuth = new byte[256];
			System.arraycopy(authKey, 0, correctedAuth, 256 - authKey.length, authKey.length);
			for (int a = 0; a < 256 - authKey.length; a++) {
				correctedAuth[a] = 0;
			}
			authKey = correctedAuth;
		}
		return authKey;
	}

	public static byte[] generateSalt(byte[] random) {
		final byte[] salt = new byte[256];
		Utilities.random.nextBytes(salt);

		final byte[] salt1 = new byte[256];
		for (int a = 0; a < 256; a++) {
			salt1[a] = (byte) ((byte) (Utilities.random.nextDouble() * 256) ^ random[a]);
		}
		return salt1;
	}

	public static byte[] generateG_A(byte[] salt) {		
		BigInteger i_g_a = BigInteger.valueOf(3);
		i_g_a = i_g_a.modPow(new BigInteger(1, salt), new BigInteger(1, secretPBytes));
		byte[] g_a = i_g_a.toByteArray();
		if (g_a.length > 256) {
			byte[] correctedAuth = new byte[256];
			System.arraycopy(g_a, 1, correctedAuth, 0, 256);
			g_a = correctedAuth;
		}
		return g_a;
	}

	public static byte[] generateG_B(byte[] random_) {
		byte[] salt = new byte[256];
		for (int a = 0; a < 256; a++) {
			salt[a] = (byte) ((byte) (Utilities.random.nextDouble() * 256) ^ random_[a]);
		}
		
		BigInteger g_b = BigInteger.valueOf(3);
		BigInteger p = new BigInteger(1, secretPBytes);
		g_b = g_b.modPow(new BigInteger(1, salt), p);

		byte[] g_b_bytes = g_b.toByteArray();
		if (g_b_bytes.length > 256) {
			byte[] correctedAuth = new byte[256];
			System.arraycopy(g_b_bytes, 1, correctedAuth, 0, 256);
			g_b_bytes = correctedAuth;
		}
		return g_b_bytes;
	}

	public String bytesToString(byte[] bytes){
		String retval = "[";
		for(int i = 0; i < bytes.length; i++){
			retval += (int)(bytes[i]&0xFF) + ",";
		}
		return retval + "]";
	}

	public void setCallbackContext(final CallbackContext callbackContext) {
		this.callbackContext = callbackContext;
	}

	public void createCall(TLRPC.PhoneCall phoneCall, byte[] g_b, byte[] a_or_b, byte[] g_a_hash, boolean isOutgoing) throws Exception{
		if(!isOutgoing){
			if (phoneCall.g_a_or_b == null) {
				callFailed(tgVoip != null ? tgVoip.getLastError() : "Invalid g_a_or_b");
				return;
			}
			if (!Arrays.equals(g_a_hash, Utilities.computeSHA256(phoneCall.g_a_or_b, 0, phoneCall.g_a_or_b.length))) {
				callFailed(tgVoip != null ? tgVoip.getLastError() : "Invalid GA hash! " + bytesToString(g_a_hash) + "!=" + bytesToString(Utilities.computeSHA256(phoneCall.g_a_or_b, 0, phoneCall.g_a_or_b.length)));
				// Log.e(TAG, e.toString());
				return;
			}
			g_a = phoneCall.g_a_or_b;
			BigInteger g_a = new BigInteger(1, phoneCall.g_a_or_b);
			BigInteger p = new BigInteger(1, secretPBytes);

			if (!Utilities.isGoodGaAndGb(g_a, p)) {
				callFailed(tgVoip != null ? tgVoip.getLastError() : "Bad GA");
				return;
			}
			g_a = g_a.modPow(new BigInteger(1, a_or_b), p);

			authKey = g_a.toByteArray();
			if (authKey.length > 256) {
				byte[] correctedAuth = new byte[256];
				System.arraycopy(authKey, authKey.length - 256, correctedAuth, 0, 256);
				authKey = correctedAuth;
			} else if (authKey.length < 256) {
				byte[] correctedAuth = new byte[256];
				System.arraycopy(authKey, 0, correctedAuth, 256 - authKey.length, authKey.length);
				for (int a = 0; a < 256 - authKey.length; a++) {
					correctedAuth[a] = 0;
				}
				authKey = correctedAuth;
			}
			byte[] authKeyHash = Utilities.computeSHA1(authKey);
			byte[] authKeyId = new byte[8];
			System.arraycopy(authKeyHash, authKeyHash.length - 8, authKeyId, 0, 8);
			keyFingerprint = Utilities.bytesToLong(authKeyId);

			if (keyFingerprint != phoneCall.key_fingerprint) {
				callFailed(tgVoip != null ? tgVoip.getLastError() : "Fingerprint mismatch! " + keyFingerprint + " != " + phoneCall.key_fingerprint);
				return;
			}
		} else {
			authKey = TGVoipJni.generateAuthKey(g_b, a_or_b);
			keyFingerprint = TGVoipJni.generateFingerprint(g_b, a_or_b);
		}
		privateCall = phoneCall;
		this.initiateActualEncryptedCall(isOutgoing);
	}

	public long getCallID() {
		return privateCall != null ? privateCall.id : 0;
	}

	protected void callFailed(String error) throws Exception {		
		throw new Exception("Call " + getCallID() + " failed with error: " + error);
	}

	public void initiateActualEncryptedCall(boolean isOutgoing){
		final double initializationTimeout = 30000 / 1000.0;
		final double receiveTimeout = 10000 / 1000.0;
		final int voipDataSaving = convertDataSavingMode(Instance.DATA_SAVING_NEVER);
		final String logFilePath = "";
		final String statisLogFilePath = "";
		boolean forceTcp = false;

		final Instance.Config config = new Instance.Config(initializationTimeout, receiveTimeout, voipDataSaving, privateCall.p2p_allowed, true, true, true,
			false, false, logFilePath, statisLogFilePath, privateCall.protocol.max_layer);

		final String persistentStateFilePath = ""; //new File(ApplicationLoader.applicationContext.getFilesDir(), "voip_persistent_state.json").getAbsolutePath();
		final int endpointType = forceTcp ? Instance.ENDPOINT_TYPE_TCP_RELAY : Instance.ENDPOINT_TYPE_UDP_RELAY;
		final Instance.Endpoint[] endpoints = new Instance.Endpoint[privateCall.connections.size()];

		for (int i = 0; i < endpoints.length; i++) {
			final TLRPC.PhoneConnection connection = privateCall.connections.get(i);
			endpoints[i] = new Instance.Endpoint(connection instanceof TLRPC.TL_phoneConnectionWebrtc, connection.id, connection.ip, connection.ipv6, connection.port, 
			endpointType, connection.peer_tag, connection.turn, connection.stun, connection.username, connection.password);
		}

		Instance.Proxy proxy = null;
		final Instance.EncryptionKey encryptionKey = new Instance.EncryptionKey(authKey, isOutgoing);

		videoCapturer = 0;

		tgVoip = Instance.makeInstance("2.4.4", config, persistentStateFilePath, endpoints, proxy, getNetworkType(), encryptionKey, remoteSink, videoCapturer);
		tgVoip.setOnSignalDataListener(this::onSignalingData);
		tgVoip.setMuteMicrophone(micMute);
	}
	public void onSignalingData(byte[] data) {
		if (privateCall == null) {
			return;
		}
		JSONObject jObject = new JSONObject();
		JSONArray arr = new JSONArray();

		try {
			jObject.put("_", "phone.sendSignalingData");
			jObject.put("access_hash", privateCall.access_hash);
			jObject.put("id", privateCall.id);
			
			for(int i = 0; i < data.length; i++)
				arr.put((int)data[i]&0xFF);
			jObject.put("data", arr);
		} catch (Exception e) {
 			Log.e(TAG, "exeption:" + e.getMessage());
			callbackContext.error("onSignalingData: Error encountered: " + e.getMessage());
			return;
		}
		
		PluginResult pluginResult = new  PluginResult(PluginResult.Status.OK, jObject); 
		pluginResult.setKeepCallback(true);
		callbackContext.sendPluginResult(pluginResult);
	}
	public void receiveSignalingData(long phone_call_id, byte[] data) {
		if (tgVoip == null || tgVoip.isGroup() || getCallID() != phone_call_id) {
			return;
		}
		tgVoip.onSignalingDataReceive(data);
	}
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
}
