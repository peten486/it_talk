package project001.itcore.it_talk.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;


import java.text.SimpleDateFormat;
import java.util.Date;

import project001.itcore.it_talk.R;
import project001.itcore.it_talk.adapter.DBManager;
import project001.itcore.it_talk.Service.BackService;
import project001.itcore.it_talk.model.ChatMessage;
import project001.itcore.it_talk.model.ChatUser;
import project001.itcore.it_talk.model.SocketConnection;

public class MainActivity extends AppCompatActivity  {

    // 현재 설정 계정
    private ChatUser user;
    String sfName = "myFile"; // local file..?


    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    Toolbar toolbar;
    Typeface font;

    // socket.io 설정 변수
    private Socket mSocket;

    // notificatoin 변수
    private NotificationManager mNM;
    private Notification.Builder mBuilder;
    Intent push;
    PendingIntent fullScreenPendingIntent;

    private boolean chk_socket = false;

    final DBManager dbManager = new DBManager(MainActivity.this, "chat.db", null, 1);


    private AlertDialog dialog;

    void initFont(){

        font = Typeface.createFromAsset(this.getAssets(), "tvNEnjoystoriesB.ttf");
    }

    void setFont(TextView textView){
        textView.setTypeface(font);
    }

    public static void applyFontForToolbarTitle(Activity context){
        Toolbar toolbar = (Toolbar) context.findViewById(R.id.toolbar);
        for(int i = 0; i < toolbar.getChildCount(); i++){
            View view = toolbar.getChildAt(i);
            if(view instanceof TextView){
                TextView tv = (TextView) view;
                Typeface titleFont = Typeface.
                        createFromAsset(context.getAssets(), "tvNEnjoystoriesB.ttf");
                if(tv.getText().equals(toolbar.getTitle())){
                    tv.setTypeface(titleFont);
                    break;
                }
            }
        }
    }

    /*
    @Override
    protected void onResume(){
        super.onResume();

        Log.d(this.getClass().getSimpleName(),"onResume");

        //

        mSectionsPagerAdapter.notifyDataSetChanged();

    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(this.getClass().getSimpleName(),"onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //user 초기화
        user = new ChatUser();

        SharedPreferences sf = getSharedPreferences(sfName,0);
        user.setNickName(sf.getString("nickName","")); // 닉네임

        // db
        if(user.getId().equals(sf.getString("id",""))) {
            user.setId(sf.getString("id","")); // id

        } else {
           user = new ChatUser();
           // 대화기록 초기화
        }

        Intent back = new Intent(MainActivity.this, BackService.class);
        back.putExtra("id", user.getId());
        back.putExtra("nickName", user.getNickName());
       // back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startService(back);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        applyFontForToolbarTitle(this);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // notification setup
        // head up noti
        mBuilder = new Notification.Builder(this);
        mBuilder.setSmallIcon(R.drawable.icon);
        mBuilder.setTicker("알람 간단한 설명");
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        mBuilder.setContentIntent(fullScreenPendingIntent);
        mBuilder.setAutoCancel(true);

        mNM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);



        // 서버 연결
        //start_ItTalk();

    }

    @Override
    protected void onDestroy() {

        Log.d("MainActivity","onDestroy");
        super.onDestroy();

        //exit();
        Intent back = new Intent(MainActivity.this, BackService.class);
        stopService(back);

    }

    @Override
    protected void onStop() {

        Log.d("MainActivity","onStop");
        super.onStop();
        // Activity 가 종료되기 전에 저장한다
        // SharedPreferences 에 설정값(특별히 기억해야할 사용자 값)을 저장하기
        SharedPreferences sf = getSharedPreferences(sfName, 0);
        SharedPreferences.Editor editor = sf.edit();//저장하려면 editor가 필요

        editor.putString("nickName", user.getNickName()); // 입력
        //user.setDeviceId();
        editor.putString("id", user.getId()); // 입력
        editor.commit(); // 파일에 최종 반영함
    }




/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private FragmentManager fm;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fm = fm;
        }

        @Override
        public Fragment getItem(int position) {
            //태그로 프래그먼트를 찾는다.
            Fragment fragment = fm.findFragmentByTag("android:switcher:" + mViewPager.getId() + ":" + getItemId(position));

            //프래그먼트가 이미 생성되어 있는 경우에는 리턴
            if (fragment != null) {
                return fragment;
            }

            //프래그먼트의 인스턴스를 생성한다.

            // .newInstance의 파라미터는 사용하기전에 공백("")으로 비워두면 됨.
            switch(position) {

                //case 0: return Frg_01.newInstance("");
                case 0: return Frg_01.newInstance("Test ver.", user);
                case 1: return Frg_02.newInstance("Test ver.", user);
                case 2: return Frg_03.newInstance("Test ver.", user);
                default: return Frg_03.newInstance("Test ver.", user);
            }

        }

        @Override
        public int getItemPosition(Object object){
            return POSITION_NONE;
        }


        // 프래그먼트를 최대 3개를 생성할 것임
        @Override
        public int getCount() {
            return 3;
        }

        // 탭의 제목으로 사용되는 문자열 생성
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "시작하기";
                case 1:
                    return "친구";
                case 2:
                    return "설정";
            }
            return null;
        }





    }

/*
    // 잇톡 서버 연결
    private void start_ItTalk(){

        try {
            mSocket = SocketConnection.getInstance().getSocket();
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.on("message", onSocketConnectionListener);
            mSocket.on("RMG", onNewMessage);
            mSocket.connect();
            Log.d(this.getClass().getSimpleName(), "start iT_talk : " + mSocket.connected());
        }
        catch (Exception e){
            e.printStackTrace();
        }

        // 연결 될 경우에는 서버에 STA 명령을 전송
        JSONObject joinMessage = new JSONObject();
        try {
            joinMessage.put("message","STA");
            joinMessage.put("type",2);
            joinMessage.put("name", user.getNickName() );
            joinMessage.put("serial", user.getId() );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("STA",joinMessage);

        Log.d(this.getClass().getSimpleName(),"[STA] " + joinMessage);
    }
*/

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
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

