/*global cordova, module*/

module.exports = {
    getArch: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "TGVOIPPlugin", "getArch", []);
    },
    hello: function (input, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "TGVOIPPlugin", "hello", [input]);
    },
    calculate: function (x, y, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "TGVOIPPlugin", "calculate", [x, y]);
    },
    causeCrash: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "TGVOIPPlugin", "causeCrash", []);
    }
};
