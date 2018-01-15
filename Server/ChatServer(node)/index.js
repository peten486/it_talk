// index.js
// writer : peten486@gmail.com
// date : 2017.09.24 
/* modify 
2017.10.01 random chat complete
2017.10.02 friend talk add....
2017.10.09 bug fix
*/


/* 
it talk socket.io 패킷 형식 
처음시작			STA
처음시작 후 대기	SWI  
발송메시지			SMG
수신메시지			RMG
랜덤 시작신청 메시지	RCS
랜덤 시작확인 		RSM
랜덤 매칭 대기		RMI 	
랜덤채팅 종료		REN
친구추가			AFM	
친구추가 후 정보전달	AFD 
친구거절			NOF	
친구추가 완료		AFS
친구삭제			RFM 
친구삭제 완료		RFO 
*/

var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);

// custom module(class?)
var client = require('./client');
var room_info = require('./room_info');
var json_type = require('./json_type');

// custom variable
var client_list = {}; 
var serial_list = [];
var room_m_list = []; // 매칭된 방
var room_u_list = []; // 매칭되지 않은 방
var json = new json_type('','');

//server start
app.get('/', function (req, res) {
  res.send('<center><H2>it talk Server started</H2></br>programmer : peten486@gmail.com</br>iTcore</center>');
});
http.listen(9999, function () {
  console.log('listening on *:9999');
  console.log('programmer : peten');
});


// socket connection state;
io.sockets.on('connection', function (socket) {
  // 처음 시작
  socket.on("STA", function(msg){
  	// 소켓 연결 후
  	// serial_list 에 추가
  	// client_list 에 추가
  	// SWI 명령 전송
    console.log( msg);
		serial_list.push(msg.serial);
		if(client_list[msg.serial] == null){
			client_list[msg.serial] = new client(socket, msg.serial, msg.name);
		}else {
			reRegistration(msg.serial, msg.name, socket);
			console.log("[err] This serial is a duplicate.");
		}
  	socket.emit("RMG", JSON.stringify(json.getJsonData(1)));
  });

  // 랜덤 채팅 시작 메시지 발신
  socket.on("RCS", function(msg){
  	// 매칭 실행
  	// 매칭 되면 RSM 명령 전송
  	matchingRandom(socket, msg.serial);
  });

  // 발송 메시지
  socket.on('SMG', function (msg) {
    console.log(msg);
  	var temp_serial = null;
  	for(var cur_index in serial_list){
      if(client_list[serial_list[cur_index]] != null){
    		if(client_list[serial_list[cur_index]].getSocket() == socket){
    			temp_serial = serial_list[cur_index];
    		}
      }
  	}
  	
  	// 현재 소켓의 시리얼이 room_m_list에 있다면 랜덤채팅,
  	// 없다면 일반 채팅
  	if(temp_serial != null){
      var partner_serial = null;
	  	if(room_m_list[temp_serial] != null){
	  		// 랜덤 채팅 
	  		partner_serial = room_m_list[temp_serial].getPartner();

        if(client_list[room_m_list[temp_serial].getPartner()] != null){
	  		 // RMG : 수신 메시지
	  		 client_list[partner_serial].getSocket().emit("RMG",msg);
        }
	  	} else {
				// 일반 채팅
				if(msg.serial != null ){
					partner_serial = msg.serial;
					if(client_list[msg.serial] != null){
						client_list[partner_serial].getSocket().emit("RMG",msg);
					} else {
							console.log("[err] client_list[partner_serial] is null");
					}
				} else {
					console.log("[err] partner_serial is null");
				}
	  	}
	  }
  });

  // 소켓 연결해제
  socket.on("disconnect", function (msg) {
  	disconnectUser(socket);
  });

  // 랜덤채팅 종료
  socket.on("REN", function (msg) {
  	endMatching(socket);
  });


  // 친구 시나리오

  // user가 상대방을 친구 추가 했을 때 메시지 박스가 뜨면서
  // 친구 추가를 하시겠습니까? (yes/no) 가 뜨고 yes를 클릭하면
  // 상대방에게 자신을 친구 추가 합니다. 승인하시겠습니까? (yes/no) 가 뜨게된다. 
  // 상대방이 yes를 클릭하면 친구추가후 정보전달이 서로 이루어지게되면서 친구 목록에 추가 된다.
  // 그리고 친구 추가 완료 메시지를 받게 된다.(안드로이드에서는 local DB로 친구들의 serial을 보관)
  

  // 친구 추가
  socket.on("AFM", function (msg) {
    // user가 상댇방을 친구 추가 했을 때 전송되는 명령 메시지
    // 받으면 상대방에게 AFM 명령 전달
    // 자신의 정보를 미리 전송
    reRegistration(msg.serial, msg.name, socket);
    transPartner(socket,1,'');
  });

  // 친구 추가 동의
  socket.on("AFD", function(msg){
    // 상대방에게 AFD 명령(자신 정보 포함, serial, nickname) 전달
    reRegistration(msg.serial, msg.name, socket);
    transPartner(socket,2,'');
  });
  
  // 친구 추가 동의 2
  socket.on("AFE", function(msg){
    // 상대방에게 AFE 명령
    transPartner(socket,7,'');
  });

  // 친구 추가 거절
  socket.on("NOF", function(msg){
    // 상대방에게 NOF 명령 전달
    transPartner(socket,3,'');
  });

  // 친구 등록 완료
  socket.on("AFS", function(msg){
    // 상대방에게 AFS 명령 전달
    transPartner(socket,4,'');
  });

  // 친구 삭제 신청
  socket.on("RFM", function(msg){
    // 상대방에게 RFM 명령 전달
    // 자신의 시리얼정보도 같이 전송
    reRegistration(msg.serial, msg.name, socket);
		transPartner(socket,5,msg);
  });

  // 친구 삭제 완료
  socket.on("RFO", function(msg){
    // 상대방에게 RFO 명령 전달 후 
    // 상대방은 RFO명령 수신후 친구가 삭제 되었습니다. 메시지 박스가 뜸
    transPartner(socket,6,msg);
  })

  // 운영자 공지사항
  socket.on("peten", function(msg){
    // 운영자 공지사항이 있는 경우 받는 디바이스에서는 알림이 나타날 수 있도록 한다.
    it_talk_operator(msg.message,'');
  });

});

