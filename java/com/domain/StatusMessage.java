package com.domain;

import org.json.JSONObject;

public class StatusMessage extends JSONObject {
	
	private String id = "id";
	private String message = "message";

	public String getId() {
		return get(id).toString();
	}

	public void putId(String id1) {
		put(id, id1);
	}

	public String getMessage() {
		return get(message).toString();
	}
	
	public void putMessage(String msg) {
		put(message, msg);
	}

	public StatusMessage(String id, String message){
		putId(id);
		putMessage(message);
	}		

}
