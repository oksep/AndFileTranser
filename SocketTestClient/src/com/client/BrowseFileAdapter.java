package com.client;

import java.io.File;
import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BrowseFileAdapter extends BaseAdapter
{
	private Context mContext;
	/** 文件与文件夹区分 **/
	public ArrayList<Boolean> mIsFileList = new ArrayList<Boolean>();
	/** 文件列表 **/
	public ArrayList<String> mFilesList = new ArrayList<String>();

	public BrowseFileAdapter(Context context, ArrayList<Boolean> mIsFileList, ArrayList<String> mFilesList) {
		this.mContext = context;
		this.mFilesList = mFilesList;
		this.mIsFileList = mIsFileList;
	}

	@Override
	public int getCount() {
		return mFilesList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.showfile_item, null);
		TextView textView = (TextView) view.findViewById(R.id.tv);
		String txt = mFilesList.get(position);
		txt = txt.substring(txt.lastIndexOf(File.separator));
		textView.setText(txt);
		ImageView iv = (ImageView) view.findViewById(R.id.iv);
		if (mIsFileList.get(position)) {
			iv.setBackgroundResource(R.drawable.icon);
		}
		return view;
	}
}
