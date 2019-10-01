package uk.ac.diamond.daq.persistence.data;

import java.util.HashSet;
import java.util.Set;

import uk.ac.diamond.daq.persistence.implementation.annotation.PersistableItem;

@PersistableItem
public class SetCollection extends AbstractCollection {

	public SetCollection() {
		super();
	}

	private HashSet<AbstractItem> collection;

	public SetCollection(long start, long stop) {
		super(start, stop);
		this.setCollection(new HashSet<AbstractItem>());
	}

	public SetCollection(long start, long stop, HashSet<AbstractItem> set) {
		this(start, stop);
		this.setCollection(set);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((collection == null) ? 0 : collection.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SetCollection other = (SetCollection) obj;
		if (collection == null) {
			if (other.collection != null)
				return false;
		} else if (!collection.equals(other.collection))
			return false;
		return true;
	}

	public Set<AbstractItem> getSet(){
		return this.collection;
	}

	public void setSet(HashSet<AbstractItem> toSet) {
		this.collection = toSet;
	}

}
