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
	private ListView mListView;//��ʾ�����ļ��б�
	private View mDialogView;//�Ի�����ͼ
	private ProgressDialog mDialog;

	private SendFileAdapter mAdapter;//�����ļ���������
	private final String TAG = "Client";
	/** �û�ѡ����ļ��б� **/
	private ArrayList<String> mFilesList = new ArrayList<String>();
	/** �洢adapter�е��±��ÿ��Ҫ��ʵ����Ϣ **/
	private HashMap<Integer, UpdateInfo> mMap = new HashMap<Integer, UpdateInfo>();
	private Socket mSocket;
	private final int PORT = 54321;//ͨ�ö˿�
	private final String LOCALIP = "127.0.0.1";//����IP
	private String mServerIp = "";//������IP
	private Boolean mIsConntcted = false;//trueΪ��������

	private final int CONN_SUCCESS = 0;//���ӳɹ�
	private final int CONN_FAIL = 1;//����ʧ��
	private final int CONN_DIALOG_SHOW = 2;//��ʾ���ӶԻ���
	private final int BREAK_CONN = 3;//�Ͽ�����
	private final int SEND_LEN = 4;//���ͳ���
	private final int TRANS_COMPLETED = 5;//�����ļ��������
	private final int TRANS_COMPLETED_ALL = 6;//�����б����ļ��������
	private final String DIV_WEN = "?";//�ָ��� ��
	private final String DIV_XING = "*";//�ָ��� *
	private final String DIV_DOLAR = "$";//�ָ��� $
	private final String DIV_FILE = "�ļ�";//�ָ��� �ļ�
	private boolean mIsWifiAble = false;//true wifi����
	private final int BUF_SIZE = 8192;//��������С

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
		// ���������
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.main);
		initMethod();
	}

	/**
	 * ��ʼ��
	 */
	public void initMethod() {
		mIsWifiAble = isWiFiAvailable(getBaseContext());
		mTextView = (TextView) findViewById(R.id.txtView);
		mTextView.setText(mIsWifiAble ? getString(R.string.wifiable) : getString(R.string.wifiunable));
		mScrollView = (ScrollView) findViewById(R.id.mSv);
		mConnectServerBtn = (Button) findViewById(R.id.btn_conn);
		//���ӷ����������Ի���
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
		//�����ļ�ִ��һ���߳�
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
		//����ļ���תҳ��
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
	 * �����Ͽ�����
	 */
	private void breakConn() {
		if (mSocket == null) {
			return;
		}
		try {
			mSocket.close();
			sendMsg(BREAK_CONN);
			System.out.println("�Ͽ�����");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ѡ���ļ���ɷ����ļ��б�
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
	 * ���ӷ�����
	 */
	public void connectServer(String ip) {
		sendMsg(CONN_DIALOG_SHOW);
		try {
			//�����������ó�ʱ
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
	 * ������������ļ�
	 * 
	 * @throws Exception
	 */
	public void sendFile() {
		BufferedInputStream fileIn = null;
		//socket�����
		BufferedOutputStream socketOut = null;
		//socket������
		BufferedReader socketIn = null;
		//��ȡsocket����/�����
		try {
			socketOut = new BufferedOutputStream(mSocket.getOutputStream());
			socketIn = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
		} catch (IOException e) {
			Log.e("IOException", e.toString());
		}
		/** �����б����ļ� **/
		for (int i = 0; i < mFilesList.size(); i++) {

			System.out.println("send:   " + mFilesList.get(i));
			String filePath = mFilesList.get(i);
			File file = new File(filePath);
			String fileName = file.getName();
			long fileLen = file.length();

			//��ȡ�ļ���������
			try {
				fileIn = new BufferedInputStream(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				Log.e("FileNotFoundException", e.toString());
			}

			//�ļ�*fNa?fLen*
			String bufFileString = DIV_FILE + DIV_XING + fileName + DIV_WEN + fileLen + DIV_DOLAR;
			byte[] fileInfoBuf = bufFileString.getBytes();
			try {
				/** �����ļ���Ϣ **/
				socketOut.write(fileInfoBuf);
				socketOut.flush();
				System.out.println("�����ļ���Ϣ:" + fileName + "�ȴ�����");
				serverBack(socketIn);
				/** �����ļ����� **/
				System.out.println("�����ļ�����...");
				int length = 0;
				byte[] buf = new byte[BUF_SIZE];
				long sendLen = 0;
				//��ȡ�ļ����������ݲ�д���������
				while ((length = fileIn.read(buf)) != -1) {
					System.out.println("���ͳ���" + length);
					socketOut.write(buf, 0, length);
					socketOut.flush();
					sendLen += length;
					sendMsg(SEND_LEN, fileLen, sendLen, i, fileName);
					serverBack(socketIn);
				}
				serverBack(socketIn);
				System.out.println(fileName + "�������");
				sendMsg(TRANS_COMPLETED, fileName);
				//�ر�file������
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
	 * �ȴ�����������
	 * 
	 * @param socketIn
	 * @throws IOException
	 */
	public void serverBack(BufferedReader socketIn) throws IOException {
		String str = socketIn.readLine();
		if (str == null) {
			System.out.println("������Ϣ:��");
			return;
		}
		System.out.println("������Ϣ:" + str);
	}

	/**
	 * ������Ϣ
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
	 * ������Ϣ
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
	 * �ж�wifi�����Ƿ����
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
