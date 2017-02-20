package com.common;

public class ThreadChannel {
	
		private static ThreadChannel threadChannel;
		
		static String message;
		
		private ThreadChannel(){}
	
		public static ThreadChannel getInstance(){			
			if(threadChannel == null)
				threadChannel = new ThreadChannel();
			return threadChannel;
		}
	
	   boolean flag = false;

	   public synchronized String getStatus() {
	    if (flag) {
	         try {
	            wait();
	         }catch (InterruptedException e) {
	            e.printStackTrace();
	         }
	      }
	      
	      System.out.println("ThreadChannel:getStatus =>:"+ message);
	      flag = true;
	      notify();
	      
	      return message;
	   }

	   public synchronized void setStatus(String msg) {
	      if (!flag) {
	         try {
	            wait();
	         }catch (InterruptedException e) {
	           e.printStackTrace();
	         }
	      }

	      message = msg;
	      
	      System.out.println("ThreadChannel:setStatus:"+ message);
	      
	      flag = false;
	      notify();
	   }
	   
 }	   