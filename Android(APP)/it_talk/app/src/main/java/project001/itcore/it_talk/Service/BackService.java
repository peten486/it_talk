package project001.itcore.it_talk.Service;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import project001.itcore.it_talk.R;
import project001.itcore.it_talk.activity.ChatActivity;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import project001.itcore.it_talk.adapter.DBManager;
import project001.itcore.it_talk.model.ChatMessage;
import project001.itcore.it_talk.model.ChatUser;
import project001.itcore.it_talk.model.SocketConnection;

public class BackService extends Service {
    public BackService() {
    }
    
    final static String TAG = "BackService";

    //from BackService to ChatActivity
    public final static String ACTION_MSG_TO_SERVICE = "MSG_TO_SERVICE";
    public final static String KEY_PARTNER_NAME_TO_SERVICE = "KEY_PARTNER_NAME_TO_SERVICE";
    public final static String KEY_PARTNER_ID_TO_SERVICE = "KEY_PARTNER_ID_TO_SERVICE";

    // from MainActivity to BackService
    public final static String KEY_INT_FROM_SERVICE = "KEY_INT_FROM_SERVICE";
    public final static String KEY_STRING_FROM_SERVICE = "KEY_STRING_FROM_SERVICE";
    public final static String ACTION_UPDATE_CNT = "UPDATE_CNT";
    public final static String ACTION_UPDATE_MSG = "UPDATE_MSG";


    // notificatoin 변수
    private NotificationManager mNM;
    private Notification.Builder mBuilder;
    Intent push;
    PendingIntent fullScreenPendingIntent;


    private Socket mSocket;
    ChatUser user, other;
    boolean socket_Connect;

    final DBManager dbManager = new DBManager(BackService.this, "chat.db", null, 1);


    //Handler
    receiveHandler handler;
    String tempsss;
    BackServiceReceiver backServiceReceiver;
 //   BackServiceThread backServiceThread;


    int cnt;

    int s_id;



