package uk.ac.diamond.daq.experiment.structure;

import java.net.URL;
import java.util.HashSet;
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
	private URL location;
	private UUID parent;
	private Set<UUID> children;

	public ExperimentNode(URL location, UUID parent) {
		this(UUID.randomUUID(), location, parent, new HashSet<>());
	}

	private ExperimentNode(UUID id, URL location, UUID parent, Set<UUID> children) {
		this.id = id;
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

		Builder withLocation(URL location) {
			this.location = location;
			return this;
		}

		Builder withChildren(Set<UUID> children) {
			this.children = children;
			return this;
		}

		public ExperimentNode build() {
			return new ExperimentNode(id, location, parent, children);
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((children == null) ? 0 : children.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
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
		ExperimentNode other = (ExperimentNode) obj;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		return true;
	}

}
