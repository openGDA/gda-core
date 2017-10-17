package uk.ac.diamond.daq.mapping.ui;

import org.eclipse.jface.databinding.swt.WidgetValueProperty;
import org.eclipse.swt.SWT;

/**
 * Property for JFace data binding to {@link NumberAndUnitsComposite}
 *
 * @author James Mudd
 */
public class NumberUnitsWidgetProperty extends WidgetValueProperty {

	public NumberUnitsWidgetProperty() {
		super(SWT.Modify);
	}

	@Override
	public Object getValueType() {
		return double.class;
	}

	@Override
	protected Object doGetValue(Object source) {
		if (source instanceof NumberAndUnitsComposite) {
			return ((NumberAndUnitsComposite) source).getValue();
		}
		return null;
	}

	@Override
	protected void doSetValue(Object source, Object value) {
		if (source instanceof NumberAndUnitsComposite &&
				value instanceof Double) {
			((NumberAndUnitsComposite) source).setValue(value);
		}

	}

}