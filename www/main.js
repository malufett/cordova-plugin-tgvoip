/*global cordova, module*/

module.exports = {
    createCall: function (params, GB, A_OR_B, GA, isOutgoing, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "TGVoipPlugin", "createCall", [params, GB, A_OR_B, GA, isOutgoing]);
    },
    generateGA: function (random, successCallback, errorCallback) {
        var successCallback_ = function (result) {
            result.g_a = new Uint8Array(result.g_a);
            result.g_a_hash = new Uint8Array(result.g_a_hash);
            result.a_or_b = new Uint8Array(result.a_or_b);
            successCallback(result);
        };
        cordova.exec(successCallback_, errorCallback, "TGVoipPlugin", "generateG_A", [random]);
    },
    generateGB: function (random, successCallback, errorCallback) {
        var successCallback_ = function (result) {
            successCallback(new Uint8Array(result));
        };
        cordova.exec(successCallback_, errorCallback, "TGVoipPlugin", "generateG_B", [random]);
    },
    generateFingerPrint: function (gb, aorb, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "TGVoipPlugin", "generateFingerprint", [gb, aorb]);
    }
};
