package uk.ac.diamond.daq.experiment.ui.plan;

import org.eclipse.swt.widgets.Composite;

/**
 * A part which contains a selection which may or may not be valid.
 * 
 * After creating the part, a validation listener may be set with {@link #addValidationListener(ValidationListener)}
 */
public abstract class ValidatablePart {
	
	private ValidationListener listener;
	
	public void addValidationListener(ValidationListener listener) {
		this.listener = listener;
	}
	
	public abstract void createPart(Composite parentComposite);
	
	public abstract boolean isValidSelection();
	
	void notifyValidationListener() {
		if (listener != null) {
			listener.handle(isValidSelection());
		}
	}

}
