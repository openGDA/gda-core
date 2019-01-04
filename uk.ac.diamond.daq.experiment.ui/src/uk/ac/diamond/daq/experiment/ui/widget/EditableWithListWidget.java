package uk.ac.diamond.daq.experiment.ui.widget;

import java.beans.PropertyChangeListener;

/**
 * Implementors of this interface will be compatible with
 * {@link ListWithCustomEditor} and {@link ElementEditor}.
 *
 */
public interface EditableWithListWidget {
	
	/**
	 * This is the label shown in the list
	 */
	String getLabel();
	
	/**
	 * Called when the 'add' button is pressed
	 */
	EditableWithListWidget createDefault();

	/**
	 * Any event fired through your type's property change support
	 * will trigger a list refresh
	 */
	void addPropertyChangeListener(PropertyChangeListener listener);
	
	/**
	 * Listener is removed from an instance
	 * when said instance is deleted from the list
	 */
	void removePropertyChangeListener(PropertyChangeListener listener);
}
