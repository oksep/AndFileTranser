package com.client;

import java.io.File;
import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class ShowFileActivity extends Activity
{
	/** 根路径 **/
	private final String ROOTPATH = Environment.getExternalStorageDirectory().getPath();
	private ListView mFileListView;
	private File[] mFiles;
	private BrowseFileAdapter mListViewAdpter;
	/** 上一路径 **/
	private String mBackPath = Environment.getExternalStorageDirectory().getPath();
	private Button mRootPathBtn;
	private Button mParentPathBtn;
	private Button mOkBtn;
	/** 文件与文件夹区分的集合 **/
	public ArrayList<Boolean> mIsFileList = new ArrayList<Boolean>();
	/** 文件列表 **/
	public ArrayList<String> mFilesList = new ArrayList<String>();
	/** 待传输文件 **/
	private ArrayList<String> mTransFilesList = new ArrayList<String>();
	private int mIndex = 0;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filelist);
		init();
	}

	/**
	 * 初始化
	 */
	public void init() {
		File file = new File(ROOTPATH);
		checkFiles(file);

		mFileListView = (ListView) findViewById(R.id.listView);
		mListViewAdpter = new BrowseFileAdapter(this, mIsFileList, mFilesList);
		mFileListView.setAdapter(mListViewAdpter);
		mFileListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (mFiles[arg2].isDirectory()) {
					File file = new File(mFiles[arg2].getPath());
					mBackPath = file.getParentFile().getPath();
					updateData(file);
				} else {
					mIndex = arg2;
					AlertDialog.Builder dialog = new AlertDialog.Builder(ShowFileActivity.this);
					dialog.setTitle("添加至传输文件列表");
					dialog.setMessage("确认添加?");
					
					dialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (!mTransFilesList.contains(mFiles[mIndex].getPath())) {
								mTransFilesList.add(mFiles[mIndex].getPath());
							}
							Toast.makeText(getBaseContext(), "已添加", 0).show();
							ShowFileActivity.this.setTitle(mTransFilesList.size() + "个文件");
						}
					});			
					dialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
					dialog.create();
					dialog.show();
				}
			}
		});

		mRootPathBtn = (Button) findViewById(R.id.btn_root);
		mRootPathBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				File file = new File(ROOTPATH);
				updateData(file);
			}
		});
		
		mParentPathBtn = (Button) findViewById(R.id.btn_upLevel);
		mParentPathBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				File file = new File(mBackPath);
				if (!mBackPath.endsWith(ROOTPATH)) {
					mBackPath = file.getParentFile().getPath();
				}
				updateData(file);
			}
		});
		
		mOkBtn=(Button) findViewById(R.id.btn_ok);
		mOkBtn.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View v) {
				Intent intent=getIntent();
				intent.putStringArrayListExtra("files", mTransFilesList);
				setResult(0, intent);
				finish();		
			}
		});
		
		
	}

	/**
	 * 更新adapter
	 * 
	 * @param file
	 */
	public void updateData(File file) {
		mFilesList.clear();
		mIsFileList.clear();
		checkFiles(file);
		mListViewAdpter.notifyDataSetChanged();
	}

	/**
	 * 遍历文件
	 * 
	 * @param file
	 */
	public void checkFiles(File file) {
		mFiles = file.listFiles();
		if(mFiles.length==0){
			Toast.makeText(this, "空文件夹", 0).show();
		}
		for (File f : mFiles) {
			mFilesList.add(f.getPath());
			if (f.isDirectory()) {
				mIsFileList.add(true);
			} else {
				mIsFileList.add(false);
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}