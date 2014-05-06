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
	/** ��·�� **/
	private final String ROOTPATH = Environment.getExternalStorageDirectory().getPath();
	private ListView mFileListView;
	private File[] mFiles;
	private BrowseFileAdapter mListViewAdpter;
	/** ��һ·�� **/
	private String mBackPath = Environment.getExternalStorageDirectory().getPath();
	private Button mRootPathBtn;
	private Button mParentPathBtn;
	private Button mOkBtn;
	/** �ļ����ļ������ֵļ��� **/
	public ArrayList<Boolean> mIsFileList = new ArrayList<Boolean>();
	/** �ļ��б� **/
	public ArrayList<String> mFilesList = new ArrayList<String>();
	/** �������ļ� **/
	private ArrayList<String> mTransFilesList = new ArrayList<String>();
	private int mIndex = 0;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filelist);
		init();
	}

	/**
	 * ��ʼ��
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
					dialog.setTitle("����������ļ��б�");
					dialog.setMessage("ȷ�����?");
					
					dialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (!mTransFilesList.contains(mFiles[mIndex].getPath())) {
								mTransFilesList.add(mFiles[mIndex].getPath());
							}
							Toast.makeText(getBaseContext(), "�����", 0).show();
							ShowFileActivity.this.setTitle(mTransFilesList.size() + "���ļ�");
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
	 * ����adapter
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
	 * �����ļ�
	 * 
	 * @param file
	 */
	public void checkFiles(File file) {
		mFiles = file.listFiles();
		if(mFiles.length==0){
			Toast.makeText(this, "���ļ���", 0).show();
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