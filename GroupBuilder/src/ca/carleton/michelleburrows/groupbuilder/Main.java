package ca.carleton.michelleburrows.groupbuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.Population;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.StringGene;
import org.jgap.impl.SwappingMutationOperator;
import org.jgap.impl.TournamentSelector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Main {
	private static String CONF_FILE = "data\\conf.json";
	private static String QUESTIONNAIRE_FILE = "";
	private static String ANSWERS_FILE = "";
	public static int MIN_GROUP_SIZE = 0;
	public static int MAX_GROUP_SIZE = 0;
	public static int MAX_GROUPS = 0;
	private static int MAX_GEN = 50;
	private static int POP_SIZE = 10;
	private static int TOURNAMENT_SIZE = 5;
	private static double TOURNAMENT_PROB = 0.4;
	private static double XOVER_RATE = 0.9;
	private static boolean stats = false;
	
	public static void main(String args[]) {
		setup();
		Question[] questionnaire = getQuestionnaire();
		Student[] students = getResponses();
		System.out.println("Uniform-Cost Search:");
		State uniformCostSolution = uniformCostSearch(questionnaire, students);
		if (uniformCostSolution == null) {
			System.out.println("No solution found");
		} else {
			System.out.println("Solution conflict: " + uniformCostSolution.getTotalConflict());
			int groupNum = 1;
			for (Group g : uniformCostSolution.getAllGroups()) {
				System.out.println("Group " + groupNum++ + ": ");
				for (Student s : g.getStudents()) {
					System.out.println("\t" + s.get("Name"));
				}
			}
		}
		
		System.out.println("\n\nGenetic Algorithm:");
		State geneticAlgoSolution = null;
		try {
			if (stats) {
				long  minConflict = Long.MAX_VALUE;
				int trials = 100;
				long totalConflict = 0;
				for (int i = 0; i < trials; ++i) {
					geneticAlgoSolution = geneticSearch(questionnaire, students);
					long conflict = geneticAlgoSolution.getTotalConflict();
					if (conflict < minConflict) {
						minConflict = conflict;
					}
					totalConflict += conflict;
				}
				System.out.println(trials + " trials: ");
				System.out.println("\tMinimum conflict achieved: " + minConflict);
				System.out.println("\tAverage conflict: " + totalConflict/trials);
			} else {
				geneticAlgoSolution = geneticSearch(questionnaire, students);
			}
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
			System.out.println("\nError configuring GA");
			System.exit(1);
		}
		if (geneticAlgoSolution == null) {
			System.out.println("No solution found");
		} else {
			System.out.println("Solution conflict: " + geneticAlgoSolution.getTotalConflict());
			int groupNum = 1;
			for (Group g : geneticAlgoSolution.getAllGroups()) {
				System.out.println("Group " + groupNum++ + ": ");
				for (Student s : g.getStudents()) {
					System.out.println("\t" + s.get("Name"));
				}
			}
		}
	}
	
	private static State uniformCostSearch(Question[] questionnaire, Student[] students) {
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
		System.out.println("Uniform cost search traversed " + nodeCount + " nodes");
		return solution;
	}
	
	private static State geneticSearch(Question[] questionnaire, Student[] students) throws InvalidConfigurationException {
		Map<String, Student> studentMap = getStudentMap(students);
		String[] names = new String[students.length];

		Configuration.reset();
		Configuration conf = new DefaultConfiguration();
		conf.getGeneticOperators().clear();
		conf.addGeneticOperator(new PermutationCrossover(conf, XOVER_RATE));
		conf.addGeneticOperator(new SwappingMutationOperator(conf));
		TournamentSelector tournament = new TournamentSelector(conf, TOURNAMENT_SIZE, TOURNAMENT_PROB);
		conf.addNaturalSelector(tournament, false);
		GroupFitnessFunction fitFunc = new GroupFitnessFunction(questionnaire, studentMap, MAX_GROUP_SIZE);
		conf.setFitnessFunction(fitFunc);
		conf.setPopulationSize(POP_SIZE);
		Gene[] sampleGenes = new Gene[students.length];
		int i = 0;
		for (String name : studentMap.keySet()) {
			names[i] = name;
			sampleGenes[i++] = new StringGene(conf, 0, 64, StringGene.ALPHABET_CHARACTERS_LOWER + StringGene.ALPHABET_CHARACTERS_UPPER);
		}
		
		Chromosome sampleChromosome = new Chromosome(conf, sampleGenes);
		conf.setSampleChromosome(sampleChromosome);
		Genotype population = new Genotype(conf, generatePopulation(conf, names));
		IChromosome bestSolution = null;
		int oldBestFit = 0;
		for (int gen = 0; gen < MAX_GEN; ++gen) {
			IChromosome bestInGen = population.getFittestChromosome();
			int bestFit = (int) bestInGen.getFitnessValue();
			if (bestFit > oldBestFit) {
				bestSolution = bestInGen;
				oldBestFit = bestFit;
			}
			population.evolve();
		}
		return fitFunc.chromosomeToState(bestSolution);
	}
	
	private static Population generatePopulation(Configuration conf, String[] names) throws InvalidConfigurationException {
		Population pop = new Population(conf);
		List<StringGene> genes = new ArrayList<StringGene>();
		for (int i = 0; i < names.length; ++i) {
			StringGene gene = new StringGene(conf, 0, 64, StringGene.ALPHABET_CHARACTERS_LOWER + StringGene.ALPHABET_CHARACTERS_UPPER);
			gene.setAllele(names[i]);
			genes.add(gene);
		}
		
		Random rand = new Random();
		for (int i = 0; i < POP_SIZE; ++i) {
			StringGene[] randGenes = new StringGene[names.length];
			Collections.shuffle(genes, rand);
			for (int j = 0; j < names.length; ++j) {
				randGenes[j] = genes.get(j);
			}
			pop.addChromosome(new Chromosome(conf, randGenes));
		}
		return pop;
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
		MIN_GROUP_SIZE = Integer.parseInt((String)settings.get("minGroupSize"));
		MAX_GROUP_SIZE = Integer.parseInt((String)settings.get("maxGroupSize"));
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
	
	private static Map<String, Student> getStudentMap(Student[] students) {
		Map<String, Student> studentMap = new HashMap<String, Student>();
		for (Student student : students) {
			studentMap.put(student.get("Name").get(0), student);
		}
		return studentMap;
	}
	
	private static class ConflictComparator implements Comparator<State> {
		public int compare(State x, State y) {
			return x.getTotalConflict()>y.getTotalConflict()?1:-1;
		}
	}
}
