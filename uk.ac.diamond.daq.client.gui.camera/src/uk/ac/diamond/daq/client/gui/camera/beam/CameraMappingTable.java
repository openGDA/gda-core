package uk.ac.diamond.daq.client.gui.camera.beam;

import java.util.Optional;
import java.util.stream.IntStream;

import org.apache.commons.math3.linear.RealMatrix;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import gda.rcp.views.CompositeFactory;
import uk.ac.gda.client.properties.camera.CameraToBeamMap;

/**
 * Displays a matrix 2x2
 *
 * @author Maurizio Nagni
 */
class CameraMappingTable implements CompositeFactory {
	private TableItem row1;
	private TableItem row2;

	@Override
	public Composite createComposite(Composite parent, int style) {
		Table table = new Table(parent, SWT.VIRTUAL | SWT.BORDER);
		table.setLinesVisible(true);
		table.setHeaderVisible(false);
		GridData data = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		table.setLayoutData(data);
		createTableColumn(table);

		row1 = new TableItem(table, SWT.NULL);
		row1.setText(0, "a00");
		row1.setText(1, "a01");
		row2 = new TableItem(table, SWT.NULL);
		row2.setText(0, "a10");
		row2.setText(1, "a11");
		table.setVisible(true);
		return table;
	}

	private void createTableColumn(Table table) {
		IntStream.range(0, 2).forEach(c -> {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setWidth(100);
		});
	}

	void displayMatrix(CameraToBeamMap beamCameraMap) {
		Optional.ofNullable(beamCameraMap.getMap())
			.ifPresent(this::updateGUI);

	}

	private void updateGUI(RealMatrix transformation) {
		row1.setText(0, Double.toString(transformation.getEntry(0, 0)));
		row1.setText(1, Double.toString(transformation.getEntry(0, 1)));

		row2.setText(0, Double.toString(transformation.getEntry(1, 0)));
		row2.setText(1, Double.toString(transformation.getEntry(1, 1)));
	}
}
