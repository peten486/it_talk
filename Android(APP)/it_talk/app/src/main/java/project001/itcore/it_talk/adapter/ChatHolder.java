package project001.itcore.it_talk.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;


import project001.itcore.it_talk.R;

/**
 * Created by peten on 2017. 10. 1..
 */

public class ChatHolder extends RecyclerView.ViewHolder {
    public TextView name;
    public TextView time;
    public TextView msg;

    public ChatHolder(View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.tv_name);
        msg = (TextView) itemView.findViewById(R.id.tv_chatMsg);
        time = (TextView) itemView.findViewById(R.id.tv_time);
    }
}