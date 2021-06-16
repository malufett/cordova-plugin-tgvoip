/*global cordova, module*/

module.exports = {
    createCall: function (params, GA, isOutgoing, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "TGVoipPlugin", "createCall", [params, GA, isOutgoing]);
    },
    generateGA: function (random, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "TGVoipPlugin", "generateG_A", [random]);
    },
    generateGB: function (random, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "TGVoipPlugin", "generateG_B", [random]);
    }
};
