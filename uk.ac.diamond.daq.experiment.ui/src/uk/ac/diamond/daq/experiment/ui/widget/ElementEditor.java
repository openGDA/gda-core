package uk.ac.diamond.daq.experiment.ui.widget;

import org.eclipse.swt.widgets.Composite;

/**
 * Interface for custom editors to use with {@link ListWithCustomEditor}
 */
public interface ElementEditor {
	
	/**
	 * This will be called before load(...), so it should not depend on a non-null model
	 */
	void createControl(Composite parent);
	
	/**
	 * Populate your editor with the given element
	 */
	void load(EditableWithListWidget element);

	/**
	 * Clear your editor of data; no element is selected
	 */
	void clear();	
}
