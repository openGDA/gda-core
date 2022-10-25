/*-
 * Copyright © 2020 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.daq.experiment.structure;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;

/**
 * Represents an experiment in a tree data structure
 */
@JsonDeserialize(builder = ExperimentTree.Builder.class)
public class ExperimentTree {

	private final String experimentName;
	private final Map<UUID, ExperimentNode> nodes;
	private ExperimentNode activeNode;

	private ExperimentTree(String experimentName, ExperimentNode activeNode, Map<UUID, ExperimentNode> nodes) {
		this.experimentName = experimentName;
		this.nodes = nodes;
		this.nodes.put(activeNode.getId(), activeNode);
		this.activeNode = activeNode;
	}

	public ExperimentNode getActiveNode() {
		return activeNode;
	}

	public void addChild(ExperimentNode child) {
		getActiveNode().addChild(child.getId());
		nodes.put(child.getId(), child);
	}

	public void moveUp() throws ExperimentControllerException {
		if (getActiveNode().isRoot()) {
			throw new ExperimentControllerException("Inconsistent experiment structure");
		}

		activeNode = nodes.get(getActiveNode().getParentId());
	}

	public void moveDown(UUID childNodeId) throws ExperimentControllerException {
		if (nodes.containsKey(childNodeId) && nodes.get(childNodeId).getParentId().equals(activeNode.getId())) {
			activeNode = nodes.get(childNodeId);
		} else {
			throw new ExperimentControllerException("Inconsistent experiment structure");
		}
	}

	public String getExperimentName() {
		return experimentName;
	}

	public ExperimentNode getNode(UUID nodeId) {
		return nodes.get(nodeId);
	}

	public Map<UUID, ExperimentNode> getNodes() {
		return Collections.unmodifiableMap(nodes);
	}

	@JsonPOJOBuilder
	public static class Builder {

		private String experimentName;
		private ExperimentNode activeNode;
		private Map<UUID, ExperimentNode> nodes = new HashMap<>();

		Builder withExperimentName(String experimentName) {
			this.experimentName = experimentName;
			return this;
		}

		Builder withActiveNode(ExperimentNode activeNode) {
			this.activeNode = activeNode;
			return this;
		}

		Builder withNodes(Map<UUID, ExperimentNode> nodes) {
			this.nodes = nodes;
			return this;
		}

		public ExperimentTree build() {
			return new ExperimentTree(experimentName, activeNode, nodes);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((activeNode == null) ? 0 : activeNode.hashCode());
		result = prime * result + ((experimentName == null) ? 0 : experimentName.hashCode());
		result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExperimentTree other = (ExperimentTree) obj;
		if (activeNode == null) {
			if (other.activeNode != null)
				return false;
		} else if (!activeNode.equals(other.activeNode))
			return false;
		if (experimentName == null) {
			if (other.experimentName != null)
				return false;
		} else if (!experimentName.equals(other.experimentName))
			return false;
		if (nodes == null) {
			if (other.nodes != null)
				return false;
		} else if (!nodes.equals(other.nodes))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ExperimentTree [experimentName=" + experimentName + ", nodes=" + nodes + ", activeNode=" + activeNode
				+ "]";
	}

}
