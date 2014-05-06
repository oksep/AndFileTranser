package com.server;

import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;



public class AcceptFileAdapter extends BaseAdapter
{
	private Context mContext;
	private HashMap<Integer, UpdateInfo> mInfoMap = new HashMap<Integer, UpdateInfo>();
	private UpdateInfo mInfo;
	private int mPercent;

	public AcceptFileAdapter(Context context, HashMap<Integer, UpdateInfo> map) {
		this.mContext = context;
		this.mInfoMap = map;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mInfoMap.size();
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
		View view = LayoutInflater.from(mContext).inflate(R.layout.item, null);
		TextView fileFrom = (TextView) view.findViewById(R.id.fileFrom);
		TextView fileName = (TextView) view.findViewById(R.id.fileName);
		mInfo = mInfoMap.get(position);

		fileFrom.append(mInfo.getIp());
		fileName.append(mInfo.getFileName());
		ProgressBar pb = (ProgressBar) view.findViewById(R.id.progressBar);
		pb.setMax((int) mInfo.getTotalLength());
		pb.setProgress((int) mInfo.getAcceptLength());
		TextView transStatus = (TextView) view.findViewById(R.id.transStatus);
		mPercent=(int) ((double)mInfo.getAcceptLength()/ mInfo.getTotalLength()*100);
		transStatus.setText("×´Ì¬:" +mPercent+ "%  ");
		transStatus.append(mInfo.getAcceptLength()+"×Ö½Ú");
		return view;
	}

}
