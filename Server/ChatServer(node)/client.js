// client.js
// writer : peten486@gmail.com
// date : 2017.09.24 

var client = function(socket, serial_no, nickname ){
    this.socket = socket;
    this.serial_no = serial_no;
    this.nickname = nickname;
};

client.prototype = {
    getNickName:function(){
        return this.nickname;
    },
    getSocket:function(){
        return this.socket;
    },
    getSerialNo:function(){
        return this.serial_no;
    }
};

module.exports = client;