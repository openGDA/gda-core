package uk.ac.gda.client;

import org.eclipse.jface.databinding.swt.WidgetValueProperty;
import org.eclipse.swt.SWT;
import org.jscience.physics.quantities.Quantity;

/**
 * Property for JFace data binding to {@link NumberAndUnitsComposite} and similar classes
 *
 * @author James Mudd
 */
public class NumberUnitsWidgetProperty<Q extends Quantity> extends WidgetValueProperty {

	public NumberUnitsWidgetProperty() {
		super(SWT.Modify);
	}

	@Override
	public Object getValueType() {
		return double.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object doGetValue(Object source) {
		if (source instanceof NumberAndUnitsComposite) {
			return ((NumberAndUnitsComposite<Q>) source).getValue();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doSetValue(Object source, Object value) {
		if (source instanceof NumberAndUnitsComposite && value instanceof Double) {
			((NumberAndUnitsComposite<Q>) source).setValue(value);
		}
	}

}