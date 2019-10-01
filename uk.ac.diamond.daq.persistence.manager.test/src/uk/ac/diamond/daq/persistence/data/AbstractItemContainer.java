package uk.ac.diamond.daq.persistence.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.ac.diamond.daq.persistence.implementation.annotation.*;

@PersistableItem
public abstract class AbstractItemContainer {

    public static final String CONTAINER_NAME = "name";

    @Listable(value = Listable.ID, priority = Listable.ID_PRIORITY)
    private long id;

    @Listable(value = Listable.VERSION, priority = Listable.VERSION_PRIORITY)
    private long version;

    @Persisted
    private AbstractItem abstractItem;

    @Listable(value = CONTAINER_NAME, key = true)
    private String name;

    AbstractItemContainer(String name, AbstractItem abstractItem) {
        this.name = name;
        this.abstractItem = abstractItem;
    }

    @JsonCreator
    AbstractItemContainer(@JsonProperty("name") String name, @JsonProperty("abstractItem") AbstractItem abstractItem, @JsonProperty("id") long id, @JsonProperty("version") long version) {
        this.name = name;
        this.abstractItem = abstractItem;
        this.setVersion(version);
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public AbstractItem getAbstractItem() {
        return abstractItem;
    }

    public void setAbstractItem(AbstractItem theItem) {
        this.abstractItem = theItem;
    }

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

    public abstract void execute();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractItemContainer that = (AbstractItemContainer) o;
        return (that.name.equals(this.name)
                && that.getAbstractItem().getName().equals(this.getAbstractItem().getName()));
    }
}
