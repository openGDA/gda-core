package uk.ac.diamond.daq.persistence.data;

import java.util.ArrayList;

import uk.ac.diamond.daq.persistence.implementation.annotation.PersistableItem;

@PersistableItem
public class ListCollection extends AbstractCollection {

	private ArrayList<AbstractItem> collection;

	private ListCollection() {
		super();
	}

	public ListCollection(long start, long stop) {
		super(start, stop);
		this.setCollection(new ArrayList<AbstractItem>());
	}

	public ListCollection(long start, long stop, ArrayList<AbstractItem> list) {
		this(start, stop);
		this.setCollection(list);
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
		ListCollection other = (ListCollection) obj;
		if (collection == null) {
			if (other.collection != null)
				return false;
		} else if (!collection.equals(other.collection))
			return false;
		return true;
	}
}
