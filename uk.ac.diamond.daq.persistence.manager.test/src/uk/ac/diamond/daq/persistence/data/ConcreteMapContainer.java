package uk.ac.diamond.daq.persistence.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.diamond.daq.persistence.implementation.annotation.Listable;

import java.util.HashMap;
import java.util.Map;

public class ConcreteMapContainer extends AbstractMapContainer{
    private static final Logger log = LoggerFactory.getLogger(ConcreteMapContainer.class);

    @Listable(value = "Name", key = true)
    private String name;


    public ConcreteMapContainer(String name) {
        this.name = name;
        map = new HashMap<>();
    }

    @JsonCreator
    public ConcreteMapContainer(@JsonProperty("name") String name, @JsonProperty("map") Map map, @JsonProperty("id") long id, @JsonProperty("version") long version) {
        super(map, id, version);
        this.name = name;
    }

    public AbstractItem getItem(String key) {
        return map.get(key);
    }

    public String getName() {
        return name;
    }

    public void addItem(String name, AbstractItem item) {
        map.put(name, item);
    }

    public void execute() {
        log.info("Executing {}", name);

        map.forEach((key, item) -> {
            log.info("Executing: {}", key);
            item.execute();
        });
    }
}
