package com.ict.twitter.plantform;

import java.util.TimerTask;

public class TotalCountShowSchedule extends TimerTask{
	ConsoleShowBean consoleBean;
	public TotalCountShowSchedule(ConsoleShowBean consoleBean){
		this.consoleBean=consoleBean;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		//Ŀǰֻ�㱨��TwitterWEB�Ĵ�С
		int [] result=consoleBean.dbCounter.countTwitterWEB();
		consoleBean.TwitterAPIShow(result);
		System.out.println("�㱨��ǰTwitterWEB�Ĵ�С");
		for(int i=0;i<result.length;i++){
			System.out.print("\t"+result[i]);
		}
		System.out.println("�㱨��ǰTwitterWEB��С����");
	}

}
