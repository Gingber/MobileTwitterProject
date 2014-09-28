package com.ict.monitor.bean;
import java.io.Serializable;


public class OutPutTask implements Serializable {
	
	private static final long serialVersionUID = 6631153939485579393L;
	public String taskName;
	public String taskType;
	public int action;//0:Insert
	
	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	public String getTaskType() {
		return taskType;
	}
	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}
	public int getAction() {
		return action;
	}
	public void setAction(int action) {
		this.action = action;
	}
	
	

}
