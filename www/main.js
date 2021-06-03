/*global cordova, module*/

module.exports = {
    test: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "TGVoipPlugin", "test", []);
    },
};
