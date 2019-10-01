package uk.ac.diamond.daq.persistence.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.ac.diamond.daq.persistence.implementation.annotation.Persisted;
import uk.ac.diamond.daq.persistence.implementation.annotation.Listable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConcreteItemContainer extends AbstractItemContainer {
    private static final Logger log = LoggerFactory.getLogger(ConcreteItemContainer.class);
    @Persisted
    private double property4;

    @JsonCreator
    public ConcreteItemContainer(@JsonProperty("name") String name, @JsonProperty("scan") AbstractItem abstractItem,
                                 @JsonProperty("property4") double property4, @JsonProperty(Listable.ID) long id, @JsonProperty(Listable.VERSION) long version) {
        super(name, abstractItem, id, version);
        this.property4 = property4;
    }

    public ConcreteItemContainer(String name, AbstractItem abstractItem, double property4) {
        super(name, abstractItem);
        this.property4 = property4;
    }

    @Override
    public void execute() {
        log.info("Trigger {} property4: {}", getName(), property4);
        getAbstractItem().execute();
    }

    public double getProperty4() {
        return property4;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ConcreteItemContainer that = (ConcreteItemContainer) o;
        return (getProperty4() == that.getProperty4());
    }

}
