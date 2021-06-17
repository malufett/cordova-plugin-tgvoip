/*global cordova, module*/

module.exports = {
    createCall: function (params, GA, isOutgoing, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "TGVoipPlugin", "createCall", [params, GA, isOutgoing]);
    },
    generateGA: function (random, successCallback, errorCallback) {
        successCallback = function (result) {
            result.g_a = new Uint8Array(result.g_a);
            result.g_a_hash = new Uint8Array(result.g_a_hash);
            result.a_or_b = new Uint8Array(result.a_or_b);
            successCallback(result);
        };
        cordova.exec(successCallback, errorCallback, "TGVoipPlugin", "generateG_A", [random]);
    },
    generateGB: function (random, successCallback, errorCallback) {
        successCallback = function (result) {
            successCallback(new Uint8Array(result));
        };
        cordova.exec(successCallback, errorCallback, "TGVoipPlugin", "generateG_B", [random]);
    },
    generateFingerPrint: function (gb, aorb, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "TGVoipPlugin", "generateFingerprint", [gb, aorb]);
    }
};
