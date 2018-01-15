package project001.itcore.it_talk.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import project001.itcore.it_talk.adapter.DBManager;
import project001.itcore.it_talk.adapter.ListViewAdapter;
import project001.itcore.it_talk.R;
import project001.itcore.it_talk.model.ChatUser;

/**
 * Created by peten on 2017. 5. 19..
 */

public class Frg_03 extends Fragment {

    ArrayList<String> LIST_MENU;
    ArrayList<String> LIST_INFO;
    ListViewAdapter adapter;
    ChatUser user;
    nickNameDialog dialog;
    ListView listview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.third_frag, container, false);
        Log.d(this.getClass().getSimpleName(), "onCreateView");


        final DBManager dbManager = new DBManager( getActivity() , "chat.db", null, 1);

        listview = (ListView) v.findViewById(R.id.listview);
        LIST_MENU = new ArrayList<String>();
        LIST_INFO = new ArrayList<String>();

        LIST_MENU.add("대화명");
        LIST_MENU.add("개발자 이메일로 문의하기");
        LIST_MENU.add("기록삭제");
        LIST_MENU.add("Serial Number");

        LIST_INFO.add("대화명 : "+user.getNickName());
        LIST_INFO.add("");
        LIST_INFO.add("대화기록들을 삭제합니다.");
        LIST_INFO.add("");

        // Pass results to ListViewAdapter Class
        adapter = new ListViewAdapter(getActivity(), LIST_MENU, LIST_INFO, new ArrayList<String>());
        listview.setAdapter(adapter);

        TextView tv = (TextView) v.findViewById(R.id.tvFragThird);
        tv.setText(getArguments().getString("msg"));

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                adapterView.getItemAtPosition(pos);

                String item = LIST_MENU.get(pos);
                Activity root = getActivity();

                switch(pos){
                    case 0:
                      //  Toast.makeText(root, "대화명 : "+user.getNickName() , Toast.LENGTH_SHORT).show(); //toast test
                        abc();
                        break;
                    case 1:
                        Toast.makeText(root, item, Toast.LENGTH_SHORT).show(); //toast test
                        break;
                    case 2:

                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        //Yes button clicked
                                            Toast.makeText(getActivity(), "기록을 삭제합니다.", Toast.LENGTH_SHORT).show(); //toast test
                                            dbManager.delete_history();
                                            Intent in = new Intent(getContext(), MainActivity.class);
                                            startActivity(in);
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                            Toast.makeText(getActivity(), "취소합니다.", Toast.LENGTH_SHORT).show(); //toast test
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("기록을 삭제하시겠습니까?").setPositiveButton("네", dialogClickListener)
                                .setNegativeButton("아니요", dialogClickListener).show();


                        break;
                    case 3:
                        Toast.makeText(root, "serial : "+user.getId(), Toast.LENGTH_SHORT).show(); //toast test
                        break;
                }



            }
        });  // 이문장은 함수 선언이 아니라 함수를 호출하는 부분입니다.


        return v;
    }

    public static Frg_03 newInstance(String text, ChatUser chatUser) {

        Frg_03 f = new Frg_03();
        Log.d(f.getClass().getSimpleName(), "newInstance");
        f.user = chatUser;
        Bundle b = new Bundle();
        b.putString("msg", text);// 설명부분.

        f.setArguments(b);

        return f;
    }


    public void abc() {
        FragmentManager fm = getFragmentManager();
        dialog = new nickNameDialog();
        dialog.setTargetFragment(this, 0);
        //System.out.println("target fragment : " + dialog.getTargetFragment().toString());
        dialog.show(getFragmentManager(),"TAG");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //System.out.println("intent after requestCode : " + requestCode + ", resultCode : " + resultCode);
        String name = data.getStringExtra("newName");
        int pos = getMenuPosition("대화명 : "+user.getNickName());
        user.setNickName(name);
        user.setDeviceId();

        SharedPreferences sf = getActivity().getSharedPreferences("myFile", 0);
        SharedPreferences.Editor editor = sf.edit();//저장하려면 editor가 필요

        editor.putString("nickName", user.getNickName()); // 입력
        editor.putString("id", user.getId()); // 입력
        editor.commit();


        if(pos != -1) {
            LIST_INFO.set(pos,"대화명 : "+ name);
            System.out.println("list view : "+LIST_INFO.get(pos));
            adapter = new ListViewAdapter(getActivity(), LIST_MENU, LIST_INFO, new ArrayList<String>());
            listview.setAdapter(adapter);
        }
        //System.out.println("intent after : " + name);
        // Toast.makeText(getActivity(), name , Toast.LENGTH_SHORT).show(); //toast test
    }

    public int getMenuPosition(String name){
        for(int i=0; i<LIST_INFO.size(); i++){
            if(LIST_INFO.get(i).equals(name)){
                return i;
            }
        }
        return -1;
    }



}