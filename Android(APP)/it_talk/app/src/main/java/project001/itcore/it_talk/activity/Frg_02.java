package project001.itcore.it_talk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import project001.itcore.it_talk.adapter.DBManager;
import project001.itcore.it_talk.adapter.ListViewAdapter;
import project001.itcore.it_talk.R;
import project001.itcore.it_talk.model.ChatMessage;
import project001.itcore.it_talk.model.ChatUser;

/**
 * Created by peten on 2017. 5. 19..
 */

public class Frg_02 extends Fragment {

    ArrayList<ChatUser> user_list;
    ListViewAdapter adapter;
    ChatUser user;

    ListView listview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.second_frag, container, false);
        listview = (ListView) v.findViewById(R.id.listview);

        Log.d(this.getClass().getSimpleName(), "onCreateView");


        final DBManager dbManager = new DBManager( getActivity() , "chat.db", null, 1);

        user_list = new ArrayList<ChatUser>();

        // DB 에서 친구목록을 불어와서 세팅
        user_list.addAll( dbManager.getUserList() );
        for(int i=0; i< user_list.size(); i++){
            if(user_list.get(i).getLastMessage() == null && dbManager.getLastMsg(user_list.get(i).getId()) != null) {
                user_list.get(i).setLastMessage(dbManager.getLastMsg(user_list.get(i).getId()));
            }
        }

        // Pass results to ListViewAdapter Class
        adapter = new ListViewAdapter(getActivity(), user_list);
        listview.setAdapter(adapter);


        TextView tv = (TextView) v.findViewById(R.id.tvFragSecond);
        tv.setText(getArguments().getString("msg"));


        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // Send single item click data to SingleItemView Class
                Intent i = new Intent(getActivity(), ChatActivity.class);
                // Pass a single position

                i.putExtra("position", position);
                i.putExtra("type", 1);
                i.putExtra("id", user.getId());
                i.putExtra("nickName", user.getNickName());
                i.putExtra("partner_id", user_list.get(position).getId());
                i.putExtra("partner_nickname", user_list.get(position).getNickName());


                // Open SingleItemView.java Activity
                startActivity(i);

            }
        });  // 이문장은 함수 선언이 아니라 함수를 호출하는 부분입니다.
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(this.getClass().getSimpleName(),"onActivityResult");

    }

    public static Frg_02 newInstance(String text, ChatUser chatUser) {


        Frg_02 f = new Frg_02();
        Log.d(f.getClass().getSimpleName(), "newInstance");
        f.user = chatUser;
        Bundle b = new Bundle();
        b.putString("msg", text);// 설명부분.

        f.setArguments(b);


        return f;
    }

}