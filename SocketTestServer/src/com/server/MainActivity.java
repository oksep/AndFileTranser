package com.server;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity
{
	private TextView mServerText;
	private TextView mConnNumsText;
	private TextView mStatusText;
	private Server mServer;
	private ListView mListView;

	private AcceptFileAdapter mAdapter;
	/**�洢�±꼰��ʾ��Ϣ**/
	private HashMap<Integer, UpdateInfo> mInfoMap = new HashMap<Integer, UpdateInfo>();
	private ArrayList<Object> mInfoList = new ArrayList<Object>();
	private UpdateInfo mInfo;//��Ϣ��
	/**����������Ϣ**/
	public static final int MSG_CONN = 0;
	/**������Ϣ**/
	public static final int MSG_TRANS = 1;

	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_TRANS:
				mInfo = (UpdateInfo) msg.obj;
				if (mInfoList.size() == 0) {
					mInfoList.add(mInfo);
					mInfoMap.put(0, mInfo);
				} else {
					int itemIndex = mInfoList.indexOf(mInfo);
					if (itemIndex != -1) {
						mInfoMap.put(itemIndex, mInfo);
					} else {
						mInfoList.add(mInfo);
						mInfoMap.put(mInfoList.size() - 1, mInfo);
					}
				}
				mAdapter.notifyDataSetChanged();
				mStatusText.setText(getString(R.string.isreciving) + mInfo.getFileName() + "       "
						+ (int) ((double) mInfo.getAcceptLength() / mInfo.getTotalLength() * 100) + getString(R.string.percent));
				break;
			case MSG_CONN:
				mConnNumsText.setText(mServer.getConnNums());
				String s = (String) msg.obj;
				if (isWiFiActive(getBaseContext())) {
					mStatusText.setText(s);
				} else {
					mStatusText.setText(getString(R.string.neterror));
				}
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.server);
		initMethod();
	}

	/**
	 * ��ʼ��
	 */
	public void initMethod() {

		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new AcceptFileAdapter(this, mInfoMap);
		mListView.setAdapter(mAdapter);
		mServerText = (TextView) findViewById(R.id.info);
		mConnNumsText = (TextView) findViewById(R.id.connNum);
		mStatusText = (TextView) findViewById(R.id.status);

		if (!isWiFiActive(this)) {
			mServerText.setText(getString(R.string.wifiunable));
			mStatusText.setText(getString(R.string.neterror));
			displayToast(getString(R.string.wifiunable));
			mConnNumsText.setText(""+0);
		} else {//wifi���� ����������
			mStatusText.setText(R.string.waitconn);
			mServerText.setText(getIp());
			mServer = new Server(mHandler,getResources());
			mServer.startServer();
			mConnNumsText.setText(mServer.getConnNums());
		}
		
	}

	public void displayToast(String str) {
		Toast.makeText(this, str, 0).show();
	}

	/**
	 * �ж�wifi�����Ƿ����
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWiFiActive(Context inContext) {
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

	/**
	 * ��WifiManager��ȡIP
	 * 
	 * @return
	 */
	public String getIp() {

		//��ȡwifi����
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		//�ж�wifi�Ƿ���
		if (!wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(true);
		}
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		String ip = intToIp(ipAddress);
		return ip;

	}

	private String intToIp(int i) {
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			//�Ͽ�����
			if(mServer!=null){
				mServer.close();
			}		
		}
		return super.onKeyDown(keyCode, event);
	}

}