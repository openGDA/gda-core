package uk.ac.diamond.daq.experiment.structure;

import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;

/**
 * Traverses a {@link ExperimentNode} tree and indicates current position
 */
public class TreeNavigator {

	private ExperimentNode currentNode;

	/**
	 * Sets the pointer to the given node
	 */
	public void point(ExperimentNode node) {
		this.currentNode = node;
	}

	/**
	 * Returns the node we are pointing to
	 */
	public ExperimentNode getCurrentNode() {
		return currentNode;
	}

	/**
	 * Moves the pointer up, to the current node's parent
	 */
	public void moveUp() throws ExperimentControllerException {
		if (currentNode.isRoot()) {
			throw new ExperimentControllerException("Inconsistent experiment structure");
		}
		currentNode = currentNode.getParent();
	}

	/**
	 * Moves the pointer down the given node which is already a child of the current node
	 */
	public void moveDown(ExperimentNode node) throws ExperimentControllerException {
		if (currentNode.equals(node.getParent())) {
			currentNode = node;
		} else {
			throw new ExperimentControllerException("Inconsistent experiment structure");
		}
	}

}
