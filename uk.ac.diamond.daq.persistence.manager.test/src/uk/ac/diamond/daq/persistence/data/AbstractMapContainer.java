package uk.ac.diamond.daq.persistence.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.ac.diamond.daq.persistence.implementation.annotation.*;

import java.util.Map;
@PersistableItem
public abstract class AbstractMapContainer {

	@Listable(value = Listable.ID, priority = Listable.ID_PRIORITY)
    private long id;

    @Listable(value = Listable.VERSION, priority = Listable.VERSION_PRIORITY)
    private long version;

    @Listable("map")
    protected Map<String, AbstractItem> map;

    @JsonCreator
    public AbstractMapContainer(@JsonProperty("map") Map map, @JsonProperty("id") long id, @JsonProperty("version") long version) {
        this.map = map;
        this.id = id;
        this.version = version;
    }

    public AbstractMapContainer(){

    }

    public Map getMap(){return this.map;};

    public void setMap(){this.map = map;};

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
