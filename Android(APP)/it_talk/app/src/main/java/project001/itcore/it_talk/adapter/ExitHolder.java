package project001.itcore.it_talk.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import project001.itcore.it_talk.R;

/**
 * Created by peten on 2017. 10. 1..
 */

public class ExitHolder extends RecyclerView.ViewHolder {
    TextView name;
    public ExitHolder(View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.tv_exit);
    }
}
