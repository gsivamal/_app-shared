package com.common;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.*;
import javax.mail.internet.*;

public class EmailService {
	
	public static void send(String to, String subject, String message){
		try {
		  //Get the session object  
		  Properties props = new Properties();  
		  props.put("mail.smtp.host", "smtp.gmail.com");  
		  props.put("mail.smtp.socketFactory.port", "465");  
		  props.put("mail.smtp.socketFactory.class",  
		            "javax.net.ssl.SSLSocketFactory");  
		  props.put("mail.smtp.auth", "true");  
		  props.put("mail.smtp.port", "465");  
		   
		  //TODO: make it OAUTH based later, avoid using password here
		  Session session = Session.getDefaultInstance(props,  
			   new javax.mail.Authenticator() {  
				   protected PasswordAuthentication getPasswordAuthentication() {  
				   return new PasswordAuthentication("gsivamal@gmail.com","Marina@75063");//change accordingly  
			   }  
		  });  
		   
		  //compose message  
		   MimeMessage mime = new MimeMessage(session);  
			   mime.setFrom(new InternetAddress("notification@tcompliance.com"));
			   mime.addRecipient(Message.RecipientType.TO,new InternetAddress(to));  
			   mime.setSubject(subject);  
			   mime.setText(message);  
		     
		   //send message  
		   ExecutorService executor = Executors.newSingleThreadExecutor();
		   	executor.submit(() -> {
		   		try {
				   Transport.send(mime);
				   System.out.println("message sent successfully");  
			   	} catch (Exception e) {
			   		e.printStackTrace();
			   	}  
		   });
	   	} catch (Exception e) {
	   		e.printStackTrace();
	   	}  
		   
	} 
	
}
