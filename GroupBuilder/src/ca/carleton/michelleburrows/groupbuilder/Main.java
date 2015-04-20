package ca.carleton.michelleburrows.groupbuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Main {
	private static String CONF_FILE = "..\\conf.json";
	private static String QUESTIONNAIRE_FILE = "";
	private static String ANSWERS_FILE = "";
	public static long MIN_GROUP_SIZE = 0;
	public static long MAX_GROUP_SIZE = 0;
	public static long MAX_GROUPS = 0;
	
	public static void main(String args[]) {
		setup();
		Question[] questionnaire = getQuestionnaire();
		Student[] students = getResponses();
		
		PriorityQueue<State> fringe = new PriorityQueue<State>(10, new ConflictComparator());
		fringe.add(new State(questionnaire, Arrays.asList(students)));
		State solution = null;
		long nodeCount = 0;
		while (!fringe.isEmpty()) {
			++nodeCount;
			State next = fringe.poll();
			if (next.isComplete()) {
				if (next.isValid()) {
					solution = next;
					break;
				} else {
					continue;
				}
			}
			if (next.isDoomed()) {
				continue;
			}
			for (State s : next.getChildren()) {
				fringe.add(s);
			}
		}
		if (solution == null) {
			System.out.println("No solution found");
		} else {
			System.out.println("Solution found after " + nodeCount + " nodes");
			int groupNum = 1;
			for (Group g : solution.getAllGroups()) {
				System.out.println("Group " + groupNum++ + ": ");
				for (Student s : g.getStudents()) {
					System.out.println("\t" + s.get("Name"));
				}
			}
		}
	}
	
	private static void setup() {
		String jsonString = "";
		try {
			jsonString = String.join("\n", Files.readAllLines(Paths.get(CONF_FILE)));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("\nError reading configuration file");
			System.exit(1);
		}
		JSONObject settings = null;
		try {
			settings = (JSONObject) new JSONParser().parse(jsonString);
		} catch (ParseException e) {
			e.printStackTrace();
			System.out.println("\nError parsing configuration file");
			System.exit(1);
		}
		QUESTIONNAIRE_FILE = (String)settings.get("questions");
		ANSWERS_FILE = (String)settings.get("responses");
		MIN_GROUP_SIZE = Long.parseLong((String)settings.get("minGroupSize"));
		MAX_GROUP_SIZE = Long.parseLong((String)settings.get("maxGroupSize"));
	}
	
	/**
	 * Get questionnaire from file
	 */
	private static Question[] getQuestionnaire() {
		Question[] questionnaire = null;
		String jsonString = "";
		try {
			jsonString = String.join("\n", Files.readAllLines(Paths.get(QUESTIONNAIRE_FILE)));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("\nError reading questionnaire file");
			System.exit(1);
		}
		JSONParser parser = new JSONParser();
		JSONArray jQuestionnaire = null;
		try {
			jQuestionnaire = (JSONArray)((JSONObject)parser.parse(jsonString)).get("questions");
		} catch (ParseException e) {
			e.printStackTrace();
			System.out.println("\nError parsing questionnaire file");
			System.exit(1);
		}
		questionnaire = new Question[jQuestionnaire.size()];
		for (int i = 0; i < jQuestionnaire.size(); ++i) {
			JSONObject jQuestion = (JSONObject) jQuestionnaire.get(i);
			Question question = new Question();
			question.parseJSON(jQuestion);
			questionnaire[i] = question;
		}
		return questionnaire;
	}
	
	/**
	 * Get questionnaire responses from file
	 */
	private static Student[] getResponses() {
		Student[] students = null;
		List<String> responses = null;
		try {
			responses = Files.readAllLines(Paths.get(ANSWERS_FILE));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("\nError reading responses file");
			System.exit(1);
		}
		
		students = new Student[responses.size()-1];
		String[] questionTitles = responses.get(0).split(",");
		for (int i = 1; i < responses.size(); ++i) {
			Student student = new Student();
			String[] studentAnswers = responses.get(i).split(",");
			for (int j = 0; j < questionTitles.length; ++j) {
				student.put(questionTitles[j], studentAnswers[j]);
			}
			students[i-1] = student;
		}
		MAX_GROUPS = students.length/MIN_GROUP_SIZE;
		return students;
	}
	
	private static class ConflictComparator implements Comparator<State> {
		public int compare(State x, State y) {
			return x.getTotalConflict()>y.getTotalConflict()?1:-1;
		}
	}
}
