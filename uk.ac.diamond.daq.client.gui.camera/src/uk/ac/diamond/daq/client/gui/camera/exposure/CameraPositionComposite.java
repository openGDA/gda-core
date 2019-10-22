package uk.ac.diamond.daq.client.gui.camera.exposure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import uk.ac.gda.client.NumberAndUnitsComposite;

public class CameraPositionComposite extends Composite {

	public CameraPositionComposite(Composite parent, int style) {
		super(parent, style);

		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(this);

		Label label;

		label = new Label(this, SWT.LEFT);
		label.setText("X:");
		GridDataFactory.swtDefaults().applyTo(label);

		Set<Unit<Length>> units = new HashSet<>();
		units.add(SI.MILLI(SI.METER));
		units.add(SI.METER);

		NumberAndUnitsComposite<Length> xLength = new NumberAndUnitsComposite<>(this, SWT.NONE, SI.METER, units);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(xLength);

		label = new Label(this, SWT.LEFT);
		label.setText("Y:");
		GridDataFactory.swtDefaults().applyTo(label);

		NumberAndUnitsComposite<Length> yLength = new NumberAndUnitsComposite<>(this, SWT.NONE, SI.METER, units);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(yLength);
	}
}
