package project001.itcore.it_talk.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by peten on 2017. 10. 15..
 */

public class Talk_Date {

    public String getTime(){
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy h:mm a");
        String timeText = outputFormat.format(new Date());
        return timeText;
    }
}
