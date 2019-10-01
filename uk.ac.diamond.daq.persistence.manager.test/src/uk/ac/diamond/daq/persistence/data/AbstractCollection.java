package uk.ac.diamond.daq.persistence.data;

import java.util.Collection;

import uk.ac.diamond.daq.persistence.implementation.annotation.Listable;
import uk.ac.diamond.daq.persistence.implementation.annotation.PersistableItem;
import uk.ac.diamond.daq.persistence.implementation.annotation.Persisted;

@PersistableItem
public abstract class AbstractCollection {

	@Persisted
	private long start;
	@Listable("stop")
	private long stop;

	@Listable(value = Listable.ID, priority = Listable.ID_PRIORITY)
	private long id;

	@Listable(value = Listable.VERSION, priority = Listable.VERSION_PRIORITY)
	private long version;

	@Persisted
	protected Collection<AbstractItem> collection;

	protected AbstractCollection() {
	}

	public AbstractCollection(long start, long stop) {
		this.start = start;
		this.stop = stop;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getStop() {
		return stop;
	}

	public void setStop(long stop) {
		this.stop = stop;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public Collection<AbstractItem> getCollection() {
		return collection;
	}

	public void setCollection(Collection<AbstractItem> collection) {
		this.collection = collection;
	}

	public void addToCollection(AbstractItem item) {
		this.collection.add(item);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((collection == null) ? 0 : collection.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + (int) (start ^ (start >>> 32));
		result = prime * result + (int) (stop ^ (stop >>> 32));
		result = prime * result + (int) (version ^ (version >>> 32));
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
		AbstractCollection other = (AbstractCollection) obj;
		if (collection == null) {
			if (other.collection != null)
				return false;
		} else if (!collection.equals(other.collection))
			return false;
		if (id != other.id)
			return false;
		if (start != other.start)
			return false;
		if (stop != other.stop)
			return false;
		if (version != other.version)
			return false;
		return true;
	}
}
