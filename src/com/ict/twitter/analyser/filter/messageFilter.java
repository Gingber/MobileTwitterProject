package com.ict.twitter.analyser.filter;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class messageFilter {
	String src1="11��8��8��, ���� 1:47";
	String src2="7:32 AM - 8 Aug 11";
	Pattern shang,xia;
	Pattern p1,p2;
	public messageFilter(){
		shang=Pattern.compile("['����'|'AM'|]{2}");
		xia=Pattern.compile("['����'|'PM'|]{2}");
		p1=Pattern.compile("([0-9]{2})��(\\d{1,2})��(\\d{1,2})��.*(\\d{1,2}):(\\d{1,2})");
		p2=Pattern.compile("([0-9]{1,2}):(\\d{2}).*(['AM'|'PM']{2}).*(\\d{1,2}).*([A-Za-z]{3}).*(\\d{2})");
	}
	
	public void doTransForm(String src){
		boolean isMorning=true;
		String trimsrc=src.trim();
		Matcher shangMatcher=shang.matcher(trimsrc);
		Matcher xiaMatcher=xia.matcher(trimsrc);
		if(shangMatcher.find()){
			isMorning=true;
		}else if(xiaMatcher.find()){
			isMorning=false;
		}else{
			System.out.println("ʱ��ƥ��ʧ��:"+trimsrc);
		}
		Matcher m1=p1.matcher(trimsrc);
		Matcher m2=p2.matcher(trimsrc);
		if(m1.find()){
			
		}else if(m2.find()){
			
		}else{
			System.out.println("ƥ��ʧ��:"+trimsrc);
		}



	}
	

}
