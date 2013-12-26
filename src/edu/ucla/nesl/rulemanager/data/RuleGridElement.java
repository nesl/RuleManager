package edu.ucla.nesl.rulemanager.data;

import java.util.ArrayList;
import java.util.List;

public class RuleGridElement {
	
	public List<String> allowedList;
	public List<String> deniedList;
	
	public List<Integer> allowedRuleIDs;
	public List<Integer> deniedRuleIDs;
	
	public RuleGridElement() {
		allowedList = new ArrayList<String>();
		deniedList = new ArrayList<String>();
		allowedRuleIDs = new ArrayList<Integer>();
		deniedRuleIDs = new ArrayList<Integer>();
	}
	
	public void addAllowed(String name, int ruleID) {
		allowedList.add(name);
		allowedRuleIDs.add(ruleID);
	}

	public void addDenied(String name, int ruleID) {
		deniedList.add(name);
		deniedRuleIDs.add(ruleID);
	}
}
