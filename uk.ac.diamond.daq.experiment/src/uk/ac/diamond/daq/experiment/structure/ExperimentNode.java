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

import java.net.URL;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Describes a single node within the experiment tree
 */
@JsonDeserialize(builder = ExperimentNode.Builder.class)
public class ExperimentNode {

	private UUID id;
	private String name;
	private URL location;
	private UUID parent;
	private Set<UUID> children;

	public ExperimentNode(String name, URL location, UUID parent) {
		this(UUID.randomUUID(), name, location, parent, new HashSet<>());
	}

	private ExperimentNode(UUID id, String name, URL location, UUID parent, Set<UUID> children) {
		this.id = id;
		this.name = name;
		this.location = location;
		this.parent = parent;
		this.children = children;
	}

	@JsonIgnore
	public boolean isRoot() {
		return parent == null;
	}

	public void addChild(UUID childId) {
		children.add(childId);
	}

	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public URL getLocation() {
		return location;
	}

	public UUID getParentId() {
		return parent;
	}

	public Set<UUID> getChildren() {
		return children;
	}

	@JsonPOJOBuilder
	public static class Builder {
		private UUID id;
		private String name;
		private UUID parent;
		private URL location;
		private Set<UUID> children;

		Builder withId(UUID id) {
			this.id = id;
			return this;
		}

		Builder withParentId(UUID parentId) {
			this.parent = parentId;
			return this;
		}

		Builder withName(String name) {
			this.name = name;
			return this;
		}

		Builder withLocation(URL location) {
			this.location = location;
			return this;
		}

		Builder withChildren(Set<UUID> children) {
			this.children = children;
			return this;
		}

		public ExperimentNode build() {
			return new ExperimentNode(id, name, location, parent, children);
		}

	}

	@Override
	public int hashCode() {
		return Objects.hash(children, id, location, name, parent);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExperimentNode other = (ExperimentNode) obj;
		return Objects.equals(children, other.children) && Objects.equals(id, other.id)
				&& Objects.equals(location, other.location) && Objects.equals(name, other.name)
				&& Objects.equals(parent, other.parent);
	}

}
