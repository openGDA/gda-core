package uk.ac.diamond.daq.experiment.api.ui;

import java.beans.PropertyChangeListener;

/**
 * Implementors of this interface will be compatible with
 * ListWithCustomEditor ElementEditor.
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

	static final String REFRESH_PROPERTY = "refreshRequest";

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
