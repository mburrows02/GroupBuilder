package ca.carleton.michelleburrows.groupbuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Student {
	private Map<String, List<String>> responses;
	
	public Student() {
		responses = new HashMap<String, List<String>>();
	}
	
	public void put(String key, String value) {
		if (responses.containsKey(key)) {
			responses.get(key).addAll(Arrays.asList(value.split(";")));
		} else {
			List<String> l = new ArrayList<String>();
			l.addAll(Arrays.asList(value.split(";")));
			responses.put(key, l);
			
		}
	}
	
	public List<String>get(String key) {
		return responses.get(key);
	}

}
