package uk.ac.diamond.daq.persistence.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.ac.diamond.daq.persistence.implementation.annotation.Listable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ConcreteListContainer extends AbstractListContainer{
    private static final Logger log = LoggerFactory.getLogger(ConcreteListContainer.class);

    @Listable("Name")
    protected String name;

    @JsonCreator
    public ConcreteListContainer(@JsonProperty("name") String name, @JsonProperty("map") List<AbstractItemContainer> list,
    		@JsonProperty(Listable.ID) long id, @JsonProperty(Listable.VERSION) long version) {
        this.name = name;
        abstractItemContainers = list;
        this.id = id;
        this.version = version;
    }
    
    public ConcreteListContainer(String name) {
    	this();
    	this.name = name;
    }

    public ConcreteListContainer(){
        super();
        abstractItemContainers = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void addTrigger(AbstractItemContainer abstractItemContainer) {
        abstractItemContainers.add(abstractItemContainer);
    }

    public List<AbstractItemContainer> getAbstractItemContainers() {
        return abstractItemContainers;
    }

    public void execute() {
        log.info("Started plan {} (id: {})", name, getId());

        for (AbstractItemContainer abstractItemContainer : abstractItemContainers) {
            abstractItemContainer.execute();
        }
    }
}