    @Override
    public void onCreate(){
        Log.d(TAG,"onCreate()");
        handler = new receiveHandler();
        socket_Connect = false;
        backServiceReceiver = new BackServiceReceiver();
        super.onCreate();

        tempsss = null;

    }

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"onBind");

        /*Log.d(TAG,"[intent] id : "+ intent.getExtras().getString("id"));
        Log.d(TAG,"[intent] nickName : "+ intent.getExtras().getString("nickName"));

        String id, name;
        id = intent.getExtras().getString("id");
        name = intent.getExtras().getString("nickName");
        user = new ChatUser(id,name);*/
        return null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand");
        startForeground(startId, new Notification());

        //Notifi_M = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //myServiceHandler handler = new myServiceHandler();
        //thread.start();

        Log.d(TAG,"[intent] id : "+ intent.getExtras().getString("id"));
        Log.d(TAG,"[intent] nickName : "+ intent.getExtras().getString("nickName"));

        String id, name;
        id = intent.getExtras().getString("id");
        name = intent.getExtras().getString("nickName");

        user = new ChatUser(id,name);
        other = new ChatUser();
        other.setId(other.getId()+"ittalk");

        Log.d(TAG,"[intent] partner_id : "+ intent.getExtras().getString("partner_id"));
        Log.d(TAG,"[intent] partner_nickname : "+ intent.getExtras().getString("partner_nickname"));



        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_MSG_TO_SERVICE);
        registerReceiver(backServiceReceiver, intentFilter);

        //String ACTIVITY_NAME = intent.getExtras().getString("ACTIVITY_NAME");

        // notification setup
        // head up noti
        mBuilder = new Notification.Builder(this);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon ));
        mBuilder.setSmallIcon(R.drawable.icon_n);
        mBuilder.setTicker("알람 간단한 설명");
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        mBuilder.setContentIntent(fullScreenPendingIntent);
        mBuilder.setAutoCancel(true);

        mNM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        start_it_talk();


     //   mNM.notify(startId, notification);
     //   mNM.cancel(startId);


        //printForegroundTask();

        ActivityManager manager = (ActivityManager) BackService.this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> task = manager.getRunningAppProcesses();
        ActivityManager.RunningAppProcessInfo runningAppProcessInfo = null;

        for(ActivityManager.RunningAppProcessInfo singleTask : task){
            if(singleTask.importanceReasonComponent !=null){
                runningAppProcessInfo = singleTask;
                break;
            }
        }

        if(runningAppProcessInfo!=null) {
            ComponentName componentInfo = runningAppProcessInfo.importanceReasonComponent;
            String packageName = componentInfo.getPackageName();
            Log.d("myPackageName", packageName);
        }

        //Log.e(TAG, "Current Activity in foreground is: " + packageName);


        if(mSocket.connected() == true)
        {
            socket_Connect = true;
        }

        s_id = startId;
        //return START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }




    //서비스가 종료될 때 할 작업

    public void onDestroy() {
        //thread.stopForever();
        //thread = null;//쓰레기 값을 만들어서 빠르게 회수하라고 null을 넣어줌.
 //       backServiceThread.setRunning(false);
        unregisterReceiver(backServiceReceiver);
        exit_it_talk();
    }


    private void start_it_talk(){
        mSocket = SocketConnection.getInstance().getSocket();
        mSocket.on("RMG", onNewMessage);
        mSocket.on(Socket.EVENT_CONNECT, onConnectServer);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.connect();
        Log.d(TAG,"start_it_talk");

    }



    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            BackService.this.runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    JSONObject data = null;
                    try {
                        data = (JSONObject) args[0];
                    }catch (ClassCastException e){
                        String data2 = (String) args[0];
                        try {
                            data = new JSONObject(data2);
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }

                    Log.d(TAG, "[RMG(Emitter)] : " + data);

                    String message = null;
                    try {
                        message = data.getString("message");
                        int type = data.getInt("type");
                        //String name = data.getString("name");

                        if(type == 1){
                            // 상대방 메시지의 경우
                            // random chat이 아닐 경우여야 함.
                            // partner_serial 의 데이터가 나오는 경우는 일반 채팅임.
                            String temp = null;

                            if(data.getString("partner_serial") == null){
                                //temp = "";
                            } else {
                                temp = data.getString("partner_serial");
                            }

                            if(!temp.equals("") && temp != null) {
                                Log.d(TAG, "[receiver] partner_serial : " + data.getString("partner_serial"));

                                ChatUser temp_user = new ChatUser(temp, dbManager.getPartnerNickname(temp));

                                Log.d(TAG,"[receiver] other_id : "+ other.getId() );
                                Log.d(TAG,"[receiver] other_nickname : "+ other.getNickName() );

                                // 현재 액티비티가 ChatActivity이면 getPartner()를 했을때
                                // 나오는 값과 들어오는 메시지의 유저랑 같으면 알림을 하지 않는다.

       //                         Log.d(TAG,"[sdf] temp_user id : " + temp_user.getId());

                                if(!other.getId().equals(temp_user.getId()) ){
                                    Log.d(TAG,"[dd] other id : "+other.getId());
                                    Log.d(TAG,"[dd] partner id : "+temp_user.getId());
                                    noti(temp_user, message);
                                }

                                ChatMessage temp_msg = new ChatMessage(user.getId(), "", message, getTime() , type);
                                //msgList.add(temp);

                                dbManager.insert_msg(temp_msg);

                                // ChatActivity 로 전달
                            }
                        } else if(type == 2) {
                            if (message.equals("SWI")) {
                                // 대기 상태
                                return;
                            } else if (message.equals("RFM")) {
                                // 친구 삭제
                                // 현재 채팅기록, 유저목록에서 삭제.
                                // 상대방에게 RFO 명령 전달
    /*
                                    ChatUser delUser = new ChatUser(data.getString("partner_serial"), "");
                                    dbManager.delete_user(delUser); */
                                Toast.makeText(getApplicationContext(), "친구삭제신청", Toast.LENGTH_SHORT).show();
                                okRemoveFriend();
                                return;
                            } else if (message.equals("RFO")) {
                                // 친구 삭제 완료
                                Toast.makeText(getApplicationContext(), "친구삭제완료", Toast.LENGTH_SHORT).show();

                            } else {

                                // Log.d(TAG, "[RMG(Emitter)] partner_serial : " + data.getString("partner_serial"));
                                Log.d(TAG, "etc msg : " + data );
                            }
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    };



    private Emitter.Listener onConnectServer = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            if (socket_Connect == false) {
                Log.d(TAG, "socket connect Server");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Log.d(TAG, "here 1");

                        //mSocket = SocketConnection.getInstance().getSocket();


                        // 연결 될 경우에는 서버에 STA 명령을 전송
                        JSONObject joinMessage = new JSONObject();
                        try {
                            joinMessage.put("message", "STA");
                            joinMessage.put("type", 2);
                            joinMessage.put("name", user.getNickName());
                            joinMessage.put("serial", user.getId());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        mSocket.emit("STA", joinMessage);

                        Log.d(TAG, "[STA] " + joinMessage);
                        socket_Connect = true;

                    }


                });
            }

        }
    };


    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            Log.d(TAG,"socket connect error");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (mSocket == null) {
                        if (mSocket.connected() == false) {
                            mSocket = SocketConnection.getInstance().getSocket();

                        }
                    } else {
                       // Toast.makeText(BackService.this, getString(R.string.message_disconnect), Toast.LENGTH_SHORT).show();
                        socket_Connect = false;
                    }
                }
            });


        }
    };




    private void noti(ChatUser partner, String msg){
        // intent 를 추가함으로써 헤드업 알림이 가능
        push = new Intent(this, ChatActivity.class);

        push.putExtra("type", 1);
        push.putExtra("id", user.getId());
        push.putExtra("nickName", user.getNickName());
        push.putExtra("partner_id", partner.getId());
        push.putExtra("partner_nickname", partner.getNickName() );

        //push.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
       push.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // only one chat activity

        fullScreenPendingIntent = PendingIntent.getActivity(this, 0, push, PendingIntent.FLAG_ONE_SHOT);
        mBuilder.setFullScreenIntent(fullScreenPendingIntent, true);


        mBuilder.setContentTitle(partner.getNickName());
        mBuilder.setContentText(msg);

        Random random = new Random();
        int temp_id = random.nextInt(99999);

        mNM.notify(s_id , mBuilder.build());
        mNM.cancel(s_id);

        ChatMessage temp = new ChatMessage(user.getId(), partner.getId(), msg, getTime(), 1);
        dbManager.insert_msg(temp);

    }


    private String getTime(){
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy h:mm a");
        String timeText = outputFormat.format(new Date());
        return timeText;
    }



    // socket 종료
    private void exit_it_talk(){
        JSONObject leaveMessage = new JSONObject();
        try {
            leaveMessage.put("message","");
            leaveMessage.put("type",2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("REN",leaveMessage);

        Log.d(TAG, "[REN(exit)] : " + leaveMessage);

        SocketConnection.getInstance().disconnect();
    }


    public void okRemoveFriend(){
        // 친구 삭제 RFO 전달
        JSONObject message = new JSONObject();
        try {
            // 자신과 상대방의 serial을 같이 보냄
            message.put("name", user.getNickName());
            message.put("serial", user.getId());
            message.put("type", 2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("RFO",message);

        Log.d(TAG, "[RFO] : " + message);

    }


    class receiveHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {

        }
    };


    public class BackServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if(action.equals(ACTION_MSG_TO_SERVICE)){
                String id = intent.getStringExtra(KEY_PARTNER_ID_TO_SERVICE);
                String name = intent.getStringExtra(KEY_PARTNER_NAME_TO_SERVICE);
                Log.d(TAG,"KEY_PARTNER_ID_TO_SERVICE : "+ id);
                Log.d(TAG,"KEY_PARTNER_NAME_TO_SERVICE : "+ name);

                other = new ChatUser(id, name);

                /*
                //send back to MainActivity
                Intent i = new Intent();
                i.setAction(ACTION_UPDATE_MSG);
                i.putExtra(KEY_STRING_FROM_SERVICE, msg);
                sendBroadcast(i);*/
            }
        }
    }
/*
    private class BackServiceThread extends Thread{

        private boolean running;

        public void setRunning(boolean running){
            this.running = running;
        }

        @Override
        public void run() {
            cnt = 0;
            running = true;
            while (running){
                try {
                    Thread.sleep(1000);

                    Intent intent = new Intent();
                    intent.setAction(ACTION_UPDATE_CNT);
                    intent.putExtra(KEY_INT_FROM_SERVICE, cnt);
                    sendBroadcast(intent);

                    cnt++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }*/



}
