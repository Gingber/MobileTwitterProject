package com.iie.twitter.analyse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UTF8Filter {
	//static String  queryStr="[^\u4e00-\u9fa5\ufe00-\uff5a\u0040\u0000-\u00ff\u0800-\u4e00]";
	static String  queryStr="[^\u4e00-\u9fa5]";
	
	/*
	 * if all UTF8 return true;
	 * OR return false;
	 */
	public static boolean IsAllUTF8(String Str){
		Pattern p1 = Pattern.compile(queryStr);
		Matcher m = p1.matcher(Str);
		if(m.find()){
			return true;
		}else{
			return false;
		}
	}
	public static String CleanLeaveUTF8(String Str){
		Pattern p1 = Pattern.compile(queryStr);
		Matcher m = p1.matcher(Str);
		while(m.find()){
			Str=m.replaceAll("");
		}
		

//		
//		try {
//			System.out.println("OLD:"+bytesToHexString(OLD.getBytes("unicode")));
//			System.out.println("NEW:"+bytesToHexString(Str.getBytes("unicode")));
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		return Str;
	}
	
	public static void main(String[] args) throws Exception{
		String t="�Ї� �й� c";
		String t2=t;
		System.out.println("T2�ǣ�"+t2);
		System.out.println(bytesToHexString(t2.getBytes("unicode")));
		
		
		if(UTF8Filter.IsAllUTF8(t2)){
			System.out.println("���ڷǷ��ַ�");
		}else{
			System.out.println("ȫ���ַ���Ϊ��Ч�ַ�");
		}
		System.out.println("���Խ������");
		
		t2=CleanLeaveUTF8(t2);
		
		System.out.println("�����Ϻ�"+t2);
		
		if(UTF8Filter.IsAllUTF8(t2)){
			System.out.println("���ڷǷ��ַ�");
		}else{
			System.out.println("ȫ���ַ���Ϊ��Ч�ַ�");
			
		}
		
		//1:���������Ĵ�����ݡ�����¼���ڵ��ַ�����ֵ��
		//2:�������쳣�����Ҳ�������д���
		//3:�ٴγ��Դ�š�
		
		
	}
    public static final String bytesToHexString(byte[] bytes) {  
        StringBuilder sb = new StringBuilder(bytes.length * 2);  
        for (int i = 0; i < bytes.length; i++) {  
            String hex = Integer.toHexString(bytes[i] & 0xff);// &0xff��byteС��0ʱ���λ��1��Ҫ�Ļ�0  
            if (hex.length() == 1)  
                sb.append('0');  
            sb.append(hex);  
            sb.append(" ");  
        }  
        return sb.toString().toUpperCase();  
    }  

}
