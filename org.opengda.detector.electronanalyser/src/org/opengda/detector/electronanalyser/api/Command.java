package org.opengda.detector.electronanalyser.api;
import java.util.LinkedList;

import gda.util.BoundedLinkedList;

/**
 * Track changes of an object by saving copies and then being able to cycle through
 * each version, allowing to undo or redo changes.
 */
public class Command {
	private BoundedLinkedList<ICopy> savedStates;
	private LinkedList<ICopy> redoSavedStates = new LinkedList<>();
	private ICopy currentState;

	public Command(ICopy currentState) {
		this(currentState, 10);
	}

	public Command(ICopy currentState, int cap) {
		this.currentState = currentState;
		savedStates= new BoundedLinkedList<>(cap);
		addCommand(currentState);
	}

	/**
	* Whenever you need to take a "snapshot" of the object to be saved.
	*/
	public void addCommand(ICopy sequenceToAdd) {
		savedStates.add(sequenceToAdd.clone());
		redoSavedStates.clear();
	}

	public void undo() {
		//savedStates can not be empty!
		if (savedStates.size() > 1) redoSavedStates.add(savedStates.removeLast());
		currentState.copy(savedStates.peekLast());
	}

	public void redo() {
		//redoSavedStates can be empty
		if (!redoSavedStates.isEmpty()) savedStates.add(redoSavedStates.removeLast());
		currentState.copy(savedStates.peekLast());
	}

	public void reset(ICopy currentState) {
		savedStates.clear();
		redoSavedStates.clear();
		addCommand(currentState);
	}
}