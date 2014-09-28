package com.ict.twitter.hbase;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Vector;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import com.ict.twitter.analyser.beans.TimeLine;

public abstract class TwitterHbase {
	Configuration conf=HbaseFactory.conf;
	String tableName=null;
	String[] familyNames;
	Map<String,String[]> columnsmap;
	SimpleDateFormat sdf;
	public TwitterHbase(String tableName){
		try {
			this.tableName=tableName;
			SetFamilyNameAndColumns();
			if(CheckTableExists(tableName)){
				
			}else{
				System.out.println("���ݿ������׼������");
				CreateTable(tableName);
			}
		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}catch(Exception e){
				e.printStackTrace();
		}
		sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
	}
	protected boolean CheckTableExists(String tablename) throws Exception{
		HBaseAdmin admin = new HBaseAdmin(conf);
		HTableDescriptor desc = new HTableDescriptor(tablename);
		boolean flag=false;
		if(admin.tableExists(tablename)){
			System.out.println("���ݿ���Ѿ�����");
			flag=true;
		}else{
			flag=false;
		}
		admin.close();
		return flag;
		
	}
	
	
	public boolean CreateTable(String tablename) throws Exception{
    	HBaseAdmin admin=null;
    	try{
    		admin = new HBaseAdmin(conf);
    		HTableDescriptor desc = new HTableDescriptor(tablename);
    		for (int i = 0; i < familyNames.length; i++) {
    			if(columnsmap.get(familyNames[i])==null){
    				 desc.addFamily(new HColumnDescriptor(familyNames[i]));
    			}else{
    				String[] columns=columnsmap.get(familyNames[i]);
    				HColumnDescriptor hcd=new HColumnDescriptor(familyNames[i]);
    				desc.addFamily(hcd);
    			}
               
            }
    		if(CheckTableExists(tablename)){
    			System.out.println("�������ݱ�֮ǰ��������ݿ���Ƿ��Ѿ�����");
    			admin.close();
    			return false;
    		}else{
    			admin.createTable(desc);
    		}
    		
    		admin.close();
    	}catch(MasterNotRunningException ex){
    		System.err.println("MASTER����ʧ��");
    		ex.printStackTrace();
    	}catch(ZooKeeperConnectionException ex){
    		System.err.println("ZK����ʧ��");
    		ex.printStackTrace();
    	}
    	catch(Exception ex){
    		ex.printStackTrace();
    	}finally{
    		admin.close();
    	}
		return false;
    	
    }
	protected abstract void SetFamilyNameAndColumns();
	

}
