package uk.ac.diamond.daq.persistence.data;

import uk.ac.diamond.daq.persistence.implementation.annotation.Listable;
import uk.ac.diamond.daq.persistence.implementation.annotation.PersistableItem;
import uk.ac.diamond.daq.persistence.implementation.annotation.Persisted;

@PersistableItem
public class ArrayCollection {

	public ArrayCollection(long start, long stop, AbstractItem[] array) {
		this(start, stop);
		this.collection = array;
	}

	@Persisted
	private long start;
	@Listable("stop")
	private long stop;

	@Listable(value = Listable.ID, priority = Listable.ID_PRIORITY)
	private long id;

	@Listable(value = Listable.VERSION, priority = Listable.VERSION_PRIORITY)
	private long version;

	@Persisted
    protected AbstractItem[] collection;

	protected ArrayCollection() {
	}

	public ArrayCollection(long start, long stop) {
		this.start = start;
		this.stop = stop;
	}

}