// 랜덤 매칭 알고리즘 
function matchingRandom(socket, serial){
	// 실행되면 RMI 명령 전송
	socket.emit("RMG", JSON.stringify(json.getJsonData(2)));

	// 방이 있는지 확인
	// 방이 존재 하면 매칭
	// 없으면 새로 방을 생성
	// 매칭 되면 RSM 명령 전송 
	if(getUnMatchingRoomCnt() > 0){
		
		for(var cur_index in serial_list){
			if(serial_list[cur_index] != serial && serial != null){
				var room_name = serial_list[cur_index];
				room_m_list[serial] = new room_info(serial, serial_list[cur_index] ,room_name, true);
				room_m_list[serial_list[cur_index]] = new room_info(serial_list[cur_index], serial,room_name, true); 
				socket.join(room_name);

       	// room_u_list 제거
				delete room_u_list[serial_list[cur_index]];

        if(room_m_list[serial_list[cur_index]] != null && client_list[serial_list[cur_index]] != null){
				  socket.emit("RMG", JSON.stringify(json.getJsonData(3)));
				  client_list[serial_list[cur_index]].getSocket().emit("RMG", JSON.stringify(json.getJsonData(3)));
				} else {
          console.log("room_m_list[serial_list[cur_index]] is null");
        }
        break;
			}
		}

	} else {
		room_u_list[serial] = new room_info(serial,'', serial,false);
		socket.join(serial);
  }
  
}

// 랜덤 매칭 되지 않은 방 개수 
function getUnMatchingRoomCnt(){
	var cnt = Object.keys(room_u_list).length;
	return cnt;
}

// 랜덤채팅 종료
function endMatching(socket){
	// 소켓으로 client_list 에서 소켓을 찾고,
	// room_m_list에서 찾아서 matching 정보를 바꿔주고 
	// user 와 partner 의 room_m_list 의 정보를 삭제
	// 그리고 partner 에게 REN 명령 전송

	var temp_serial = null;
  	for(var cur_index in serial_list){
			if(serial_list[cur_index] == null){
				console.log('[err] disconnectUser : serial_list[cur_index] is null');
			}
			if(client_list[serial_list[cur_index]] != null){
				if (client_list[serial_list[cur_index]].getSocket() == socket) {
					temp_serial = serial_list[cur_index];
				}else{
					console.log("[err] client_list[serial_list[cur_index]].getSocket() is null");
				}
			} else {
				console.log("[err] client_list[serial_list[cur_index]] is null");
			}
  	}

  	if(temp_serial != null && room_m_list[temp_serial] != null){
	  	var partner_serial = room_m_list[temp_serial].getPartner();
	  	
      if(partner_serial != null){
        delete room_m_list[temp_serial];
	  	  delete room_m_list[partner_serial];
      }
      if(client_list[partner_serial] != null){
        client_list[partner_serial].getSocket().emit("RMG",JSON.stringify(json.getJsonData(5)));
      }
	} 
}

