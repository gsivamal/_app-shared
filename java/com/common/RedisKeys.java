package com.common;

public interface RedisKeys {
	
	// Master Keys
	public String company_prefix = "company:";

	// Sequences
	public String compid_seq = "compid_seq";

	//Redis Type: List, Purpose: Company List Index Key
	public String company_idx = company_prefix+"idx";


	//Company specific keys

	public String categories_suffix = ":categories";
	

	
	
}
