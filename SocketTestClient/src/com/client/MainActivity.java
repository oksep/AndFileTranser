package com.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity
{

	private TextView mTextView;
	private ScrollView mScrollView;
	private Button mConnectServerBtn;
	private Button mSendFileBtn;
	private Button mBrowseFilesBtn;
	private Button mBreakBtn;
	private ListView mListView;//显示发送文件列表
	private View mDialogView;//对话框视图
	private ProgressDialog mDialog;

	private SendFileAdapter mAdapter;//发送文件的适配器
	private final String TAG = "Client";
	/** 用户选择的文件列表 **/
	private ArrayList<String> mFilesList = new ArrayList<String>();
	/** 存储adapter中的下标和每项要现实的信息 **/
	private HashMap<Integer, UpdateInfo> mMap = new HashMap<Integer, UpdateInfo>();
	private Socket mSocket;
	private final int PORT = 54321;//通用端口
	private final String LOCALIP = "127.0.0.1";//本地IP
	private String mServerIp = "";//服务器IP
	private Boolean mIsConntcted = false;//true为连接正常

	private final int CONN_SUCCESS = 0;//连接成功
	private final int CONN_FAIL = 1;//连接失败
	private final int CONN_DIALOG_SHOW = 2;//显示连接对话框
	private final int BREAK_CONN = 3;//断开连接
	private final int SEND_LEN = 4;//发送长度
	private final int TRANS_COMPLETED = 5;//单个文件发送完成
	private final int TRANS_COMPLETED_ALL = 6;//所有列表内文件发送完成
	private final String DIV_WEN = "?";//分隔符 ？
	private final String DIV_XING = "*";//分隔符 *
	private final String DIV_DOLAR = "$";//分隔符 $
	private final String DIV_FILE = "文件";//分隔符 文件
	private boolean mIsWifiAble = false;//true wifi可用
	private final int BUF_SIZE = 8192;//缓冲区大小

	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CONN_SUCCESS:
				mIsConntcted = true;
				mDialog.dismiss();
				displayToast(getString(R.string.connsuccess));
				if (mServerIp.equals("")) {
					mTextView.append("\n" + getString(R.string.connlocal));
				} else {
					mTextView.append("\n" + getString(R.string.connserver) + mServerIp);
				}
				mConnectServerBtn.setVisibility(View.GONE);
				mBrowseFilesBtn.setVisibility(View.VISIBLE);
				mBreakBtn.setVisibility(View.VISIBLE);
				mScrollView.smoothScrollBy(0, mTextView.getHeight());
				break;
			case CONN_FAIL:
				mDialog.dismiss();
				displayToast(getString(R.string.connfailed));
				mTextView.append("\n" + getString(R.string.connfailed));
				mScrollView.scrollTo(0, mTextView.getHeight());
				break;
			case CONN_DIALOG_SHOW:
				mDialog.show();
				break;
			case BREAK_CONN:
				mTextView.append("\n" + getString(R.string.connbreak));
				mScrollView.smoothScrollBy(0, mTextView.getHeight());
				displayToast(getString(R.string.connbreak));
				mConnectServerBtn.setVisibility(View.VISIBLE);
				mSendFileBtn.setVisibility(View.GONE);
				mBrowseFilesBtn.setVisibility(View.GONE);
				mBreakBtn.setVisibility(View.GONE);
				mMap.clear();
				mFilesList.clear();
				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}
				break;
			case SEND_LEN:
				mMap.put(msg.arg1, (UpdateInfo) msg.obj);
				mAdapter.notifyDataSetChanged();
				break;
			case TRANS_COMPLETED:
				String str = (String) msg.obj;
				mTextView.append("\n" + str + getString(R.string.transcomplete));
				mScrollView.scrollTo(0, mTextView.getHeight());
				break;
			case TRANS_COMPLETED_ALL:
				mBrowseFilesBtn.setVisibility(View.VISIBLE);
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		getString(R.string.connsuccess);
		super.onCreate(savedInstanceState);
		// 隐藏软键盘
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.main);
		initMethod();
	}

	/**
	 * 初始化
	 */
	public void initMethod() {
		mIsWifiAble = isWiFiAvailable(getBaseContext());
		mTextView = (TextView) findViewById(R.id.txtView);
		mTextView.setText(mIsWifiAble ? getString(R.string.wifiable) : getString(R.string.wifiunable));
		mScrollView = (ScrollView) findViewById(R.id.mSv);
		mConnectServerBtn = (Button) findViewById(R.id.btn_conn);
		//连接服务器弹出对话框
		mConnectServerBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mIsWifiAble) {
					displayToast(getString(R.string.checknet));
					return;
				} else {
					AlertDialog.Builder dialog_ip = new AlertDialog.Builder(MainActivity.this);
					dialog_ip.setTitle(getString(R.string.connsuccess));
					mDialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_ip, null);
					dialog_ip.setView(mDialogView);
					dialog_ip.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

							EditText editIp = (EditText) mDialogView.findViewById(R.id.serverIpText);
							mServerIp = editIp.getText().toString();

							if (!TextUtils.isEmpty(mServerIp)) {
								Log.i(TAG, mServerIp);
								new Thread(new Runnable() {
									@Override
									public void run() {
										connectServer(mServerIp);
									}
								}).start();

							} else {
								Log.i(TAG, LOCALIP);
								new Thread(new Runnable() {
									@Override
									public void run() {
										connectServer(LOCALIP);
									}
								}).start();
							}

						}
					});
					dialog_ip.create();
					dialog_ip.show();
				}
			}
		});

		mBreakBtn = (Button) findViewById(R.id.btn_break);
		mBreakBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				breakConn();
			}
		});

		mSendFileBtn = (Button) findViewById(R.id.btn_send);
		//发送文件执行一个线程
		mSendFileBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mIsConntcted) {
					displayToast(getString(R.string.unconn));
					return;
				}
				mSendFileBtn.setVisibility(View.GONE);
				mBrowseFilesBtn.setVisibility(View.GONE);
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							sendFile();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
		});

		mBrowseFilesBtn = (Button) findViewById(R.id.browseFiles);
		//浏览文件跳转页面
		mBrowseFilesBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, ShowFileActivity.class);
				startActivityForResult(intent, 0);
			}
		});

		mDialog = new ProgressDialog(this);
		mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mDialog.setTitle(getString(R.string.wait));
		mDialog.setMessage(getString(R.string.conning));
		mDialog.setCancelable(false);
		mDialog.setIndeterminate(false);
		mListView = (ListView) findViewById(R.id.listView);
	}

	/**
	 * 主动断开连接
	 */
	private void breakConn() {
		if (mSocket == null) {
			return;
		}
		try {
			mSocket.close();
			sendMsg(BREAK_CONN);
			System.out.println("断开连接");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 选择文件完成返回文件列表
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mFilesList = data.getStringArrayListExtra("files");
		if (mFilesList.size() == 0) {
			mListView.setAdapter(null);
			return;
		}
		mSendFileBtn.setVisibility(View.VISIBLE);
		for (int i = 0; i < mFilesList.size(); i++) {
			UpdateInfo info = new UpdateInfo();
			mMap.put(i, info);
		}
		mAdapter = new SendFileAdapter(this, mFilesList, mMap);
		mListView.setAdapter(mAdapter);
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 连接服务器
	 */
	public void connectServer(String ip) {
		sendMsg(CONN_DIALOG_SHOW);
		try {
			//创建连接设置超时
			InetAddress addr = Inet4Address.getByName(ip);
			mSocket = new Socket();
			mSocket.connect(new InetSocketAddress(addr, PORT), 4000);
			sendMsg(CONN_SUCCESS);
		} catch (Exception e) {
			sendMsg(CONN_FAIL);
			e.printStackTrace();
		}
	}

	/**
	 * 向服务器发送文件
	 * 
	 * @throws Exception
	 */
	public void sendFile() {
		BufferedInputStream fileIn = null;
		//socket输出流
		BufferedOutputStream socketOut = null;
		//socket输入流
		BufferedReader socketIn = null;
		//获取socket输入/输出流
		try {
			socketOut = new BufferedOutputStream(mSocket.getOutputStream());
			socketIn = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
		} catch (IOException e) {
			Log.e("IOException", e.toString());
		}
		/** 遍历列表发送文件 **/
		for (int i = 0; i < mFilesList.size(); i++) {

			System.out.println("send:   " + mFilesList.get(i));
			String filePath = mFilesList.get(i);
			File file = new File(filePath);
			String fileName = file.getName();
			long fileLen = file.length();

			//获取文件的输入流
			try {
				fileIn = new BufferedInputStream(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				Log.e("FileNotFoundException", e.toString());
			}

			//文件*fNa?fLen*
			String bufFileString = DIV_FILE + DIV_XING + fileName + DIV_WEN + fileLen + DIV_DOLAR;
			byte[] fileInfoBuf = bufFileString.getBytes();
			try {
				/** 发送文件信息 **/
				socketOut.write(fileInfoBuf);
				socketOut.flush();
				System.out.println("发送文件信息:" + fileName + "等待反馈");
				serverBack(socketIn);
				/** 发送文件数据 **/
				System.out.println("发送文件数据...");
				int length = 0;
				byte[] buf = new byte[BUF_SIZE];
				long sendLen = 0;
				//读取文件输入流数据并写入输出流中
				while ((length = fileIn.read(buf)) != -1) {
					System.out.println("发送长度" + length);
					socketOut.write(buf, 0, length);
					socketOut.flush();
					sendLen += length;
					sendMsg(SEND_LEN, fileLen, sendLen, i, fileName);
					serverBack(socketIn);
				}
				serverBack(socketIn);
				System.out.println(fileName + "发送完成");
				sendMsg(TRANS_COMPLETED, fileName);
				//关闭file输入流
				try {
					fileIn.close();
					fileIn = null;
				} catch (IOException e) {
					Log.e("IOException", e.toString());
				}
			} catch (IOException e1) {
				sendMsg(BREAK_CONN);
				Log.e("IOException", e1.toString());
				return;
			} catch (Exception e) {
				sendMsg(BREAK_CONN);
				Log.e("Exception", e.toString());
				return;
			}
		}
		sendMsg(TRANS_COMPLETED_ALL);

	}

	/**
	 * 等待服务器反馈
	 * 
	 * @param socketIn
	 * @throws IOException
	 */
	public void serverBack(BufferedReader socketIn) throws IOException {
		String str = socketIn.readLine();
		if (str == null) {
			System.out.println("反馈信息:空");
			return;
		}
		System.out.println("反馈信息:" + str);
	}

	/**
	 * 发送消息
	 */
	private void sendMsg(int tag, long fileLen, long sendLen, int index, String fileName) {
		UpdateInfo info = new UpdateInfo();
		info.setSendLength(sendLen);
		info.setTotalLength(fileLen);
		info.setFileName(fileName);
		Message msg = new Message();
		msg.arg1 = index;
		msg.what = tag;
		msg.obj = info;
		mHandler.sendMessage(msg);
	}

	public void sendMsg(int tag, String str) {
		Message msg = new Message();
		msg.obj = str;
		msg.what = tag;
		mHandler.sendMessage(msg);
	}

	/**
	 * 发送消息
	 */
	public void sendMsg(int tag) {
		Message msg = new Message();
		msg.what = tag;
		mHandler.sendMessage(msg);
	}

	public void displayToast(String info) {
		Toast.makeText(this, info, 0).show();
	}

	/**
	 * 判断wifi网络是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWiFiAvailable(Context inContext) {
		Context context = inContext.getApplicationContext();
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getTypeName().equals("WIFI") && info[i].isConnected()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mIsConntcted) {
				breakConn();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

}