// 소켓 연결 해제 
function disconnectUser(socket) {
  var temp_serial = null;
  for (var cur_index in serial_list) {
		if(serial_list[cur_index] == null){
			console.log('[err] disconnectUser : serial_list[cur_index] is null');
		}
		if(client_list[serial_list[cur_index]] != null){
			if (client_list[serial_list[cur_index]].getSocket() == socket) {
      	temp_serial = serial_list[cur_index];
    	}else{
				console.log("[err] client_list[serial_list[cur_index]].getSocket() is null");
			}
		} else {
			console.log("[err] client_list[serial_list[cur_index]] is null");
		}
  }

  if (temp_serial != null) {
    delete client_list[temp_serial];
    
    if(room_m_list[temp_serial] != null){
    	delete room_m_list[temp_serial];
  	} else {
      console.log('[err] disconnectUser : room_m_list[temp_serial] is null');
    }
  	
    if (room_u_list[temp_serial] != null){
  		delete room_u_list[temp_serial];
    } else {
      console.log('[err] disconnectUser : room_u_list[temp_serial] is null');
    }
	
	
    //  계정 삭제(임시 위치입니다.) 
    var index = findIndex(temp_serial);
    serial_list.splice(index,1);
  	delete client_list[temp_serial];
  	
  	// print();

  } else {
    console.log('[err] disconnectUser : temp_serial is null');
  }
}

// 사용자 정보 재등록
function reRegistration(serial, name, socket){
  if(serial != null && client_list[serial] != null){
    delete client_list[serial];
    client_list[serial] = new client(socket, serial, name);
  }
}

// 상대방에게 명령 전달
function transPartner(socket, type, msg){

  var temp_serial = null;
  for (var cur_index in serial_list){
    if(client_list[serial_list[cur_index]].getSocket() == socket && serial_list[cur_index] != null){
      temp_serial = serial_list[cur_index];
    } else {
      console.log("[err] serial_list[cur_index] is null");
    }
  }

  
  if(temp_serial != null){
    if(room_m_list[temp_serial] != null){
      var partner_serial = room_m_list[temp_serial].getPartner();

      if(client_list[partner_serial] != null){
        
        switch(type){
          case 1:
            // type == 1 
            // 친구 추가 메시지 전달
            // 랜덤채팅 중에 친구 추가시 상대방에게 친구 전달 AFM 명령 전달

            client_list[partner_serial].getSocket().emit("RMG",JSON.stringify(json.getJsonData(6)));
            break;
          case 2:
            // type == 2
            // 친구 추가 후 정보전달 메시지 전달
            // 상대방에게 AFD 메시지와 함께 사용자의 정보(nickname, serial) 전달

            client_list[partner_serial].getSocket().emit("RMG",JSON.stringify(json.getJsonData2(7, temp_serial, client_list[temp_serial].getNickName() )));
            console.log("AFD : " + temp_serial + ", " + client_list[temp_serial].getNickName() );
            break;
          case 3:
            // type == 3
            // 친구 거절 NOF 명령 전달

            client_list[partner_serial].getSocket().emit("RMG",JSON.stringify(json.getJsonData(8)));
            break;
          case 4:
            // type == 4
            // 친구 등록 완료 AFS 명령 전달

            client_list[partner_serial].getSocket().emit("RMG",JSON.stringify(json.getJsonData(9)));
            break;
          
          case 7:
            // type == 7
            // 상대방에게 AFE 메시지전달. 정보전달

            client_list[partner_serial].getSocket().emit("RMG",JSON.stringify(json.getJsonData2(12, temp_serial, client_list[temp_serial].getNickName())));
            break;
            
        }
        
      }else {
        console.log('[err] transPartner : partner_serial is null');
      }
    } else {

		partner_serial = msg.partner_serial;
		if(partner_serial != null && client_list[partner_serial] != null){
			switch(type){
				case 5:
				// type == 5
				// 친구 삭제 신청 RFM 명령 전달
				client_list[partner_serial].getSocket().emit("RMG",JSON.stringify(json.getJsonData2(10, temp_serial, '')));
				break;
			case 6:
				// type == 6
				// 친구 삭제 완료 RFO 명령 전달

				client_list[partner_serial].getSocket().emit("RMG",JSON.stringify(json.getJsonData(11)));
				break;
			}
	}else {
		console.log("[err] partner_Serial is null");
	}
}
	}

}




// 운영자 공지사항 전달 
function it_talk_operator(msg){
  for (var cur_index in serial_list){
    if(serial_list[cur_index] != null && client_list[serial_list[cur_index]] != null){
      client_list[serial_list[cur_index]].getSocket().emit("RMG",JSON.stringify(json.getJsonData3(msg)));
    }
  }
}

// ids index 찾기
function findIndex(temp){
  var index = null;
  for (var cur_index in serial_list) {
    if (serial_list[cur_index] == temp) {
      index = cur_index;
    }
  }
  return index;
}

function print(){
  console.log('serial_list');
  console.log(serial_list);
  console.log('client_list');
  console.log(client_list);
  console.log('room_u_list');
  console.log(room_u_list);
  console.log('room_m_list');
  console.log(room_m_list);
}





