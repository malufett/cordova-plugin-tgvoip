/*global cordova, module*/

module.exports = {
    getArch: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "HelloCPlugin", "getArch", []);
    },
    hello: function (input, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "HelloCPlugin", "hello", [input]);
    },
    calculate: function (x, y, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "HelloCPlugin", "calculate", [x, y]);
    },
    causeCrash: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "HelloCPlugin", "causeCrash", []);
    },
    getTrafficStats: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "HelloCPlugin", "getTrafficStats", []);
    }
};
