package com.domain;

import org.json.JSONObject;

import com.common.NuUtil;

public abstract class JSONObjectBase extends JSONObject {
	
	int id;
	int version;	
	String createdate;
	
	public JSONObjectBase(){
		super();
		
		//put("createdate", NuUtil.getTodayDate());
	}
	
	public JSONObjectBase(String record){
		super(record);
	}

}
