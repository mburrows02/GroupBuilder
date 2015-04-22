package ca.carleton.michelleburrows.groupbuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jgap.FitnessFunction;
import org.jgap.IChromosome;

public class GroupFitnessFunction extends FitnessFunction {
	private static final long serialVersionUID = 8922877956164654450L;
	private final Question[] questionnaire;
	private final Map<String, Student> studentMap;
	private final int groupSize;
	
	public GroupFitnessFunction(Question[] questionnaire, Map<String, Student> studentMap, int groupSize) {
		this.questionnaire = questionnaire;
		this.studentMap = studentMap;
		this.groupSize = groupSize;
	}

	@Override
	protected double evaluate(IChromosome arg0) {
		return 100000 - chromosomeToState(arg0).getTotalConflict();
	}
	
	public State chromosomeToState(IChromosome arg0) {
		List<Student> students = new ArrayList<Student>();
		List<Group> partialGroups = new ArrayList<Group>();
		List<Group> completeGroups = new ArrayList<Group>();
		Group currentGroup = null;
		for (int i = 0; i < arg0.size(); ++i) {
			if (i % groupSize == 0) {
				if (currentGroup != null) {
					completeGroups.add(currentGroup);
				}
				currentGroup = new Group(questionnaire);
			}
			String name = (String)arg0.getGene(i).getAllele();
			currentGroup.addStudent(studentMap.get(name));
		}
		completeGroups.add(currentGroup);
		return new State(questionnaire, students, partialGroups, completeGroups);
		
	}

}
