package ChatServer;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * // 실행 방법 // java MultichatGUIClient 대화명
 * 
 * @since 2012. 07. 11.
 * @author yanggun7201
 */
public class MultichatGUIClient implements ActionListener 
{
	Frame f;
	Panel p;
	Button b1;
	Button b2;
	Button b3;
	TextField tf;
	TextArea ta;
	// ===============================
	String name;				//사용자 이름
	String userId;				//사용자 ID
	String partnerId;			//상대방 ID
	String partnerName;			//상대방 이름
	boolean isMatch;	//매칭상태
	ClientSender sender;
	Socket socket;
	Thread receiver;
	// ===============================

	public MultichatGUIClient(String name, String userId) {
		// ===============================
		this.name = name;
		this.userId = userId;
		
		f = new Frame(name);
		// ===============================

		p = new Panel();
		b1 = new Button("전송");
		b2 = new Button("종료");
		b3 = new Button("새로고침");
		tf = new TextField();
		ta = new TextArea(20, 50);
		isMatch = false;
		
	}

	public void launchTest() 
	{
		f.addWindowListener(new WindowAdapter() 
		{
			public void windowClosing(WindowEvent e) 
			{
				System.exit(0);
			}
		});

		// =============================================UI
		// frame은 기본적으로 BorderLayout이다.
		f.add(BorderLayout.SOUTH, p);

		b1.addActionListener(this);
	//	b1.setBackground(Color.yellow);
		
		b2.addActionListener(this);
		b3.addActionListener(this);
	//	b2.setBackground(Color.yellow);

		tf.setColumns(40);
		tf.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				char keyCode = e.getKeyChar();
				if (keyCode == KeyEvent.VK_ENTER) {
					//actionPerformed(null);

					MultichatGUIClient.this.setMessage(name + " : " + tf.getText());
					sender.send(tf.getText());
					// =========================
					tf.setText("");
					tf.requestFocus();
				}
			}
		});

		p.setBackground(Color.green);
		//p.add(b3);
		p.add(tf);
		p.add(b1);
		p.add(b2);

		f.add(BorderLayout.CENTER, ta);

		f.setVisible(true);
		f.pack();
		//=================================================UI 끝
		
		// =================================================
		socket = null;
		try {
			String serverIp = "doyouknowpeten.xyz";
			socket = new Socket("localhost", 9999); // 소켓을 생성하여 연결을 요청한다.
			System.out.println("서버에 연결되었습니다.");

			// 메시지 전송용 Thread 생성
			sender = new ClientSender(socket);

			// 메시지 수신용 Thread 생성
			receiver = new Thread(new ClientReceiver(socket));

			receiver.start();
		} 
		catch (ConnectException ce) 
		{
			ce.printStackTrace();
		} 
		catch (Exception e) 
		{
			
		}
		// =================================================
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// =========================
		if(e.getSource() == b1){
			sender.send(tf.getText());
			// =========================
			MultichatGUIClient.this.setMessage(name + " : " + tf.getText());
			tf.setText("");
			tf.requestFocus();
		}
		else if(e.getSource() == b2){
			sender.stop();
		}
		
	}

	// =================================================
	public void setMessage(String message) {
		ta.append(message);
		ta.append("\n");
	}
	// =================================================

	
	// ===============================================================================
	// 메시지 전송용 객체
	// Thread 아님!!! (GUI 버전이므로, Event를 받아서 처리함)
	class ClientSender 
	{
		Socket socket;
		DataOutputStream out;

		ClientSender(Socket socket) 
		{
			this.socket = socket;

			try 
			{
				this.out = new DataOutputStream(socket.getOutputStream());
				// 시작하자 마자, 자신의 ID및 대화명 전송
				if (out != null) 
				{
					String startMessage;
					startMessage = "CONN/" + name + "/" + userId;
					out.writeUTF(startMessage);
					startMessage = "START/" + name + "/" + userId;
					out.writeUTF(startMessage);
				}

			} 
			catch (Exception e) 
			{
				
			}
		}

		public void send(String message) 
		{
			if (out != null) 
			{
				try 
				{
					// 키보드로 입력받은 데이터를 서버로 전송
					// 매칭된 상태에서만 메세지 전송
					if(isMatch)
					{
						message = "SENDMSG/" + partnerId + "/" + message;
						out.writeUTF(message);
					}
				} 
				catch (IOException e) 
				{
					
				}
			}
		}
		
		public void stop(){
			if (out != null) 
			{
				try 
				{
					if(isMatch)
					{
						String temp = "CLEMSG/" + partnerId ;
						out.writeUTF(temp);
						out.close();
						MultichatGUIClient.this.setMessage(partnerName + "님과 대화가 종료되었습니다.");
					}
				} 
				catch (IOException e) 
				{
					
				}
			}
		}
	}

	// 메시지 수신용 Thread
	class ClientReceiver implements Runnable 
	{
		Socket socket;
		DataInputStream in;

		// 생성자
		ClientReceiver(Socket socket) {
			this.socket = socket;

			try {
				// 서버로 부터 데이터를 받을 수 있도록 DataInputStream 생성
				this.in = new DataInputStream(socket.getInputStream());
			} catch (IOException e) {
			}
		}

		public void run() 
		{
			String reciveMsg = null;
			while (in != null) 
			{
				try 
				{
					// 서버로 부터 전송되는 데이터를 출력
					reciveMsg = in.readUTF();
					MessageGrup(reciveMsg);
					//MultichatGUIClient.this.setMessage(reciveMsg);
				} 
				catch (IOException e) 
				{
					
				}
			}
		}
		
		public void MessageGrup(String message)
		{
			String[] msgList;
			
			msgList = message.split("/");
			String classify = msgList[0];
			
			//매칭이 성공해서 시작
			if(classify.equals("START"))
			{
				partnerId = msgList[1];
				partnerName = msgList[2];
				isMatch = true;
				MultichatGUIClient.this.setMessage(partnerName + "님과 대화가 시작되었습니다.");
			}
			//메세지 
			else if(classify.equals("MSG"))
			{
				MultichatGUIClient.this.setMessage(partnerName + " : " + msgList[1]);
			}
			//상대방 종료 알림
			else if(classify.equals("END"))
			{
				isMatch = false;
				MultichatGUIClient.this.setMessage(partnerName + "님과 대화가 종료되었습니다.");
			}
		}
	}

	// ===============================================================================

	// 실행 방법
	// java MultichatGUIClient 대화명
	public static void main(String[] args) 
	{
		//닉네임
		String name = null;
		//디바이스 넘버
		String userID = null;
		
		Scanner scanner = new Scanner(System.in);

		do {
			System.out.println("대화명을 입력하세요.");
			System.out.print(">>> ");
			name = scanner.nextLine();
			
			System.out.println("ID를 입력하세요.");
			System.out.print(">>> ");
			userID = scanner.nextLine();
			
			if (name.isEmpty()) {
				System.out.println("대화명은 한글자 이상 입력해야 합니다.\n\n");
			}
		} while (name.isEmpty());

		new MultichatGUIClient(name, userID).launchTest();
	}
}