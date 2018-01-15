package project001.itcore.it_talk.adapter;

import android.content.ContentProvider;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;

import project001.itcore.it_talk.model.ChatMessage;
import project001.itcore.it_talk.model.ChatUser;

/**
 * Created by peten on 2017. 10. 4..
 */

public class DBManager extends SQLiteOpenHelper{
    // 생성자로 DB 이름과 버전을 넘겨 받습니다.
    Context context;
    public DBManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }
    // 생성자에서 넘겨받은 이름과 버전의 데이터베이스가 존재하지 않을때 한번 호출 됩니다
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 새로운 테이블을 생성한다.
        // create table 테이블명 (컬럼명 타입 옵션);

        db.execSQL("CREATE TABLE tb_chat_message( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_serial TEXT, " +
                "partner_serial TEXT, " +
                "msg TEXT, " +
                "date TEXT, " +
                "type INTEGER " +
                ");");

        db.execSQL("CREATE TABLE tb_partner( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "serial TEXT, " +
                "nickname TEXT " +
                ");");

    }
/*
    public void insert(String _query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(_query);
        db.close();
    }

    public void update(String _query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(_query);
        db.close();
    }

    public void delete(String _query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(_query);
        db.close();
    }
*/

    public boolean isFriend(String serial){

        StringBuffer sb = new StringBuffer();
        sb.append(" select * from tb_partner where serial = ? ");


        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.rawQuery(sb.toString(), new String[]{ serial });

        ChatMessage temp = null;


        if(cursor.moveToFirst()){
         //   Toast.makeText(context, serial+"is Friend", Toast.LENGTH_SHORT).show();
            return true;
        }

        cursor.close();
       // Toast.makeText(context, serial+"is not Friend", Toast.LENGTH_SHORT).show();
        return false;
    }

    public ArrayList<ChatUser> getUserList(){
        StringBuffer sb = new StringBuffer();
        sb.append(" select * from tb_partner ");

        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.rawQuery(sb.toString(), null);

        ArrayList<ChatUser> temp = new ArrayList<ChatUser>();

        while( cursor.moveToNext() ){

            ChatUser user = null;
            user = new ChatUser(cursor.getString(1), cursor.getString(2));
            temp.add(user);

        }

        cursor.close();

        //Toast.makeText(context, "getUserList", Toast.LENGTH_SHORT).show();
        return temp;
    }

    public ArrayList<ChatMessage> getMsgList(){
        StringBuffer sb = new StringBuffer();
        sb.append(" select * from tb_chat_message ");

        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.rawQuery(sb.toString(), null);

        ArrayList<ChatMessage> temp = new ArrayList<ChatMessage>();

        while( cursor.moveToNext() ){
            temp.add(new ChatMessage(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getInt(5)));
        }

        cursor.close();
        //Toast.makeText(context, "getMsgList", Toast.LENGTH_SHORT).show();

        return temp;
    }

    public String getPartnerNickname(String serial){
        StringBuffer sb = new StringBuffer();
        sb.append(" select * from tb_partner where 1=1 and serial = ? ");
        sb.append(" ORDER BY _id DESC LIMIT 1 ");
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.rawQuery(sb.toString(), new String[]{ serial });

        ChatUser temp = null;

        if(cursor.moveToFirst()){
            temp = new ChatUser(cursor.getString(1), cursor.getString(2) );
        }

        cursor.close();

        return temp.getNickName();
    }

    public ChatMessage getLastMsg(String serial){

        StringBuffer sb = new StringBuffer();
        sb.append(" select * from tb_chat_message where 1=1 and partner_serial = ? ");
        sb.append(" ORDER BY _id DESC LIMIT 1 ");


        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.rawQuery(sb.toString(), new String[]{ serial });

        ChatMessage temp = null;

        if(cursor.moveToFirst()){
            temp = new ChatMessage(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getInt(5));
        }

        cursor.close();

        return temp;
    }

    public ArrayList<ChatMessage> getMsgList(String serial){
        StringBuffer sb = new StringBuffer();
        sb.append(" select * from tb_chat_message where partner_serial = ? ");

        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.rawQuery(sb.toString(), new String[]{ serial });

        ArrayList<ChatMessage> temp = new ArrayList<ChatMessage>();


        while( cursor.moveToNext() ){
            temp.add(new ChatMessage(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getInt(5) ));
        }

        cursor.close();

        return temp;
    }



    // insert

    public void insert_user ( ChatUser user ){
        SQLiteDatabase db = getWritableDatabase();

        StringBuffer sb = new StringBuffer();
        sb.append(" INSERT INTO tb_partner ( ");
        sb.append(" serial, nickname ) ");
        sb.append(" VALUES ( ?, ? ) ");

        db.execSQL(sb.toString(), new Object[]{user.getId(), user.getNickName() });

        db.close();
        //Toast.makeText(context, "insert_user", Toast.LENGTH_SHORT).show();
    }

    public void insert_msg ( ChatMessage msg ){
        SQLiteDatabase db = getWritableDatabase();

        StringBuffer sb = new StringBuffer();
        sb.append(" INSERT INTO tb_chat_message ( ");
        sb.append(" user_serial, partner_serial, msg, date, type ) ");
        sb.append(" VALUES ( ?, ?, ?, ?, ? ) ");

        db.execSQL(sb.toString(), new Object[]{msg.getId(), msg.getOtherUserId(), msg.getMessage(), msg.getDate(), msg.getType()});

        db.close();
        //Toast.makeText(context, "insert_user", Toast.LENGTH_SHORT).show();
    }

    public void insert_user_list(ArrayList<ChatUser> user_list ){
        SQLiteDatabase db = getWritableDatabase();

        for(int i=0; i < user_list.size(); i++){

            StringBuffer sb = new StringBuffer();
            sb.append(" INSERT INTO tb_partner ( ");
            sb.append(" serial, nickname ) ");
            sb.append(" VALUES ( ?, ? ) ");

            db.execSQL(sb.toString(), new Object[]{user_list.get(i).getId(), user_list.get(i).getNickName() });
        }
        db.close();
    }

    public void update_user(ChatUser user){
        StringBuffer sb = new StringBuffer();
        sb.append(" update tb_partner set nickname = ? where serial = ? ");

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sb.toString(), new Object[]{ user.getNickName(), user.getId() });
        db.close();
    }


    public void insert_msg_list(ArrayList<ChatMessage> msg_list){
        SQLiteDatabase db = getWritableDatabase();

        for(int i=0; i<msg_list.size(); i++){

            StringBuffer sb = new StringBuffer();
            sb.append(" INSERT INTO tb_chat_message ( ");
            sb.append(" user_serial, partner_serial, msg, date, type ) ");
            sb.append(" VALUES ( ?, ?, ?, ?, ? ) ");

            db.execSQL(sb.toString(), new Object[]{msg_list.get(i).getId(), msg_list.get(i).getOtherUserId(), msg_list.get(i).getMessage(), msg_list.get(i).getDate(), msg_list.get(i).getType()});
        }

        db.close();
        //Toast.makeText(context, "insert_msg_list", Toast.LENGTH_SHORT).show();
    }

    public void delete_user(ChatUser user){

        SQLiteDatabase db = getWritableDatabase();

        // user와 해당하는 user의 대화목록까지 제거
        StringBuffer sb1 = new StringBuffer();
        sb1.append(" delete from tb_partner where serial = ? ");

        StringBuffer sb2 = new StringBuffer();
        sb2.append(" delete from tb_chat_message where partner_serial = ? ");

        db.execSQL(sb1.toString(), new Object[]{ user.getId()});
        db.execSQL(sb2.toString(), new Object[]{ user.getId() });

        db.close();

    }

    public void delete_history(){
        // 모든 대화기록들을 삭제

        SQLiteDatabase db = getWritableDatabase();
        String sqlDelete = "DELETE FROM tb_chat_message" ;
        db.execSQL(sqlDelete) ;
        db.close();

       // Toast.makeText(context, "대화기록 삭제", Toast.LENGTH_SHORT).show();

        //delete_all();
    }



    public void delete_all(){

        SQLiteDatabase db = getWritableDatabase();
        String sqlDelete = "DELETE FROM tb_partner" ;
        db.execSQL(sqlDelete) ;
        db.close();

      //  Toast.makeText(context, "파트너 삭제", Toast.LENGTH_SHORT).show();
    }


    // 데이터베이스가 존재하지만 버전이 다르면 호출 됩니다.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
