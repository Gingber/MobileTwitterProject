package com.ict.twitter.CrawlerNode;

import java.util.concurrent.ExecutorService; 
import java.util.concurrent.Executors; 

/** 
* Java�̣߳������������� 
* 
* @author leizhimin 2009-11-5 10:57:29 
*/ 
public class Test { 
        public static void main(String[] args) { 
                //�����������ʵ��˻� 
                MyCount myCount = new MyCount("95599200901215522", 0); 
                //����һ���̳߳� 
                ExecutorService pool = Executors.newFixedThreadPool(2); 
                Thread t1 = new SaveThread("����", myCount, 2000); 
                Thread t2 = new SaveThread("����", myCount, 3600); 
                Thread t3 = new DrawThread("����", myCount, 5600); 
                Thread t4 = new SaveThread("����", myCount, 600); 
                Thread t5 = new DrawThread("��ţ", myCount, 1300); 
                Thread t6 = new DrawThread("����", myCount, 800); 
                Thread t7 = new SaveThread("����", myCount, 600); 
                //ִ�и����߳� 
                pool.execute(t1); 
                pool.execute(t2); 
                pool.execute(t3); 
                pool.execute(t4); 
                pool.execute(t5); 
                pool.execute(t6);
                pool.execute(t7); 
                //�ر��̳߳� 
                pool.shutdown(); 
        } 
} 

/** 
* ����߳��� 
*/ 
class SaveThread extends Thread { 
        private String name;                //������ 
        private MyCount myCount;        //�˻� 
        private int x;                            //����� 

        SaveThread(String name, MyCount myCount, int x) { 
                this.name = name; 
                this.myCount = myCount; 
                this.x = x; 
        } 

        public void run() { 
                myCount.saving(x, name); 
        } 
} 

/** 
* ȡ���߳��� 
*/ 
class DrawThread extends Thread { 
        private String name;                //������ 
        private MyCount myCount;        //�˻� 
        private int x;                            //����� 

        DrawThread(String name, MyCount myCount, int x) { 
                this.name = name; 
                this.myCount = myCount; 
                this.x = x; 
        } 

        public void run() { 
                myCount.drawing(x, name); 
        } 
} 


/** 
* ��ͨ�����˻�������͸֧ 
*/ 
class MyCount { 
        private int cash;                             //�˻���� 

        MyCount(String oid, int cash) { 
                this.cash = cash; 
        } 

        /** 
         * ��� 
         * 
         * @param x        ������� 
         * @param name ������ 
         */ 
        public synchronized void saving(int x, String name) { 
                if (x > 0) { 
                        cash += x;                    //��� 
                        System.out.println(name + "���" + x + "����ǰ���Ϊ" + cash); 
                } 
                notifyAll();            //�������еȴ��̡߳� 
        } 

        /** 
         * ȡ�� 
         * 
         * @param x        ������� 
         * @param name ������ 
         */ 
        public synchronized void drawing(int x, String name) { 
                if (cash - x < 0) { 
                        try { 
                                wait(); 
                        } catch (InterruptedException e1) { 
                                e1.printStackTrace(); 
                        } 
                } else { 
                        cash -= x;                     //ȡ�� 
                        System.out.println(name + "ȡ��" + x + "����ǰ���Ϊ" + cash); 
                } 
                notifyAll();             //�������д����� 
        } 
}