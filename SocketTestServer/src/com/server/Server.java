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
	//ServerSocket对象
	private ServerSocket mServerSocket;
	private Handler mHandler;
	//服务器端口
	private static final int SERVERPORT = 54321;
	//连接到得客户端
	private static List<Socket> mSocketList = Collections.synchronizedList(new ArrayList<Socket>());
	//线程池
	private ExecutorService mExecutorService;
	private final String TAG = "Server";

	// 消息来自server
	public static final int SERVER_MSG = 0;
	private boolean runFlag = true;
	private String mSavePath;
	private Resources mRes;

	/**
	 * 构造器
	 * 
	 * @param handler
	 */
	public Server(Handler handler,Resources res) {
		this.mRes=res;
		this.mHandler = handler;
		this.mSavePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "kdcDownload";

		//路径不存在则创建
		File saveDir = new File(mSavePath);
		if (!saveDir.exists()) {
			saveDir.mkdir();
		}
	}

	/**
	 * 启动服务
	 */
	public void startServer() {
		try {
			//设置服务器端口
			mServerSocket = new ServerSocket(SERVERPORT);
			Log.i(TAG, "建立ServerSocket");
		} catch (IOException e) {
			Log.i(TAG, R.string.app_name + "建立ServerSocket失败");
			e.printStackTrace();
		}
		//创建一个线程池
		mExecutorService = Executors.newCachedThreadPool();
		//启动等待客户端连接线程
		new Thread(new threadPoolRunnable()).start();
	}

	/**
	 * 此线程为每accept一个客户端socket就启动一个新的客户端线程
	 * 
	 * @author Administrator
	 * 
	 */
	private class threadPoolRunnable implements Runnable
	{
		@Override
		public void run() {
			//用来临时保存客户端连接的Socket对象
			Socket socket = null;
			while (runFlag) {
				try {
					if (mServerSocket != null) {
						Log.i(TAG, "wait client...");
						socket = mServerSocket.accept();
						Log.i(TAG, socket.toString() + "connect to server");
						//接收客户连接并添加到list中
						mSocketList.add(socket);
						//开启一个客户端线程
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
		System.out.println("关闭服务");
	}
}
