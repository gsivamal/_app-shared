package com.common;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Random;
import java.util.function.IntPredicate;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.xml.sax.InputSource;
import spark.Request;
import wjw.shiro.redis.SimpleAuthenticationInfoExt;

public class NuUtil {
	
	public enum UserStatus {
		Active("Active"),
		NotActivated("NotActivated");
		
		public String value;
        private UserStatus(String value) {
        	this.value = value;
        }
	};	
	
	public static String nustone = "nustone";
	
	//Random pool status
	public static String POOLENTRY_STATUS_PLANNED = "PLANNED";
	public static String POOLENTRY_STATUS_PENDING_SCHEDULE = "PENDING SCHEDULE";
	public static String POOLENTRY_STATUS_PENDING = "PENDING SUBMISSION";
	public static String POOLENTRY_STATUS_SUBMITTED = "ORDER SUBMITTED"; //SEND TO QUEST
	
	//Drug Test Status
	public static String REQUEST_STATUS_INITIATED = "INITIATED";
	public static String REQUEST_STATUS_FAILED = "FAILED";
	public static String REQUEST_STATUS_SCHEDULED = "SCHEDULED";
	public static String REQUEST_STATUS_MROPROCESS = "MROPROCESS";
	public static String REQUEST_STATUS_COMPLETED = "COMPLETED";
	
	//Email constants
	//public static String JULIE_EMAIL = "juliewaldrip@rocketmail.com";
	public static String CONTACT_TCOMPLIANCE_EMAIL = "contact@tcompliance.com";
	
	// Spark serve all static file are under "/public" in classpath if the route isn't consumed by others routes.
	// With Maven, the "/public" folder is assumed to be in "/main/resources"
	public static String resources_location = "resources";
	
	//public static String image_location = "/sparkstone-admin/company-images/";

	public static int maxRequestSize = 5 * 1024 * 1024;  // the maximum size allowed for multipart/form-data requests
	
	//CDL
	//public static int CDL_DTUG_TEST_POOL_RATE = 50; //50%
	//public static int CDL_ALCOHOL_TEST_POOL_RATE = 10; //10%
	
	//NONCDL
	//public static int NONCDL_DTUG_TEST_POOL_RATE = 75; //50%
	//public static int NONCDL_ALCOHOL_TEST_POOL_RATE = 25; //10%
	
	// ** Date Util **//	
	public static String getTodayDate(){
		SimpleDateFormat dtf = new SimpleDateFormat("MM/dd/yyyy");
		return dtf.format(new Date());
	}
	
	public static LocalDate toLocateDate(String d){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		LocalDate date = LocalDate.parse(d, formatter);
		return date;
	}
	
	public static String formatDate(String dt){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
	   	String formatted = toLocateDate(dt).format(formatter);	
		return formatted;
	}
	
	public static String formatDate(LocalDate ld){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
	   	String dt = ld.format(formatter);	
		return dt;
	}
	
	public static String formatDateTime(LocalDateTime ld){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm");
	   	String dt = ld.format(formatter);	
		return dt;
	}
	
	public static long daysAhead(String ed) {
		LocalDate expDate =	toLocateDate(ed);
		LocalDate today = LocalDate.now();
		return ChronoUnit.DAYS.between(today,expDate);
	}
	//<-
	
	//Given array, check a value exists
	public static boolean arrayContains(String arr[], String val){
		boolean contains = false;
		for (int i = 0; i < arr.length; i++) {
			String s = arr[i];
			
			if(s.equals(val)){
				contains = true;
			}
		}
		return contains;
	}
	
	//Random generator - my logic
	public static int newrandom_salai(int max){
		
		String r = String.valueOf(Math.random());
	 	String random = r.substring(r.indexOf("."));
		
		int charsize = 0;
		if(max < 10){
			charsize = 1;
		} else if(max < 100){
			charsize = 2;
		} else if(max < 1000){
			charsize = 3;
		} else if(max < 10000){
			charsize = 4;
		}
		
		StringBuffer tmp = new StringBuffer();
		int start = 1;
		int end = charsize;	    	
		for(int i=start; i <= end; i++){
			try{
				tmp.append(random.charAt(i));
			}catch(Exception e){}	
			if(Integer.parseInt(tmp.toString()) > max){
				//System.out.println("Tmp:"+tmp + " > max:"+max);
				tmp = new StringBuffer();
				++start;
				++end;
			} 
		}
		
		return Integer.parseInt(tmp.toString());	    
	}
	
	//Random generator
	public static int newrandom(int max){
		return newrandom(0, max);		
	}
	
	//Random generator
	public static int newrandom(int min, int max){			
			StringBuffer tmp = new StringBuffer();			
			IntPredicate p = new IntPredicate() {        		
				@Override
				public boolean test(int value) {
					if((value > min) && (value < max))
						return true;
					else 
						return false;
				}
			};        	
        	Random r = new Random();
        		r.ints().filter(p).limit(1).forEach( ic -> {	        			
        			tmp.append(ic);
        		});			
			return Integer.parseInt(tmp.toString());    
	}

	
	public static String readContent(Request req) throws IOException {
		
		byte b[] = new byte[req.contentLength()];
		
		try (InputStream io = req.raw().getInputStream()) {			
			io.read(b);		
		}
		
		return new String(b);		
	}	

	
	public static MultipartRequest readMultipartFiles(Request req) throws IOException {
		
		//salai otherwise working dir
		//MultipartRequest multi = new MultipartRequest(req.raw(), ".", NuUtil.maxRequestSize);
		
		MultipartRequest multi = new MultipartRequest(req.raw(), "resources", NuUtil.maxRequestSize);
		 
		return multi;		
	}
	
	public static String getSessionCompanyId(Request req) {
		
		SimpleAuthenticationInfoExt authInfo = (SimpleAuthenticationInfoExt)req.session().attribute("authInfo");
		String companyid = authInfo.getCompanyId();
		
		return companyid;
	}
	
	public static String getSessionEmail(Request req) {
		
		SimpleAuthenticationInfoExt authInfo = (SimpleAuthenticationInfoExt)req.session().attribute("authInfo");
		String email = authInfo.getPrincipals().asList().get(0).toString();
		
		return email;
	}
	
	
	public static String xpath(String responsexml, String expression){
		String nodeValue = null;
		try {
			XPath xpath = XPathFactory.newInstance().newXPath();
			InputSource inputSource = new InputSource(new StringReader( responsexml ));
			nodeValue = xpath.evaluate(expression, inputSource, XPathConstants.STRING).toString();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return nodeValue;
	}

	public static String getTagValue(String responsexml, String tagname){	
		//requestStatus
		String requestStatus = responsexml.substring( responsexml.indexOf(tagname) ,responsexml.indexOf("/"+tagname));
		return requestStatus.replace(tagname, "").replace("&amp;", "").replace("gt;", "").replace("lt;","");	 
	}	
	
	
	public static void createFile(String newFileName, String fileContents){
		
		try {

    		//String result = Less.compile(new File(AdminUtil.static_file_location+origFileName), false);

    		Path cssFilePath = new File(newFileName).toPath();
    		
    		 // truncate and overwrite an existing file, or create the file if
    	     // it doesn't initially exist
    	     OutputStream out = Files.newOutputStream(cssFilePath);

    	     byte[] b = fileContents.getBytes();
    	     out.write(b,0,b.length);
    		
    	     out.close();
    	     
    		//System.out.println("Compiled css:"+ fileContents);
		
    	}catch(Exception e){
    		e.printStackTrace();
    	}
		
		
	}
	
}
