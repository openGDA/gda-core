package uk.ac.diamond.daq.persistence.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.ac.diamond.daq.application.persistence.annotation.Id;
import uk.ac.diamond.daq.application.persistence.annotation.Listable;
import uk.ac.diamond.daq.application.persistence.annotation.Persistable;
import uk.ac.diamond.daq.application.persistence.annotation.Version;

@Persistable
public abstract class AbstractItem extends Item {

    public static final String SEARCH_NAME_FIELD = "name";

    @Id
    private long id;

    @Version
    private long version;

    @Listable(value = SEARCH_NAME_FIELD, primary = true)
    private String name;

    @JsonCreator
    AbstractItem(@JsonProperty("name") String name, @JsonProperty("id") long id, @JsonProperty("version") long version) {
        this.name = name;
        this.id = id;
        this.setVersion(version);
    }

    AbstractItem(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setId(){
        setId(true);
    }

    public abstract void execute();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractItem abstractItem = (AbstractItem) o;
        return (Objects.equals(name, abstractItem.name) && this.getVersion() == abstractItem.getVersion() && this.getId() == abstractItem.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }
}
