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
import org.parler.messenger.NativeLoader;


public class TGVoipJni {	
    protected static final String TAG = "TGVoipPlugin";
    protected CallbackContext context;
    protected NativeInstance tgVoip;    
    protected long videoCapturer;
    private ProxyVideoSink localSink;
	private ProxyVideoSink remoteSink;
    private boolean micMute = false;
	protected NetworkInfo lastNetInfo;
	private final static int LIB_VERSION = 35;
    private final static String LIB_NAME = "tmessages." + LIB_VERSION;

    protected TLRPC.PhoneCall privateCall;	
	private byte[] g_a;
	private byte[] a_or_b;
	private byte[] g_a_hash;
	private byte[] authKey;
	private long keyFingerprint;
	private byte[] secretPBytes = new byte[]{ 199, 28, 174, 185, 198, 177, 201, 4, 142, 108, 82, 47, 112, 241, 63, 115, 152, 13, 64, 35, 142, 62, 33, 193, 73, 52, 208, 55, 86, 61, 147, 15, 72, 25, 138, 10, 167, 193, 64, 88, 34, 148, 147, 210, 37, 48, 244, 219, 250, 51, 111, 110, 10, 201, 37, 19, 149, 67, 174, 212, 76, 206, 124, 55, 32, 253, 81, 246, 148, 88, 112, 90, 198, 140, 212, 254, 107, 107, 19, 171, 220, 151, 70, 81, 41, 105, 50, 132, 84, 241, 143, 175, 140, 89, 95, 100, 36, 119, 254, 150, 187, 42, 148, 29, 91, 205, 29, 74, 200, 204, 73, 136, 7, 8, 250, 155, 55, 142, 60, 79, 58, 144, 96, 190, 230, 124, 249, 164, 164, 166, 149, 129, 16, 81, 144, 126, 22, 39, 83, 181, 107, 15, 107, 65, 13, 186, 116, 216, 168, 75, 42, 20, 179, 20, 78, 14, 241, 40, 71, 84, 253, 23, 237, 149, 13, 89, 101, 180, 185, 221, 70, 88, 45, 177, 23, 141, 22, 156, 107, 196, 101, 176, 214, 255, 156, 163, 146, 143, 239, 91, 154, 228, 228, 24, 252, 21, 232, 62, 190, 160, 248, 127, 169, 255, 94, 237, 112, 5, 13, 237, 40, 73, 244, 123, 249, 89, 217, 86, 133, 12, 233, 41, 133, 31, 13, 129, 21, 246, 53, 177, 5, 238, 46, 78, 21, 208, 75, 36, 84, 191, 111, 79, 173, 240, 52, 177, 4, 3, 17, 156, 216, 227, 185, 47, 204, 91};

    static {
		System.loadLibrary(LIB_NAME);		
    }

	TGVoipJni(){		
        localSink = new ProxyVideoSink();
		remoteSink = new ProxyVideoSink();
	}

	public void createCall(TLRPC.PhoneCall phoneCall, boolean isOutgoing){
		if (phoneCall.g_a_or_b == null) {
			callFailed(tgVoip != null ? tgVoip.getLastError() : Instance.ERROR_UNKNOWN);
			return;
		}
		if (!Arrays.equals(g_a_hash, Utilities.computeSHA256(phoneCall.g_a_or_b, 0, phoneCall.g_a_or_b.length))) {
			callFailed(tgVoip != null ? tgVoip.getLastError() : Instance.ERROR_UNKNOWN);
			return;
		}
		g_a = phoneCall.g_a_or_b;
		BigInteger g_a = new BigInteger(1, phoneCall.g_a_or_b);
		BigInteger p = new BigInteger(1, secretPBytes);

		if (!Utilities.isGoodGaAndGb(g_a, p)) {
			callFailed(tgVoip != null ? tgVoip.getLastError() : Instance.ERROR_UNKNOWN);
			return;
		}
		g_a = g_a.modPow(new BigInteger(1, a_or_b), p);

		byte[] authKey = g_a.toByteArray();
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
		VoIPService.this.authKey = authKey;
		keyFingerprint = Utilities.bytesToLong(authKeyId);

		if (keyFingerprint != phoneCall.key_fingerprint) {
			callFailed(tgVoip != null ? tgVoip.getLastError() : Instance.ERROR_UNKNOWN);
			return;
		}

		privateCall = phoneCall;
		this.initiateActualEncryptedCall(isOutgoing);
	}

	public long getCallID() {
		return privateCall != null ? privateCall.id : 0;
	}

	protected void callFailed(String error) {		
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
		tgVoip.setMuteMicrophone(micMute);
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
