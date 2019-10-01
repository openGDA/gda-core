package uk.ac.diamond.daq.persistence.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.ac.diamond.daq.persistence.implementation.annotation.Listable;
import uk.ac.diamond.daq.persistence.implementation.annotation.Persisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConcreteItemBsubA extends ConcreteItemB {

    public static final String UNIQUE_FIELD = "prop4";
    private static final Logger log = LoggerFactory.getLogger(ConcreteItemBsubA.class);

    @Listable(UNIQUE_FIELD)
    private int property4;

    public ConcreteItemBsubA(String name, int property1,
                             double property3, int property4) {
        super(name, property1, property3);

        this.property4 = property4;
    }

    @JsonCreator
    public ConcreteItemBsubA(@JsonProperty("name") String name, @JsonProperty("property1") int property1,
                             @JsonProperty("property3") double property3, @JsonProperty("property4") int property4, @JsonProperty("id") long id, @JsonProperty("version") long version) {
        super(name, property1, property3, id, version);

        this.property4 = property4;
    }

    public int getProperty4() {
        return property4;
    }

    @Override
    public void execute() {
        log.info("Executing ConcreteItemBsubA {} (id: {}, version: {}) property1: {}, property3: {}, property4: {}", getName(), getId(),
                getVersion(), getProperty1(), getProperty3(), getProperty4());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ConcreteItemBsubA that = (ConcreteItemBsubA) o;
        return (getProperty4() == that.getProperty4());
    }

}
