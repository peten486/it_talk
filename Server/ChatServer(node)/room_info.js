// room_info.js
// writer : peten486@gmail.com
// date : 2017.09.24 

var room_info = function( user, partner,  roomName, match ){
    this.user = user; // 
    this.partner = partner; // 
    this.roomName = roomName;
    this.match = match;
};

room_info.prototype = {
    getUser : function(){
        return this.user;
    },
    getPartner : function(){
        return this.partner;
    },
    getRoomName : function(){
        return this.roomName;
    },
    setPartner:function(partner){
        this.partner = partner;
    },
    getMatching:function(){
        return this.match;
    },
    Matching:function(){
        if(this.match == true){
            this.match = false;
        }
        else{
            this.match = true;
        }
    }
};

module.exports = room_info;