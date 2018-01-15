package ChatServer;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

import java.util.Date;
import ChatServer.SocketUtil;



public class MultichatServer 
{
	//Client 정보를 저장하기 위한 class
	public class ClientInfo
	{
		private String userId;
		private DataOutputStream outStream;
		private boolean isMatching;
		private String partnerId;
		
		ClientInfo(String id, DataOutputStream outstream)
		{
			userId = id;
			outStream = outstream;
			isMatching = false;
		}
		
		//getter
		DataOutputStream getStream() {return outStream;}
		String getUserId() {return userId;}
		String getPartnerId() {return partnerId;}
		boolean getMatching() {return isMatching;}
		
		//setter
		void setMatching(boolean is) {isMatching = is;}
		void setPartner(String pId) {partnerId = pId; }
	}

	// 전역변수
	// user id, 클라이언트 OutputStream 저장용 대화방(HashMap) 정의
	Map<String, ClientInfo> clients;

	// 생성자
	MultichatServer() {
		clients = Collections.synchronizedMap( //
				new HashMap<String, ClientInfo>());
	}

	// 비즈니스 로직을 처리하는 메서드
	public void start() 
	{
		ServerSocket serverSocket = null;
		Socket socket = null;

		try {
			// 9999 포트에 바인딩된 서버 소켓 생성
			serverSocket = new ServerSocket(9999);
			System.out.println("서버가 시작되었습니다.");

			while (true) 
			{
				// 클라이언트 접속 대기 accept()
				socket = serverSocket.accept();
				System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "]" + "에서 접속하였습니다.");

				// 서버에서 클라이언트로 메시지를 전송할 Thread 생성
				ServerReceiver thread = new ServerReceiver(socket);
				// 스레드 실행
				thread.start();

			} // while

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			SocketUtil.close(serverSocket);
		}
	} // start()

	// 대화방에 있는 전체 유저에게 메시지 전송
	// 현재 모든 친구들한테 전송
	void sendToAll(String msg) 
	{
		// 대화방에 접속한 유저의 대화명 리스트 추출
		Iterator<String> it = clients.keySet().iterator();

		while (it.hasNext()) 
		{
			try 
			{
				String id = it.next();
				DataOutputStream out = clients.get(id).getStream();
				out.writeUTF(msg);
			} 
			catch (IOException e) 
			{
				System.out.print("Error : sendToAll");
				System.out.println(e.toString());
			}
		} // while
	} // sendToAll

	void sendToUser(String id, String msg)
	{
		try
		{
			DataOutputStream out = clients.get(id).getStream();
			out.writeUTF(msg);
		}
		catch (IOException e) 
		{
			System.out.println("Error : sendToUser");
			System.out.println(e.toString());
		}
	}
	
	
	//main
	public static void main(String[] args) {
		new MultichatServer().start();
	}

	// Inner Class로 정의 하여, 대화방 field에 접근 할 수 있도록 한다.
	// 서버에서 클라이언트로 메시지를 전송할 Thread
	class ServerReceiver extends Thread 
	{
		Socket socket;
		String myId;
		DataInputStream in;
		DataOutputStream out;

		ServerReceiver(Socket socket) {
			this.socket = socket;
			try {
				// 클라이언트 소켓에서 데이터를 수신받기 위한 InputStream 생성
				in = new DataInputStream(socket.getInputStream());

				// 클라이언트 소켓에서 데이터를 전송하기 위한 OutputStream 생성
				out = new DataOutputStream(socket.getOutputStream());
			} catch (IOException e) {
			}
		}

		public void run() 
		{
			String reciveMessage = "";
			try {
				// 서버에서는 최초에 클라이언트가 보낸 대화명을 받아야 한다.
				 reciveMessage = in.readUTF();

				// 대화명을 받아, 전에 클라이언트에게 대화방 참여 메시지를 보낸다.
//				sendToAll("#" + myId + "님이 들어오셨습니다.");

				// 대화명, 클라이언로 메시지를 보낼 수 있는 OutputStream 객체를
				// 대화방 Map에 저장한다.
				messageGroup(reciveMessage);
				
//				clients.put(name, out);
				System.out.println("현재 서버접속자 수는 " + clients.size() + "입니다.");

				// 클라이언트가 전송한 메시지를 받아, 전에 클라이언트에게 메시지를 보낸다.
				while (in != null) 
				{
					//sendToAll(in.readUTF());
					//sendToUser("qwe", in.readUTF());
					reciveMessage = in.readUTF();
					messageGroup(reciveMessage);
				} // while

			} 
			catch (IOException e) 
			{
				// ignore
			} 
			finally 
			{
				// finally절이 실행된다는 것은 클라이언트가 빠져나간 것을 의미한다.
				//sendToAll("#" + name + "님이 나가셨습니다.");

				// 대화방에서 객체 삭제
				System.out.println("exit : " + clients.get(myId));
				String tempParterId = clients.get(myId).getPartnerId();
				if(clients.containsKey(tempParterId))
				{
					System.out.println("상대방에게 접속 종료 알림");
					sendToUser(tempParterId, "END");
					clients.get(tempParterId).setMatching(false);
				}
				
//				System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "]" + "에서 접속을 종료하였습니다.");
				clients.remove(myId);
				System.out.println("현재 서버접속자 수는 " + clients.size() + "입니다.");
			} // try
		} // run
		
		public void messageGroup(String msg)
		{
			String[] msgList;
			System.out.println("message : " + msg);
			
			msgList = msg.split("/");
			
			// msg 형식
			// 첫번째 : 패킷 형식 코드
			// 두번째 : user id
			// 세번째 : partner id
			// 네번째 : 메시지 
			
			String classify = msgList[0];
			
			ClientInfo tempClient;
			tempClient = new ClientInfo(msgList[1], out);
			// MainActivity 클라이언트 시작시 받는 메세지
			if(classify.equals("SAT")){
				// 처음 시작 후 유저 등록 후 대기메시지 전
				
				clients.put(tempClient.getUserId(), tempClient);
				sendToUser(msgList[1], "WSE/" + msgList[1]);
				
			}
			else if(classify.equals("RCS"))
			{
				
				if(isClients(tempClient.getUserId())){
				
					myId = msgList[2];
					
					//매칭 알고리즘
					Iterator<String> it = clients.keySet().iterator();
	
					while (it.hasNext()) 
					{
						String id = it.next();
						System.out.println("while\n id : " + id + " and myId : " + myId);
						System.out.println("while\n id : " + id + " and matching : " + clients.get(id).getMatching());
						//매칭 성공
						if(!clients.get(id).getMatching() && id != myId)
						{
							System.out.println("Mactching " + clients.get(myId) + " - " + clients.get(id) );
							//client partner setting
							
							// 랜덤채팅 메시지 전달 
							sendToUser(id, "CSM/" + clients.get(myId).getUserId() );
							sendToUser(myId, "CSM/" + clients.get(id).getUserId() );
							
							//server partner setting
							clients.get(id).setPartner(clients.get(myId).getUserId());
							clients.get(myId).setPartner(clients.get(id).getUserId());
							clients.get(id).setMatching(true);
							clients.get(myId).setMatching(true);
							break;
						}
					} // while
				}
				
			}
			//메세지 전송 요청 처리
			else if(classify.equals("SMG"))
			{
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy:MM:dd-hh:mm:ss");
				String datetime1 = sdf1.format(cal.getTime());
				

				sendToUser(msgList[2], "RMG/" + msgList[1] +"/" + msgList[3] + "/" + datetime1);
			}
			// end chat
			else if(classify.equals("CLEMSG")){
				// 대화방에서 객체 삭제
				System.out.println("exit "+ clients.get(myId) );
				String tempParterId = clients.get(myId).getPartnerId();
				if(clients.containsKey(tempParterId))
				{
					System.out.println("상대방에게 접속 종료 알림");
					sendToUser(tempParterId, "END");
					clients.get(tempParterId).setMatching(false);
				}
				
//				System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "]" + "에서 접속을 종료하였습니다.");
				clients.remove(myId);
				System.out.println("현재 서버접속자 수는 " + clients.size() + "입니다.");
			}
			
		}
	} // ReceiverThread
	
	
	public boolean isClients(String id){
		if(clients.containsKey(id)){
			return true; 
		}
		return false;
	}
	
	
} // class
