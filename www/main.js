/*global cordova, module*/

module.exports = {
    createCall: function (params, isOutgoing, successCallback, errorCallback) {
        console.log('executed createCall!');
        cordova.exec(successCallback, errorCallback, "TGVoipPlugin", "createCall", [params, isOutgoing]);
    },
};
