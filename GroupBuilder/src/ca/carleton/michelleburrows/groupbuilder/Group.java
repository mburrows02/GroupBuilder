package ca.carleton.michelleburrows.groupbuilder;

import java.util.ArrayList;
import java.util.List;

public class Group {
	private Question[] questions;
	private List<Student> students;
	private long conflictPoints;
	
	public Group(Question[] questions) {
		this.questions = questions;
		students = new ArrayList<Student>();
		conflictPoints = 0;
		for (Question question : questions) {
			conflictPoints += question.getMinBestAgreementCount() < 0?0:question.getMinBestAgreementCount();
			if (question.getAnswers() != null) {
				for (Answer answer : question.getAnswers()) {
					conflictPoints += answer.getMinSelect();
				}
			}
		}
	}
	
	public Group copy() {
		Group group = new Group(questions);
		for (Student student : students) {
			group.addStudent(student);
		}
		return group;
	}
	
	public List<Student> getStudents() {
		return students;
	}
	
	public int numStudents() {
		return students.size();
	}
	
	public long getConflictPoints() {
		return conflictPoints;
	}
	
	public void addStudent(Student s) {
		conflictPoints = 0;
		students.add(s);
		for (Question question : questions) {
			long minBestAgreementCount = question.getMinBestAgreementCount() < 0 ? students.size() : question.getMinBestAgreementCount();
			long bestAgreementCount = 0;
			long minValue = Long.MAX_VALUE;
			long maxValue = Long.MIN_VALUE;
			if (question.getType().equals("n")) {
				for (Student student : students) {
					long ans = Long.parseLong(student.get(question.getText()).get(0));
					if (ans > maxValue) {
						maxValue = ans;
					}
					if (ans < minValue) {
						minValue = ans;
					}
				}
				conflictPoints += maxValue - minValue;
			} else if (question.getAnswers() != null){
				for (Answer answer : question.getAnswers()) {
					
					int count = 0;
					for (Student student : students) {
						if (student.get(question.getText()).contains(answer.getText())) {
							++count;
						}
					}
					if (count > answer.getMaxSelect()) {
						conflictPoints += count - answer.getMaxSelect(); 
					}
					if (count < answer.getMinSelect()) {
						conflictPoints += answer.getMaxSelect() - count; 
					}
					if (count > bestAgreementCount) {
						bestAgreementCount = count;
					}
				}
				if (bestAgreementCount < minBestAgreementCount) {
					conflictPoints += minBestAgreementCount - bestAgreementCount;
				}
			}
		}
	}

}
