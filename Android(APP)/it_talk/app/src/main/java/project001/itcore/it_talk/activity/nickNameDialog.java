package project001.itcore.it_talk.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import project001.itcore.it_talk.activity.ChatActivity;

/**
 * Created by peten on 2017. 7. 14..
 */

public class nickNameDialog extends DialogFragment {
    private EditText editText;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        mBuilder.setTitle("대화명을 입력해주세요");
        editText = new EditText(getActivity());



        // 대화명 변경시 Enter 키 금지
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == event.KEYCODE_ENTER){
                    return true;
                }
                return false;
            }
        });

        mBuilder.setView(editText);
        mBuilder.setPositiveButton("입력",
                new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id){

                        Intent i = new Intent(getActivity(), ChatActivity.class);
                        // Pass a newNickName
                        i.putExtra("newName", editText.getText().toString());

                        //System.out.println("intent before : " + editText.getText().toString());
                        //System.out.println("target fragment : " + getTargetFragment().toString());
                        getTargetFragment().onActivityResult(0,0,i);
                        // Toast.makeText(getActivity(),editText.getText().toString(),Toast.LENGTH_SHORT);
                    }
                }).setNegativeButton("취소",null);


        return mBuilder.create();

    }

    @Override
    public void onStop(){
        super.onStop();
    }

}
