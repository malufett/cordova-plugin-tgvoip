/*global cordova, module*/

module.exports = {
    test: function (params, successCallback, errorCallback) {
        console.log('executed test!');
        cordova.exec(successCallback, errorCallback, "TGVoipPlugin", "test", [params]);
    },
};
