package uk.ac.gda.client;

import javax.measure.Quantity;

import org.eclipse.jface.databinding.swt.WidgetValueProperty;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Widget;

/**
 * Property for JFace data binding to {@link NumberAndUnitsComposite} and similar classes
 *
 * @author James Mudd
 */
public class NumberUnitsWidgetProperty<Q extends Quantity<Q>, S extends Widget> extends WidgetValueProperty<S, Double> {

	public NumberUnitsWidgetProperty() {
		super(SWT.Modify);
	}

	@Override
	public Object getValueType() {
		return double.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Double doGetValue(S source) {
		if (source instanceof NumberAndUnitsComposite) {
			return ((NumberAndUnitsComposite<Q>) source).getValue();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doSetValue(S source, Double value) {
		if (source instanceof NumberAndUnitsComposite) {
			((NumberAndUnitsComposite<Q>) source).setValue(value);
		}
	}

}