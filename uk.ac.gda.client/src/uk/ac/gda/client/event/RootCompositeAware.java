package uk.ac.gda.client.event;

import java.util.Optional;
import java.util.UUID;

import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.ui.tool.ClientSWTElements;

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

	/**
	 * Verifies if the {@code container} has the same parent of the event
	 * @param container the container receiving the event
	 * @return {@code true} if have the same parent, {@code false} otherwise
	 */
	default boolean haveSameParent(Composite container) {
		return ClientSWTElements.findParentUUID(container)
				.map(uuid -> getRootComposite().map(rc -> rc.equals(uuid)).orElse(false)).orElse(false);
	}

}
