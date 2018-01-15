// json_type.js
// writer : peten486@gmail.com
// date : 2017.10.02



var json_type = function(){
};

json_type.prototype = {
    getJsonData3:function(msg){
      var json;
      // 운영자 메시지 전달 
      json = {
         str : msg,
         type : 2,
         message : 'peten'
      }
      return json;
    },
    getJsonData2:function(number, serial, name){
      var json;
      if (number == 7){
            // 친구 추가 명령 전달
            json = {
                partner_name : name,
                partner_serial : serial,
                type : 2,
                message : 'AFD'
            }
        } else if (number == 10) {
            // 친구 삭제 신청
            json = {
              type : 2,
               message : 'RFM',
               partner_serial : serial
            }
        } else if (number == 12){
            // 친구 추가 명령 전달
            json = {
                partner_name : name,
                partner_serial : serial,
                type : 2,
                message : 'AFE'
            }
        }
      return json;
    },
    getJsonData:function(number){
    	var json;
        if(number == 1){
        	// 처음 시작 후 대기 
            json = {
            	type : 2,
            	message : 'SWI'
            }
        } else if (number == 2){
        	// 랜덤 매칭 대기 
        	json = {
            	type : 2,
            	message : 'RMI'
            }
        } else if (number == 3){
        	// 랜덤 매칭 완료
        	json = {
            	type : 2,
            	message : 'RSM'
            }
        } else if (number == 4){
            // 랜덤 채팅 종료
            json = {
                type : 2,
                message : 'REN'
            }
        }else if (number == 5){
            // 랜덤 채팅 종료 상대방에게 전달
            json = {
                type : 2,
                message : 'RER'
            }
        }else if (number == 6){
            // 친구 추가 명령 전달
            json = {
                type : 2,
                message : 'AFM'
            }
        }else if (number == 8){
            // 친구 추가 거절 전달
            json = {
                type : 2,
                message : 'NOF'
            }
        } else if (number == 9) {
            // 친구 추가 완료
            json = {
              type : 2,
               message : 'AFS'
            }
        } else if (number == 11) {
            // 친구 삭제 완료
            json = {
              type : 2,
               message : 'RFO'
            }
        }


        return json;
    }
};

module.exports = json_type;