                    Log.d(this.getClass().getSimpleName(), "[RMG(Emitter)] : " + data);

                    String message = null;
                    try {
                        message = data.getString("message");
                        int type = data.getInt("type");
                        //String name = data.getString("name");

                        if(type == 1){
                            // 상대방 메시지의 경우
                            // random chat이 아닐 경우여야 함.
                            // partner_serial 의 데이터가 나오는 경우는 일반 채팅임.
                            String temp = data.getString("partner_serial");
                            if(!temp.equals("") && !temp.isEmpty()) {
                                Log.d(this.getClass().getSimpleName(), "[RMG(Emitter)] partner_serial : " + data.getString("partner_serial"));

                                ChatUser partner = new ChatUser(temp, data.getString("name"));
                                noti(partner, message);

                                // ChatActivity 로 전달
                            }
                        } else if(type == 2){
                            if(message.equals("SWI")){
                                // 대기 상태

                            }
                        } else{
                            Log.d(this.getClass().getSimpleName(), "[RMG(Emitter)] partner_serial : " + data.getString("partner_serial"));

                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    /*
    // socket 종료
    private void exit(){
        JSONObject leaveMessage = new JSONObject();
        try {
            leaveMessage.put("message","");
            leaveMessage.put("type",2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("REN",leaveMessage);

        Log.d(this.getClass().getSimpleName(), "[REN(exit)] : " + leaveMessage);

        SocketConnection.getInstance().disconnect();
    }
*/


    /**
     * Listener for socket connection error.. listener registered at the time of socket connection
     */
    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

                Log.d("peten","socket error");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mSocket == null) {
                            if (mSocket.connected() == false) {
                                mSocket = SocketConnection.getInstance().getSocket();

                                chk_socket = false;
                            }
                        }

                        if (chk_socket == false) {
                            if (!isFinishing()) {
                                dialog = new AlertDialog.Builder(MainActivity.this)
                                        .setMessage(getString(R.string.message_disconnect2))
                                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }

                                        })
                                        .setNegativeButton("아니요", null)
                                        .show();
                                //dialog.dismiss();
                            }
                            chk_socket = true;

                        }
                    }

                });


        }
    };

    /**
     * Listener to handle messages received from chat server of any type... Listener registered at the time of socket connected
     */
    private Emitter.Listener onSocketConnectionListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // handle the response args
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

        push.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        fullScreenPendingIntent = PendingIntent.getActivity(this, 0, push, PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setFullScreenIntent(fullScreenPendingIntent, true);


        mBuilder.setContentTitle(partner.getNickName());
        mBuilder.setContentText(msg);
        mNM.notify(0, mBuilder.build());

        ChatMessage temp = new ChatMessage(user.getId(), partner.getId(), msg, getTime(), 1);
        dbManager.insert_msg(temp);

    }

    private String getTime(){
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy h:mm a");
        String timeText = outputFormat.format(new Date());
        return timeText;
    }



}
