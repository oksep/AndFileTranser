package com.client;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SendFileAdapter extends BaseAdapter
{
	private Context mContext;
	private ArrayList<String> mFilesList;
	private  HashMap<Integer, UpdateInfo> mMap;

	public SendFileAdapter(Context context, ArrayList<String> List, HashMap<Integer, UpdateInfo> mMap) {
		this.mContext = context;
		this.mFilesList = List;
		this.mMap=mMap;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mFilesList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View mView = LayoutInflater.from(mContext).inflate(R.layout.sendfile_item, null);
		TextView fileNameTxt = (TextView) mView.findViewById(R.id.fileName);
		TextView sendLenTxt = (TextView) mView.findViewById(R.id.transStatus);
		ProgressBar progressBar = (ProgressBar) mView.findViewById(R.id.progressBar);
		progressBar.setMax((int) mMap.get(position).getTotalLength());
		progressBar.setProgress((int) mMap.get(position).getSendLength());
		fileNameTxt.setText("ÎÄ¼þ:"+mFilesList.get(position).substring(mFilesList.get(position).lastIndexOf(File.separator)));
//		fileNameTxt.setText(""+mMap.get(position).getTotalLength());
		sendLenTxt.setText(mMap.get(position).getSendLength()+"×Ö½Ú");
		return mView;
	}

}
