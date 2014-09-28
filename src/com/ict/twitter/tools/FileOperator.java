package com.ict.twitter.tools;

import java.io.File;
import java.io.IOException;

public class FileOperator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {		
		System.out.println(isPathExist("c:/shan"));
	}
	public static boolean isPathExist(String filePath){
		File f=new File(filePath);
		return f.exists();
		
	}
	public static boolean createFileOrDir(String filePath){
		File f=new File(filePath);
		if(f.isDirectory()&&!f.exists()){
			f.mkdirs();
		}else if(!f.exists()){
			String parent=f.getParent();
			File parentFile=new File(parent);
			System.out.println("��Ŀ¼��·����"+parentFile);
			
			parentFile.mkdirs();
			System.out.println("ȫ��·����"+f.getAbsolutePath());
			try {
				if(!f.getName().contains(".")){
					f.mkdir();
				}else{
					f.createNewFile();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("�ļ�����ʧ��");
				e.printStackTrace();
			}			
		}
		
		return true;
	}
	public static int FileOrDictory(File f){
        if(f.exists()){        
	        if(f.isDirectory()){
	        	System.out.println("���ļ���");
	        	return 2;
	        }else if(f.exists()&&!f.isDirectory()){
	        	System.out.println("�����ļ���");
	        	return 1;
	        }else{
	        	System.out.println("�ļ�������");
	        	return 0;
	        }
        }else{
        	System.out.println("�ļ�������");
        	return 0;
        }

	}

}
