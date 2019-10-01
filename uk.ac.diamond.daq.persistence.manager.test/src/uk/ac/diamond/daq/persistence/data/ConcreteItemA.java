package uk.ac.diamond.daq.persistence.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.ac.diamond.daq.persistence.implementation.annotation.Listable;

public class ConcreteItemA extends AbstractItem {

    public static final String CLASS_UNIQUE_FIELD = "uniqueProperty";
    private static final Logger log = LoggerFactory.getLogger(ConcreteItemA.class);
    @Listable("property1")
    private int property1;
    @Listable("property2")
    private int property2;
    @Listable(CLASS_UNIQUE_FIELD)
    private String property3 = CLASS_UNIQUE_FIELD;

    @JsonCreator
    public ConcreteItemA(@JsonProperty("name") String name, @JsonProperty("property1") int property1, @JsonProperty("property2") int property2,
                         @JsonProperty("property3") String property3, @JsonProperty("id") long id, @JsonProperty("version") long version) {
        super(name, id, version);

        this.property1 = property1;
        this.property2 = property2;
        this.property3 = property3;
    }

    public ConcreteItemA(String name, int property1,
                         int property2, String property3) {
        super(name);

        this.property1 = property1;
        this.property2 = property2;
        this.property3 = property3;
    }


    public int getProperty1() {
        return property1;
    }

    public int getProperty2() {
        return property2;
    }

    public String getProperty3() {
        return property3;
    }

    @Override
    public void execute() {
        log.info("Executing ConcreteItemA {} property1: {}, property2: {}, {}: {}", getName(), property1, property2, CLASS_UNIQUE_FIELD, property3);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ConcreteItemA that = (ConcreteItemA) o;
        return (that.getProperty1() == this.getProperty1() && that.getProperty2() == this.getProperty2() && that.getProperty3().equals(this.getProperty3()));
    }
}
