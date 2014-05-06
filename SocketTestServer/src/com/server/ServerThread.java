package com.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ServerThread implements Runnable
{

	private Handler mHandler = null;
	private Socket mSocket = null;
	private List<Socket> mSocketList = null;
	/** 文件存储路径 **/
	private String mSavePath;//存储路径
	private final String TAG = "ServerThread";
	private UpdateInfo mInfo;//进度更新信息
	private final String DIV_WEN = "?";//信息分隔符
	private final String DIV_XING = "*";//信息分隔符
	private final String DIV_DOLAR = "$";//信息分隔符
	private final String FILE_FLAG = "文件";//信息分隔符
	private final int BUF_SIZE = 8192;//缓冲区大小
	private boolean mIsData = false;//是true接收数据，是false接收文件信息
	private Resources mRes;//资源

	public ServerThread(Handler handler, Socket socket, String path, List<Socket> list, Resources res) {
		System.out.println("开启新线程");
		this.mHandler = handler;
		this.mSocket = socket;
		this.mSavePath = path;
		this.mSocketList = list;
		this.mRes = res;
	}

	@Override
	public void run() {

		boolean flag = true;
		BufferedInputStream socketIn = null;
		BufferedOutputStream fileOut = null;
		PrintWriter socketOut = null;
		//获取socket的输入流
		try {
			socketIn = new BufferedInputStream(mSocket.getInputStream());
			socketOut = new PrintWriter(mSocket.getOutputStream());
		} catch (IOException e) {
			Log.e(TAG, "IOException" + e);
		}

		while (flag) {
			byte[] buf = new byte[BUF_SIZE];
			int length = 0;
			long totalLen = 0;
			long accLen = 0;
			String fileName = "";
			try {
				while ((length = socketIn.read(buf)) != -1) {

					if (!mIsData) {//文件信息
						String info = new String(buf, 0, length);
						System.out.println("接收文件信息:" + info);
						if (info.substring(0, 2).equals(FILE_FLAG)) {
							int xingIndex = info.indexOf(DIV_XING);
							int wenIndex = info.indexOf(DIV_WEN);
							int dolarIndex = info.indexOf(DIV_DOLAR);
							fileName = info.substring(xingIndex + 1, wenIndex);
							//获得文件输出流
							fileOut = new BufferedOutputStream(new FileOutputStream(mSavePath + File.separator
									+ fileName));
							//文件长度
							String fileLen = info.substring(wenIndex + 1, dolarIndex);
							totalLen = new Long(fileLen).longValue();
							accLen = 0;
							System.out.println("文件名" + fileName + "   文件长度:" + fileLen);
							mInfo = new UpdateInfo();
							sendMsg(fileName, totalLen, accLen);
							/** 反馈 **/
							socketOut.println("has receive...." + info);
							socketOut.flush();
							mIsData = true;
						}
					}
					else {//数据	
						accLen += length;
						System.out.println("读取数据");
						if (accLen > 0) {
							//System.out.println(new String(buf));
							fileOut.write(buf, 0, length);
							sendMsg(fileName, totalLen, accLen);
							/** 反馈 **/
							if (accLen % BUF_SIZE == 0 || accLen == totalLen) {
								socketOut.println("has receive...." + accLen);
								socketOut.flush();
							}
							if (accLen == totalLen) {
								socketOut.println("receive completed:" + fileName);
								socketOut.flush();
								fileOut.close();
								mIsData = false;
								sendMsg(fileName + mRes.getString(R.string.acccomplete));
								break;
							}
						}
					}
				}
			} catch (IOException e) {
				Log.i("连接断开(异常):", mSocket.toString());
				mSocketList.remove(mSocket);
				String str = fileName.equals("") ? fileName + mRes.getString(R.string.connfailed) : "";
				File file = new File(mSavePath + File.separator + fileName);
				file.delete();
				sendMsg(mSocket.getInetAddress().toString() + mRes.getString(R.string.connbreak) + str);
				flag = false;
				break;
			} catch (NumberFormatException e) {
				Log.e("NumberFormatException", e.toString());
			} catch (NullPointerException e) {
				Log.e("NullPointerException", e.toString());
			} catch (Exception e) {
				Log.e("Exception", e.toString());
			}

			if (length == -1) {
				Log.i("连接断开(主动):", mSocket.toString());
				mSocketList.remove(mSocket);
				String str = fileName.equals("") ? "" : fileName + mRes.getString(R.string.connfailed);
				File file = new File(mSavePath + File.separator + fileName);
				file.delete();
				sendMsg(mSocket.getInetAddress().toString() + mRes.getString(R.string.connbreak) + str);
				flag = false;
				break;
			}

		}
	}

	/**
	 * 网络提示
	 * 
	 * @param str
	 */
	private void sendMsg(String str) {
		Message msg = new Message();
		msg.obj = str;
		msg.what = MainActivity.MSG_CONN;
		mHandler.sendMessage(msg);
	}

	/**
	 * 进度提示
	 * 
	 * @param fileName
	 *        文件名
	 * @param totalLen
	 *        文件总长度
	 * @param accLen
	 *        接收长度
	 */
	private void sendMsg(String fileName, long totalLen, long accLen) {
		mInfo.setAcceptLength(accLen);
		mInfo.setFileName(fileName);
		mInfo.setTotalLength(totalLen);
		mInfo.setIp(mSocket.getInetAddress().toString());
		Message msg = new Message();
		msg.obj = mInfo;
		msg.what = MainActivity.MSG_TRANS;
		mHandler.sendMessage(msg);
	}
}
