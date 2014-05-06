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
	/** �ļ��洢·�� **/
	private String mSavePath;//�洢·��
	private final String TAG = "ServerThread";
	private UpdateInfo mInfo;//���ȸ�����Ϣ
	private final String DIV_WEN = "?";//��Ϣ�ָ���
	private final String DIV_XING = "*";//��Ϣ�ָ���
	private final String DIV_DOLAR = "$";//��Ϣ�ָ���
	private final String FILE_FLAG = "�ļ�";//��Ϣ�ָ���
	private final int BUF_SIZE = 8192;//��������С
	private boolean mIsData = false;//��true�������ݣ���false�����ļ���Ϣ
	private Resources mRes;//��Դ

	public ServerThread(Handler handler, Socket socket, String path, List<Socket> list, Resources res) {
		System.out.println("�������߳�");
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
		//��ȡsocket��������
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

					if (!mIsData) {//�ļ���Ϣ
						String info = new String(buf, 0, length);
						System.out.println("�����ļ���Ϣ:" + info);
						if (info.substring(0, 2).equals(FILE_FLAG)) {
							int xingIndex = info.indexOf(DIV_XING);
							int wenIndex = info.indexOf(DIV_WEN);
							int dolarIndex = info.indexOf(DIV_DOLAR);
							fileName = info.substring(xingIndex + 1, wenIndex);
							//����ļ������
							fileOut = new BufferedOutputStream(new FileOutputStream(mSavePath + File.separator
									+ fileName));
							//�ļ�����
							String fileLen = info.substring(wenIndex + 1, dolarIndex);
							totalLen = new Long(fileLen).longValue();
							accLen = 0;
							System.out.println("�ļ���" + fileName + "   �ļ�����:" + fileLen);
							mInfo = new UpdateInfo();
							sendMsg(fileName, totalLen, accLen);
							/** ���� **/
							socketOut.println("has receive...." + info);
							socketOut.flush();
							mIsData = true;
						}
					}
					else {//����	
						accLen += length;
						System.out.println("��ȡ����");
						if (accLen > 0) {
							//System.out.println(new String(buf));
							fileOut.write(buf, 0, length);
							sendMsg(fileName, totalLen, accLen);
							/** ���� **/
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
				Log.i("���ӶϿ�(�쳣):", mSocket.toString());
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
				Log.i("���ӶϿ�(����):", mSocket.toString());
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
	 * ������ʾ
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
	 * ������ʾ
	 * 
	 * @param fileName
	 *        �ļ���
	 * @param totalLen
	 *        �ļ��ܳ���
	 * @param accLen
	 *        ���ճ���
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
