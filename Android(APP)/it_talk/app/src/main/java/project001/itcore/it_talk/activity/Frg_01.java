package project001.itcore.it_talk.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import project001.itcore.it_talk.R;
import project001.itcore.it_talk.model.ChatUser;

/**
 * Created by peten on 2017. 5. 19..
 */

public class Frg_01 extends Fragment {
    ChatUser user;
    String nickname, serial;

    Typeface font;

    void initFont(){

        font = Typeface.createFromAsset(getActivity().getAssets(), "tvNEnjoystoriesB.ttf");
    }

    void setFontButton(Button button){
        button.setTypeface(font);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(this.getClass().getSimpleName(), "onCreateView");
        View v = inflater.inflate(R.layout.first_frag, container, false);

        TextView tv = (TextView) v.findViewById(R.id.tvFragFirst);
        Button button = (Button) v.findViewById(R.id.m_but1);
        tv.setText(getArguments().getString("msg"));

        initFont();
        setFontButton(button);


        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                goToAttract(view);
            }
        });

        return v;
    }

    public void goToAttract(View v)
    {
        Intent intent = new Intent(this.getActivity(), ChatActivity.class);

        // 채팅창에 파라미터 전달
        intent.putExtra("type", 0);
        intent.putExtra("id", serial);
        intent.putExtra("nickName", nickname);

        Log.d(this.getClass().getSimpleName(), "goToAttract :" + intent);
        startActivity(intent);
    }

    public static Frg_01 newInstance(String text, ChatUser chatUser) {

        Frg_01 f = new Frg_01();

        Log.d(f.getClass().getSimpleName(), "newInstance");
        f.user = chatUser;
        Bundle b = new Bundle();
        b.putString("msg", text); // 설명부분.

        f.setArguments(b);
        f.serial = chatUser.getId();
        f.nickname = chatUser.getNickName();

        return f;
    }
}