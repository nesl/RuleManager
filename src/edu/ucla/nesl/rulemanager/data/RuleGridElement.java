package edu.ucla.nesl.rulemanager.data;

import java.util.ArrayList;
import java.util.List;

public class RuleGridElement {
	
	public List<String> allowedList;
	public List<String> deniedList;
	
	public List<Integer> allowedRuleIDs;
	public List<Integer> deniedRuleIDs;
	
	public List<String> partialAllowedList;
	public List<String> partialDeniedList;
	
	public RuleGridElement() {
		allowedList = new ArrayList<String>();
		deniedList = new ArrayList<String>();
		
		allowedRuleIDs = new ArrayList<Integer>();
		deniedRuleIDs = new ArrayList<Integer>();
		
		partialAllowedList = new ArrayList<String>();
		partialDeniedList = new ArrayList<String>();
	}
	
	public void addAllowed(String name, int ruleID) {
		allowedList.add(name);
		allowedRuleIDs.add(ruleID);
	}

	public void addDenied(String name, int ruleID) {
		deniedList.add(name);
		deniedRuleIDs.add(ruleID);
	}
	
	public void addPartialAllowed(String name) {
		partialAllowedList.add(name);
	}
	
	public void addPartialDenied(String name) {
		partialDeniedList.add(name);
	}
}
