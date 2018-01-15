package project001.itcore.it_talk.activity;



import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


import project001.itcore.it_talk.R;
import project001.itcore.it_talk.Service.BackService;
import project001.itcore.it_talk.adapter.ChatAdapter;
import project001.itcore.it_talk.adapter.DBManager;
import project001.itcore.it_talk.model.ChatMessage;
import project001.itcore.it_talk.model.ChatUser;
import project001.itcore.it_talk.model.SocketConnection;
import project001.itcore.it_talk.model.Talk_Date;

/**
 * Created by peten on 2017. 7. 9..
 */



public class ChatActivity extends AppCompatActivity {
    @Bind(R.id.toolbar_title)
    TextView toolbarTitle;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.rv_chat)
    RecyclerView rvChat;
    @Bind(R.id.edit_text)
    EditText editText;
    @Bind(R.id.iv_send)
    ImageView ivSend;
    @Bind(R.id.relativeLayout)
    RelativeLayout relativeLayout;
    @Bind(R.id.rl_main)
    RelativeLayout rlMain;


    final static String TAG = "ChatActivity";
    
    ArrayList<String> mMsg;
    ArrayList<String> mTime;

    ArrayList<ChatMessage> msgList;

    private ChatAdapter adapter;
    private Socket mSocket;

    private int chat_type;
    private ChatUser user;
    private ChatUser partner;
    Typeface font; // 폰트

    private AlertDialog dialog;

    final DBManager dbManager = new DBManager(ChatActivity.this, "chat.db", null, 1);

    private Menu m_menu = null;

    private boolean cur_activity = false;

    private boolean chat_active = false;


    // Receiver
    @Override
    protected void onPause(){
        super.onPause();

        // 액티비티가 Background로 숨는 경우에 호출
        Log.d(TAG,"onPause");
        Intent backIntent = new Intent();
        backIntent.setAction(BackService.ACTION_MSG_TO_SERVICE);
        backIntent.putExtra(BackService.KEY_PARTNER_ID_TO_SERVICE, "");
        backIntent.putExtra(BackService.KEY_PARTNER_NAME_TO_SERVICE, "");
        sendBroadcast(backIntent);

        cur_activity = false;

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // toolbar
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        initToolbar();
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);


        //toolbarTitle.setText("낯선사람");
        initFont();
        setFont(toolbarTitle);

        mMsg = new ArrayList<>();
        mTime = new ArrayList<>();
        adapter = new ChatAdapter(ChatActivity.this, mMsg, mTime);
        LinearLayoutManager lm = new LinearLayoutManager(ChatActivity.this);
        lm.setStackFromEnd(true);
        rvChat.setLayoutManager(lm);
        rvChat.setAdapter(adapter);
        rvChat.scrollToPosition(rvChat.getAdapter().getItemCount() - 1);

    }

    @Override
    protected void onResume(){

        Log.d(TAG,"onResume");
        super.onResume();
        //cur_activity = true;
        msgList = new ArrayList<ChatMessage>();

        Intent intent = getIntent();

        Log.d(TAG,"[intent] id : "+ intent.getExtras().getString("id"));
        Log.d(TAG,"[intent] nickName : "+ intent.getExtras().getString("nickName"));

        user = new ChatUser(intent.getExtras().getString("id"), intent.getExtras().getString("nickName"));
        chat_type = intent.getExtras().getInt("type");

        if(chat_type == 0) {
            Log.d(TAG,"random start ");
            if(chat_active == false) {
                start_Random();
                partner = null;
                msgList = new ArrayList<ChatMessage>();
             //   cur_activity = true;
            }
        } else {
           // cur_activity = true;
            Log.d(TAG,"[intent] partner_id : "+ intent.getExtras().getString("partner_id"));
            Log.d(TAG,"[intent] partner_nickname : "+ intent.getExtras().getString("partner_nickname"));
            partner = new ChatUser(intent.getExtras().getString("partner_id"), intent.getExtras().getString("partner_nickname"));
            toolbarTitle.setText(partner.getNickName());
            start_talk();

            // 채팅 history 초기화 후 화면 출력
            initMsg();

        }

        Intent backIntent = new Intent();
        backIntent.setAction(BackService.ACTION_MSG_TO_SERVICE);
        backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);

        if(partner != null && partner.getId() != null && partner.getNickName() != null ) {
            backIntent.putExtra(BackService.KEY_PARTNER_ID_TO_SERVICE, partner.getId());
            backIntent.putExtra(BackService.KEY_PARTNER_NAME_TO_SERVICE, partner.getNickName());

        }else {
            backIntent.putExtra(BackService.KEY_PARTNER_ID_TO_SERVICE, "");
            backIntent.putExtra(BackService.KEY_PARTNER_NAME_TO_SERVICE, "");
        }
        sendBroadcast(backIntent);

    }

    void menuSetting(){
        // menu_item 변경
    //    Log.d(TAG,"menu Setting");

        if(chat_type != 0) {
            if (m_menu != null) {
                MenuItem item1 = m_menu.findItem(R.id.action_item_1);
                MenuItem item2 = m_menu.findItem(R.id.action_refresh);

                item1.setTitle("친구삭제");
                item2.setVisible(false);

            }
        }
    }

    void initFont(){

        font = Typeface.createFromAsset(this.getAssets(), "tvNEnjoystoriesB.ttf");
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        setTitle("");
    }
    void setFont(TextView textView){
        textView.setTypeface(font);
    }


    @OnClick(R.id.iv_send)
    public void onClick() {
        String message = editText.getText().toString().trim();
        if(!message.equalsIgnoreCase("")){
            editText.setText("");
            emitSocket(message);
            adapter.addNewMsg(message,0,new Talk_Date().getTime(),"");
            rvChat.scrollToPosition(rvChat.getAdapter().getItemCount() - 1);
        }
    }

    private void emitSocket(String message){
        JSONObject sendMessage = new JSONObject();
        try {
            sendMessage.put("message",message);
            sendMessage.put("type",1);
            if(chat_type == 0){

                sendMessage.put("name",getString(R.string.random_person));

                ChatMessage temp = new ChatMessage(user.getId(), "", message, new Talk_Date().getTime() , 0);
                msgList.add(temp);
            }else{
                Log.d(TAG,"partner_serial : "+ partner.getId());
                Log.d(TAG,"name : "+ user.getNickName() );
                sendMessage.put("serial", partner.getId() );
                sendMessage.put("partner_serial", user.getId() );
                sendMessage.put("name",user.getNickName());

                ChatMessage temp = new ChatMessage(user.getId(), partner.getId() , message, new Talk_Date().getTime() , 0);
                msgList.add(temp);

                dbManager.insert_msg(temp);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG,"sendMessage : " + sendMessage);
        mSocket.emit("SMG",sendMessage);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");

        stop_listener();

        cur_activity = false;
        if(chat_type == 0){
            exit_random();
        }
        if(dialog != null){
            dialog.dismiss();
            dialog = null;
        }




        super.onDestroy();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        Log.d(TAG,"onCreateOptionsMenu");
        this.m_menu = menu;
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        menuSetting();
        return true;
    }

    private void start_talk(){
        mSocket = SocketConnection.getInstance().getSocket();
        mSocket.on("RMG", onNewMessage);
        mSocket.connect();
        cur_activity = true;
        Log.d(TAG,"start_talk : "+ mSocket.connected());

    }

    private void start_Random(){
        mSocket = SocketConnection.getInstance().getSocket();

        mSocket.on("RMG", onNewMessage);
        mSocket.connect();

        Log.d(TAG,"start_Random : "+ mSocket.connected());

        // 서버에 RCS 명령을 전송
        JSONObject joinMessage = new JSONObject();
        try {
            joinMessage.put("message","RCS");
            joinMessage.put("type",2);
//          joinMessage.put("name", user.getNickName() );
            joinMessage.put("serial", user.getId() );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("RCS",joinMessage);

        Log.d(TAG,"[RCS] " + joinMessage);

        cur_activity = true;
        chat_active = true;
    }

    private void exit_random(){
        JSONObject leaveMessage = new JSONObject();
        try {
            leaveMessage.put("message","");
            leaveMessage.put("type",2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("REN",leaveMessage);

        Log.d(TAG, "[REN(end Random Talk)] : " + leaveMessage);

        cur_activity = false;
        chat_active = false;
    }


    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(cur_activity == true) {
                ChatActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = null;
                        try {
                            data = (JSONObject) args[0];
                        } catch (ClassCastException e) {
                            String data2 = (String) args[0];
                            try {
                                data = new JSONObject(data2);
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                        Log.d(TAG, "[RMG(Emitter)]: " + data);

                        String message = null;
                        try {
                            message = data.getString("message");
                            int type = data.getInt("type");
                            //String name = data.getString("name");
                            String cur_time = new Talk_Date().getTime();

                            if (type == 2) {
                                clear();
                                if (message.equals("SWI")) {
                                    // 대기 상태
                                    return;
                                } else if (message.equals("RMI")) {
                                    toolbarTitle.setText(getString(R.string.message_waitState));
                                    message = "";
                                    return;
                                } else if (message.equals("RSM")) {
                                    Log.d(TAG, "message : " + getString(R.string.message_matching));
                                    message = getString(R.string.message_matching);
                                    toolbarTitle.setText(getString(R.string.random_person));
                                } else if (message.equals("RER")) {
                                    //Toast.makeText(getApplicationContext(), "상대가 나갔습니다. \n  새로고침", Toast.LENGTH_SHORT).show();
                                    if (chat_type == 0) {
                                        if (!isFinishing()) {
                                            new AlertDialog.Builder(ChatActivity.this)
                                                    .setMessage(getString(R.string.message_refresh))
                                                    .setPositiveButton("아니요", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            finish();
                                                            //return;
                                                        }
                                                    })
                                                    .setNegativeButton("네", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            exit_random();
                                                            clear();
                                                            stop_listener();
                                                            start_Random();
                                                        }
                                                    }).show();
                                        }
                                    }
                                    return;
                                } else if (message.equals("AFM")) {
                                    if (!isFinishing()) {
                                        new AlertDialog.Builder(ChatActivity.this)
                                                .setMessage(getString(R.string.message_addFriend))
                                                .setPositiveButton("아니요", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        // NOF 명령 전달
                                                        noAddFriend();
                                                    }
                                                })
                                                .setNegativeButton("네", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        // AFD 명령 전달
                                                        okAddFriend();
                                                    }
                                                }).show();
                                    }
                                    return;
                                } else if (message.equals("AFD")) {
                                    // 친구요청 승인
                                    partner = new ChatUser(data.getString("partner_serial"), data.getString("partner_name"));
                                    // AFE 명령 전송.
                                    okAddFriend2();


                                    return;
                                } else if (message.equals("AFE")) {
                                    // 친구요청 승인2
                                    partner = new ChatUser(data.getString("partner_serial"), data.getString("partner_name"));
                                    // AFS 명령 전달
                                    okFriend();

                                    // 현재 대화목록의 상대 id를 추가시켜준다
                                    // 친구목록에 추가하고
                                    // 현재 대화목록을 저장한다.

                                    if (dbManager.isFriend(partner.getId()) == false) {
                                        if (msgList.size() > 0) {
                                            setPartnerId_msgList(partner.getId());
                                            dbManager.insert_msg_list(msgList);
                                        }
                                        dbManager.insert_user(partner);
                                    }

                                    // 채팅창 변경
                                    chat_type = 1;
                                    menuSetting();
                                    toolbarTitle.setText(partner.getNickName());
                                    start_talk();

                                    // 채팅 history 초기화 후 화면 출력
                                    initMsg();

                                    Intent backIntent = new Intent();
                                    backIntent.setAction(BackService.ACTION_MSG_TO_SERVICE);
                                    backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);

                                    if (partner != null && partner.getId() != null && partner.getNickName() != null) {
                                        backIntent.putExtra(BackService.KEY_PARTNER_ID_TO_SERVICE, partner.getId());
                                        backIntent.putExtra(BackService.KEY_PARTNER_NAME_TO_SERVICE, partner.getNickName());

                                    } else {
                                        backIntent.putExtra(BackService.KEY_PARTNER_ID_TO_SERVICE, "");
                                        backIntent.putExtra(BackService.KEY_PARTNER_NAME_TO_SERVICE, "");
                                    }
                                    sendBroadcast(backIntent);


                                    exit_random();
                                    return;
                                } else if (message.equals("AFS")) {
                                    // 친구목록에 추가하고
                                    // 현재 대화목록을 저장한다.
                                    if (partner != null) {
                                        if (dbManager.isFriend(partner.getId()) == false) {
                                            if (msgList.size() > 0) {
                                                setPartnerId_msgList(partner.getId());
                                                dbManager.insert_msg_list(msgList);
                                            }
                                            dbManager.insert_user(partner);
                                        }

                                        // 채팅창 변경
                                        chat_type = 1;
                                        menuSetting();
                                        toolbarTitle.setText(partner.getNickName());
                                        start_talk();

                                        // 채팅 history 초기화 후 화면 출력
                                        initMsg();

                                        Intent backIntent = new Intent();
                                        backIntent.setAction(BackService.ACTION_MSG_TO_SERVICE);
                                        backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);

                                        if (partner != null && partner.getId() != null && partner.getNickName() != null) {
                                            backIntent.putExtra(BackService.KEY_PARTNER_ID_TO_SERVICE, partner.getId());
                                            backIntent.putExtra(BackService.KEY_PARTNER_NAME_TO_SERVICE, partner.getNickName());

                                        } else {
                                            backIntent.putExtra(BackService.KEY_PARTNER_ID_TO_SERVICE, "");
                                            backIntent.putExtra(BackService.KEY_PARTNER_NAME_TO_SERVICE, "");
                                        }
                                        sendBroadcast(backIntent);
                                        exit_random();

                                    } else {
                                        Log.d(TAG, "[AFS] : no partner id");
                                    }
                                    return;
                                } else if (message.equals("NOF")) {
                                    // 친구거절
                                    // 상대방이 거절을 하셨습니다 알림을 띄워준다.

                                    new AlertDialog.Builder(ChatActivity.this)
                                            .setTitle("친구 거절")
                                            .setMessage(getString(R.string.message_noFriend))
                                            .setPositiveButton("확인", null)
                                            .show();
                                    return;
                                } else if (message.equals("RFM")) {
                                    // 친구 삭제
                                    // 현재 채팅기록, 유저목록에서 삭제.
                                    // 상대방에게 RFO 명령 전달
                                    if (!isFinishing()) {
                                        ChatUser delUser = new ChatUser(data.getString("partner_serial"), "");
                                        dbManager.delete_user(delUser);

                                        Toast.makeText(getApplicationContext(), "친구삭제신청", Toast.LENGTH_SHORT).show();
                                        okRemoveFriend();

                                        finish();
                                    }
                                    //   return;

                                } else if (message.equals("RFO")) {
                                    if (!isFinishing()) {
                                        // 친구 삭제 완료
                                        Toast.makeText(getApplicationContext(), "친구삭제완료", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }

                            } else {
                                // type이 0, 1 이면 자신이나 상대방의 MSG임
                                // 여기서는 msg를 받는 쪽이므로 type는 1임

                                //Log.d(TAG,"[RMG] : what the problem? ");

                                ChatMessage temp = new ChatMessage(user.getId(), "", message, cur_time, type);
                                msgList.add(temp);

                                dbManager.insert_msg(temp);
                            }

                            adapter.addNewMsg(message, type, cur_time, user.getNickName());
                            rvChat.scrollToPosition(rvChat.getAdapter().getItemCount() - 1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }


    };

    // listener close
    void stop_listener(){
        mSocket.off("RMG", onNewMessage);
    }

    // 화면 지우기 & 초기화?
    private void clear(){
        mMsg = new ArrayList<>();
        mTime = new ArrayList<>();
        adapter = new ChatAdapter(ChatActivity.this, mMsg, mTime);
        LinearLayoutManager lm = new LinearLayoutManager(ChatActivity.this);
        lm.setStackFromEnd(true);
        rvChat.setLayoutManager(lm);
        rvChat.setAdapter(adapter);
        rvChat.scrollToPosition(rvChat.getAdapter().getItemCount() - 1);
    }

    // 새로고침
    private void refresh(){
       // isExited = true;
        if(chat_type == 0) {
            exit_random();
            clear();
            start_Random();
        }
    }


    // 메뉴 클릭 이벤트
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 메뉴의 항목을 선택(클릭)했을 때 호출되는 콜백메서드
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();


        switch(id) {
            case R.id.action_item_1:
                if(chat_type == 0) {
                    Toast.makeText(getApplicationContext(), "친구추가", Toast.LENGTH_SHORT).show();
                    addFriend();
                } else {
                    Toast.makeText(getApplicationContext(), "친구삭제", Toast.LENGTH_SHORT).show();
                    removeFriend();
                }
                return true;
            case R.id.action_item_2:
                Toast.makeText(getApplicationContext(), "신고하기", Toast.LENGTH_SHORT).show();
                // ?
                return true;
            case R.id.action_refresh:
                refresh();
                Toast.makeText(getApplicationContext(), "새로고침", Toast.LENGTH_SHORT).show();
                return true;
            case android.R.id.home: // 이전으로
                Intent backIntent = new Intent();
                backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                backIntent.setAction(BackService.ACTION_MSG_TO_SERVICE);
                backIntent.putExtra(BackService.KEY_PARTNER_ID_TO_SERVICE, "");
                backIntent.putExtra(BackService.KEY_PARTNER_NAME_TO_SERVICE, "");
                sendBroadcast(backIntent);
                this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void addFriend(){
        // 친구 추가 AFM 전달
        JSONObject message = new JSONObject();
        try {
            message.put("name", user.getNickName());
            message.put("serial", user.getId());
            message.put("type", 2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("AFM",message);

        Log.d(TAG, "[AFM] : " + message);
    }

    public void removeFriend(){
        // 친구 삭제 RFM 전달
        JSONObject message = new JSONObject();
        try {
            message.put("message", "RFM");
            message.put("type", 2);
            // 자신과 상대방의 serial을 같이 보냄
            Log.d(TAG,"partner_serial : "+ partner.getId());
            Log.d(TAG,"name : "+ user.getNickName() );
            message.put("serial", user.getId() );
            message.put("partner_serial", partner.getId() );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mSocket.emit("RFM",message);

        ChatUser delUser = new ChatUser(partner.getId(), "");
        dbManager.delete_user(delUser);

        Log.d(TAG, "[RFM] : " + message);

    }

    public void okRemoveFriend(){
        // 친구 삭제 RFO 전달
        JSONObject message = new JSONObject();
        try {
            // 자신과 상대방의 serial을 같이 보냄
            message.put("name", user.getNickName());
            message.put("serial", user.getId() );
            message.put("partner_serial", partner.getId() );
            message.put("type", 2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("RFO",message);

        Log.d(TAG, "[RFO] : " + message);

    }

    public void okAddFriend(){
        // 친구 추가 승인 AFD 전달
        JSONObject message = new JSONObject();
        try {
            message.put("type", 2);
            message.put("name", user.getNickName());
            message.put("serial", user.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("AFD",message);

        Log.d(TAG, "[AFD] : " + message);
    }

    public void okAddFriend2(){
        // 친구 추가 승인 AFE 전달
        JSONObject message = new JSONObject();
        try {
            message.put("type", 2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("AFE",message);

        Log.d(TAG, "[AFE] : " + message);
    }

    public void noAddFriend(){
        // 친구 추가 거절 NOF 전달
        JSONObject message = new JSONObject();
        try {
            message.put("type", 2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("NOF",message);

        Log.d(TAG, "[NOF] : " + message);
    }

    public void okFriend(){
        // 친구 추가 완료 AFS 전달
        JSONObject message = new JSONObject();
        try {
            message.put("type", 2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("AFS",message);

        Log.d(TAG, "[AFS] : " + message);
    }

    public void initMsg(){
        // 메시지 초기화
        // DB에 있는 채팅목록들을 읽어온다
        // 읽어온 채팅목록들을 화면에 뿌려준다.

        msgList = dbManager.getMsgList(partner.getId());

        for(int i=0; i<msgList.size(); i++){
            ChatMessage temp = msgList.get(i);
            adapter.addNewMsg(temp.getMessage(), temp.getType(), temp.getDate(), user.getNickName() );
        }
        // 스크롤 최하단으로 위치
        rvChat.scrollToPosition(rvChat.getAdapter().getItemCount() - 1);
    }

    public void setPartnerId_msgList(String partner_serial ){
        // msgList에 없는 partner id 정보를 넣어준다.

        for(int i=0; i < msgList.size(); i++){
            msgList.get(i).setOtherUserId(partner_serial);
        }
    }


}
