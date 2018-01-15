package project001.itcore.it_talk.adapter;

/**
 * Created by peten on 2017. 7. 12..
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import project001.itcore.it_talk.R;
import project001.itcore.it_talk.model.ChatUser;

public class ListViewAdapter extends BaseAdapter {

    // Declare Variables
    Context context;
    ArrayList<String> menu;
    ArrayList<String> info;
    ArrayList<String> time;
    LayoutInflater inflater;

    public ListViewAdapter(Context context, ArrayList<String> menu, ArrayList<String> info, ArrayList<String> time) {
        this.context = context;
        this.menu = menu;
        this.info = info;
        this.time = time;
    }

    public ListViewAdapter(Context context, ArrayList<ChatUser> users){
        this.context = context;
        menu = new ArrayList<String>();
        info = new ArrayList<String>();
        time = new ArrayList<String>();

        for(int i=0; i<users.size(); i++){
            menu.add(users.get(i).getNickName());
            if(users.get(i).getLastMessage() != null) {
                info.add(users.get(i).getLastMessage().getMessage());
                time.add(users.get(i).getLastMessage().getDate());
            }
            else{
                info.add("");
                time.add("");
            }
        }
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public int getCount() {
        return menu.size();
    }

    public View getView(int position, View convertView, ViewGroup parent) {


         // txtMenu

        // Declare Variables
        TextView txtMenu;
        TextView txtInfo;
        TextView txtTime;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View itemView = inflater.inflate(R.layout.listview_item, parent, false);
        View itemView_normal = inflater.inflate(R.layout.listview_item_normal, parent, false);


        if(info.get(position).isEmpty()) {
            txtMenu = (TextView) itemView_normal.findViewById(R.id.txtMenu);
            txtMenu.setText(menu.get(position));
            return itemView_normal;
        } else {
            // Locate the TextViews in listview_item.xml
            txtMenu = (TextView) itemView.findViewById(R.id.menu);
            txtInfo = (TextView) itemView.findViewById(R.id.info);
            txtTime = (TextView) itemView.findViewById(R.id.time);

            // Capture position and set to the TextViews
            txtMenu.setText(menu.get(position));
            txtInfo.setText(info.get(position));
            if(time.size() == getCount()) {
                txtTime.setText(time.get(position));
            }
        }

        return itemView;
    }
}