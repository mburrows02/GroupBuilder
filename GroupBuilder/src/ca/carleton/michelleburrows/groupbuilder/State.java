package ca.carleton.michelleburrows.groupbuilder;

import java.util.ArrayList;
import java.util.List;

public class State {
	private Question[] questions;
	private List<Student> students; //Remaining to be sorted
	private List<Group> partialGroups;
	private List<Group> completeGroups;
	
	public State(Question[] questions, List<Student> students) {
		this.questions = questions;
		this.students = students;
		partialGroups = new ArrayList<Group>();
		partialGroups.add(new Group(questions));
		completeGroups = new ArrayList<Group>();
	}
	
	public State(Question[] questions, List<Student> students, List<Group> partialGroups, List<Group> completeGroups) {
		this.questions = questions;
		this.students = students;
		this.partialGroups = partialGroups;
		this.completeGroups = completeGroups;
	}
	
	public State next(int i) {
		List<Student> students = new ArrayList<Student>();
		students.addAll(this.students);
		Student nextStudent = students.remove(students.size() - 1);
		List<Group> partialGroups = new ArrayList<Group>();
		List<Group> completeGroups = new ArrayList<Group>();
		for (int j = 0; j < this.partialGroups.size(); ++j) {
			Group group = this.partialGroups.get(j).copy();
			if (j == i) {
				if (group.numStudents() == 0) {
					partialGroups.add(new Group(questions));
				}
				group.addStudent(nextStudent);
				if (group.numStudents() >= Main.MAX_GROUP_SIZE) {
					completeGroups.add(group);
					continue;
				} 
			}
			partialGroups.add(group);
		}
		for (int j = 0; j < this.completeGroups.size(); ++j) {
			completeGroups.add(this.completeGroups.get(j).copy());
		}
		return new State(questions, students, partialGroups, completeGroups);
	}
	
	public boolean isComplete() {
		return students.isEmpty();
	}
	
	public boolean isValid() {
		for (Group g : partialGroups) {
			if (g.numStudents() > 0 && g.numStudents() < Main.MIN_GROUP_SIZE) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isDoomed() {
		return completeGroups.size() + partialGroups.size() > Main.MAX_GROUPS;
	}
	
	public List<State> getChildren() {
		List<State> states = new ArrayList<State>();
		for (int i = 0; i < partialGroups.size(); ++i) {
			states.add(next(i));
		}
		return states;
	}
	
	public long getTotalConflict() {
		long conflict = 0;
		for (Group g : partialGroups) {
			conflict += g.getConflictPoints();
		}
		for (Group g : completeGroups) {
			conflict += g.getConflictPoints();
		}
		conflict += students.size();
		return conflict;
	}
	
	public List<Group> getAllGroups() {
		List<Group> all = new ArrayList<Group>();
		for (Group g : partialGroups) {
			if (g.numStudents() > 0) {
				all.add(g);
			}
		}
		all.addAll(completeGroups);
		return all;
	}
}
