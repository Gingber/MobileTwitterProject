package com.ict.monitor.bean;

import java.sql.Timestamp;

public class TWCrawlTask {
	int taskID;

	boolean taskType;
	String taskName;
	boolean needProxy;
	String taskCmd;
	String taskParams;
	int logMsgsFreq;
	String taskState;
	Timestamp startTime;
	Timestamp stopTime;
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (needProxy ? 1231 : 1237);
		result = prime * result + ((taskCmd == null) ? 0 : taskCmd.hashCode());
		result = prime * result + taskID;
		result = prime * result
				+ ((taskName == null) ? 0 : taskName.hashCode());
		result = prime * result
				+ ((taskParams == null) ? 0 : taskParams.hashCode());
		result = prime * result + (taskType ? 1231 : 1237);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TWCrawlTask other = (TWCrawlTask) obj;
		if (needProxy != other.needProxy) {
			return false;
		}
		if (taskCmd == null) {
			if (other.taskCmd != null) {
				return false;
			}
		} else if (!taskCmd.equals(other.taskCmd)) {
			return false;
		}
		if (taskID != other.taskID) {
			return false;
		}
		if (taskName == null) {
			if (other.taskName != null) {
				return false;
			}
		} else if (!taskName.equals(other.taskName)) {
			return false;
		}
		if (taskParams == null) {
			if (other.taskParams != null) {
				return false;
			}
		} else if (!taskParams.equals(other.taskParams)) {
			return false;
		}
		if (taskType != other.taskType) {
			return false;
		}
		return true;
	}
	public boolean isTaskType() {
		return taskType;
	}
	public void setTaskType(boolean taskType) {
		this.taskType = taskType;
	}
	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	public boolean isNeedProxy() {
		return needProxy;
	}
	public void setNeedProxy(boolean needProxy) {
		this.needProxy = needProxy;
	}
	public String getTaskCmd() {
		return taskCmd;
	}
	public void setTaskCmd(String taskCmd) {
		this.taskCmd = taskCmd;
	}
	public String getTaskParams() {
		return taskParams;
	}
	public void setTaskParams(String taskParams) {
		this.taskParams = taskParams;
	}
	public String getTaskState() {
		return taskState;
	}
	public void setTaskState(String taskState) {
		this.taskState = taskState;
	}
	public Timestamp getStartTime() {
		return startTime;
	}
	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}
	public int getLogMsgsFreq() {
		return logMsgsFreq;
	}
	public void setLogMsgsFreq(int logMsgsFreq) {
		this.logMsgsFreq = logMsgsFreq;
	}
	public Timestamp getStopTime() {
		return stopTime;
	}
	public void setStopTime(Timestamp stopTime) {
		this.stopTime = stopTime;
	}
	public int getTaskID() {
		return taskID;
	}
	public void setTaskID(int taskID) {
		this.taskID = taskID;
	}

	

}
