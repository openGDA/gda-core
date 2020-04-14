package uk.ac.diamond.daq.experiment.structure;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Describes a single node within the experiment tree
 */
public class ExperimentNode {

	private final URL fileLocation;
	private final ExperimentNode parent;
	private final Set<ExperimentNode> children;

	public ExperimentNode(URL file, ExperimentNode parent) {
		this.fileLocation = file;
		this.parent = parent;
		this.children = new HashSet<>();
	}

	public URL getFileLocation() {
		return fileLocation;
	}

	public boolean isRoot() {
		return parent == null;
	}

	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public ExperimentNode getParent() {
		return parent;
	}

	public Set<ExperimentNode> getChildren() {
		return Collections.unmodifiableSet(children);
	}

	public void addChild(ExperimentNode node) {
		children.add(node);
	}

}
