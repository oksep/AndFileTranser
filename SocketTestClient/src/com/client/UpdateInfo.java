package com.client;

public class UpdateInfo
{
	private String mFileName = "";
	private long mTotalLength = 0;
	private long mSendLength = 0;

	public String getFileName() {
		return mFileName;
	}

	public void setFileName(String fileName) {
		this.mFileName = fileName;
	}

	public long getTotalLength() {
		return mTotalLength;
	}

	public void setTotalLength(long totalLength) {
		this.mTotalLength = totalLength;
	}

	public long getSendLength() {
		return mSendLength;
	}

	public void setSendLength(long sendLength) {
		this.mSendLength = sendLength;
	}



}
