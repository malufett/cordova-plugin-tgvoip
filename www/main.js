/*global cordova, module*/

const convertToArray = function (data) {
    if (data instanceof Uint8Array) {
        var temp = data;
        data = [];
        for (var id in temp)
            data.push(temp[id])
    }
    return data || [];
};

module.exports = {
    createCall: function (params, GB, A_OR_B, GA, isOutgoing, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "TGVoipPlugin", "createCall", [params, convertToArray(GB), convertToArray(A_OR_B), convertToArray(GA), isOutgoing]);
    },
    receiveSignalingData: function (callId, data, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "TGVoipPlugin", "receiveSignalingData", [callId, convertToArray(data)]);
    },
    generateGA: function (random, successCallback, errorCallback) {
        var successCallback_ = function (result) {
            result.g_a = new Uint8Array(result.g_a);
            result.g_a_hash = new Uint8Array(result.g_a_hash);
            result.a_or_b = new Uint8Array(result.a_or_b);
            successCallback(result);
        };
        cordova.exec(successCallback_, errorCallback, "TGVoipPlugin", "generateG_A", [convertToArray(random)]);
    },
    generateGB: function (random, successCallback, errorCallback) {
        var successCallback_ = function (result) {
            successCallback(new Uint8Array(result));
        };
        cordova.exec(successCallback_, errorCallback, "TGVoipPlugin", "generateG_B", [convertToArray(random)]);
    },
    generateFingerPrint: function (gb, aorb, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "TGVoipPlugin", "generateFingerprint", [convertToArray(gb), convertToArray(aorb)]);
    },
    test: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "TGVoipPlugin", "test", []);
    }
};
