package uk.ac.diamond.daq.client.gui.camera.event;

import java.util.UUID;

/**
 * A context aware event. A context aware element may discriminate events
 * published by other elements comparing its parent UUID with the one returned
 * by {@link #getRootComposite()}.
 * 
 * @author Maurizio Nagni
 */
public interface RootCompositeAware {

	/**
	 * Returns the root component for the element which publishes this event
	 * @return a component unique id
	 */
	public UUID getRootComposite();

}
