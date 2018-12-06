package com.bluetank.fire_chat_ex;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class ProgressDlg extends AsyncTask<Integer,String,Integer> {

    private ProgressDialog progressDialog;
    private Context context;


    //constructor
    public ProgressDlg(Context context){
        this.context=context;
    }

    @Override
    protected void onPreExecute() {

        //ProgressDialog 세팅
        progressDialog=new ProgressDialog(context);

        //스타일 설정
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("진행중...");

        progressDialog.show();

        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(Integer... integers) {

        //
        return null;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        progressDialog.dismiss();
    }
}
