package com.server;


public class UpdateInfo
{
	private String mIp="";
	private String mFileName="";
	private long mTotalLength=0;
	private long mAcceptLength=0;

	public long getTotalLength() {
		return mTotalLength;
	}

	public void setTotalLength(long totalLength) {
		this.mTotalLength = totalLength;
	}

	public long getAcceptLength() {
		return mAcceptLength;
	}

	public void setAcceptLength(long acceptLength) {
		this.mAcceptLength = acceptLength;
	}

	public String getIp() {
		return mIp;
	}

	public void setIp(String ip) {
		this.mIp = ip;
	}

	public String getFileName() {
		return mFileName;
	}

	public void setFileName(String fileName) {
		this.mFileName = fileName;
	}

}
