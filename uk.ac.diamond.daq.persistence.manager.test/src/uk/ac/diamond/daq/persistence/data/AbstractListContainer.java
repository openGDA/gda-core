package uk.ac.diamond.daq.persistence.data;

import uk.ac.diamond.daq.persistence.implementation.annotation.Listable;
import uk.ac.diamond.daq.persistence.implementation.annotation.PersistableItem;
import uk.ac.diamond.daq.persistence.implementation.annotation.Persisted;

import java.util.List;

@PersistableItem
public abstract class AbstractListContainer {

	@Listable(value = Listable.ID, priority = Listable.ID_PRIORITY)
    protected long id;

    @Listable(value = Listable.VERSION, priority = Listable.VERSION_PRIORITY)
    protected long version;

    @Persisted
    protected List<AbstractItemContainer> abstractItemContainers;

    public long getVersion() {
        return this.version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getId() {
        return this.id;
    }

    public void setId(boolean resetVersion) {
        this.id = id++;
        if (resetVersion) {
            this.version = 0;
        }
    }

}
