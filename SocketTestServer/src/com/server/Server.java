package com.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.res.Resources;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Server
{
	//ServerSocket����
	private ServerSocket mServerSocket;
	private Handler mHandler;
	//�������˿�
	private static final int SERVERPORT = 54321;
	//���ӵ��ÿͻ���
	private static List<Socket> mSocketList = Collections.synchronizedList(new ArrayList<Socket>());
	//�̳߳�
	private ExecutorService mExecutorService;
	private final String TAG = "Server";

	// ��Ϣ����server
	public static final int SERVER_MSG = 0;
	private boolean runFlag = true;
	private String mSavePath;
	private Resources mRes;

	/**
	 * ������
	 * 
	 * @param handler
	 */
	public Server(Handler handler,Resources res) {
		this.mRes=res;
		this.mHandler = handler;
		this.mSavePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "kdcDownload";

		//·���������򴴽�
		File saveDir = new File(mSavePath);
		if (!saveDir.exists()) {
			saveDir.mkdir();
		}
	}

	/**
	 * ��������
	 */
	public void startServer() {
		try {
			//���÷������˿�
			mServerSocket = new ServerSocket(SERVERPORT);
			Log.i(TAG, "����ServerSocket");
		} catch (IOException e) {
			Log.i(TAG, R.string.app_name + "����ServerSocketʧ��");
			e.printStackTrace();
		}
		//����һ���̳߳�
		mExecutorService = Executors.newCachedThreadPool();
		//�����ȴ��ͻ��������߳�
		new Thread(new threadPoolRunnable()).start();
	}

	/**
	 * ���߳�Ϊÿacceptһ���ͻ���socket������һ���µĿͻ����߳�
	 * 
	 * @author Administrator
	 * 
	 */
	private class threadPoolRunnable implements Runnable
	{
		@Override
		public void run() {
			//������ʱ����ͻ������ӵ�Socket����
			Socket socket = null;
			while (runFlag) {
				try {
					if (mServerSocket != null) {
						Log.i(TAG, "wait client...");
						socket = mServerSocket.accept();
						Log.i(TAG, socket.toString() + "connect to server");
						//���տͻ����Ӳ���ӵ�list��
						mSocketList.add(socket);
						//����һ���ͻ����߳�
						mExecutorService.execute(new ServerThread(mHandler, socket, mSavePath, mSocketList,mRes));
						sendMsg(socket.getInetAddress().toString() + mRes.getString(R.string.join));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public String getConnNums() {
		return "" + mSocketList.size();
	}

	private void sendMsg(String str) {
		Message msg = new Message();
		msg.obj = str;
		msg.what = MainActivity.MSG_CONN;
		mHandler.sendMessage(msg);
	}

	public void close() {
		runFlag = false;

		try {
			if (mServerSocket != null) {
				mServerSocket.close();
				mServerSocket = null;
			}
			for (Socket s : mSocketList) {
				s.close();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mSocketList.clear();
		if (mExecutorService != null) {
			mExecutorService.shutdown();
			mExecutorService = null;
		}
		System.out.println("�رշ���");
	}
}
