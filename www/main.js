/*global cordova, module*/

module.exports = {
    createCall: function (params, GA, isOutgoing, successCallback, errorCallback) {
        console.log('executed createCall!');
        cordova.exec(successCallback, errorCallback, "TGVoipPlugin", "createCall", [params, GA, isOutgoing]);
    },
};
