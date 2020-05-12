package uk.ac.gda.client.event;

import java.util.Optional;
import java.util.UUID;

/**
 * A context aware event. A context aware element may discriminate events comparing its root parent UUID with the one
 * returned by {@link #getRootComposite()}.
 *
 * @author Maurizio Nagni
 */
public interface RootCompositeAware {

	/**
	 * Returns the root component for the element which publishes this event
	 *
	 * @return a component unique id
	 */
	public Optional<UUID> getRootComposite();

}
