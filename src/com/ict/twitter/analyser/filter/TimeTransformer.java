package com.ict.twitter.analyser.filter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TimeTransformer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public String NormTimeFormat(String create_time){
         String NormCreateTime = null;
         Date date = new Date();
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);  // ������ʾʱ���ʽ
         if (create_time.contains("����") | create_time.contains("����")) {
        	 String substr = create_time.substring(Math.max(create_time.length() - 2, 0)); // ��ȡβ���ַ�
        	 if (substr.contains("����") | substr.contains("����")) { //�ж��Ƿ�Ϊ"����"��"����"
        		 SimpleDateFormat cformatter = new SimpleDateFormat("yy��MM��dd��, hh:mm a", Locale.CHINA); // a��hh:mm����
                 // Parse the date
            	 try { 
              		date = cformatter.parse(create_time);	
              		//System.out.println("������ȷת������: " + sdf.format(date));
              		NormCreateTime = sdf.format(date);
              	} catch (ParseException e) {
              		e.printStackTrace();
              	}  
        	 } else {
        		 SimpleDateFormat cformatter = new SimpleDateFormat("yy��MM��dd��, a hh:mm", Locale.CHINA); // a��hh:mmǰ��
                 // Parse the date
            	 try { 
              		date = cformatter.parse(create_time);	
              		//System.out.println("������ȷת������: " + sdf.format(date));
              		NormCreateTime = sdf.format(date);
              	} catch (ParseException e) {
              		e.printStackTrace();
              	} 
        	 }
         } else { 
         	SimpleDateFormat eformatter = new SimpleDateFormat("hh:mm aa - dd MMM yy", Locale.ENGLISH); // ʱ����ʾ����
            // Parse the date
         	try {	 
         		date = eformatter.parse(create_time);
         		//System.out.println("Ӣ����ȷת������: " + sdf.format(date));
         		NormCreateTime = sdf.format(date);
         	} catch (ParseException e) {
         		e.printStackTrace();
         	}
         }
         
         return NormCreateTime;
     }

	public String Convert(String timelong){
		String template="1393439473";
		if(timelong.length()==template.length()){
			long time=Long.parseLong(timelong);
			Date date=new Date(time*1000);
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return sdf.format(date);
			
		}
		return null;
	}

}
