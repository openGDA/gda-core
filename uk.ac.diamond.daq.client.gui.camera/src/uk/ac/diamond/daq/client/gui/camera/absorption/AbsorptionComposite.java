package uk.ac.diamond.daq.client.gui.camera.absorption;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.springframework.context.ApplicationListener;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.event.ROIChangeEvent;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * Creates a composite to compares the brightness of two ROI
 * 
 * @author Maurizio Nagni
 *
 */
public class AbsorptionComposite implements CompositeFactory {
	private IDataset dataset;
	private final List<ROIStatisticRow> rows = new ArrayList<>();

	@Override
	public Composite createComposite(final Composite parent, int style) {
		try {
			SpringApplicationContextProxy.addApplicationListener(roiListener);
		} catch (GDAClientException e1) {
			e1.printStackTrace();
		}
		Table table = new Table(parent, SWT.VIRTUAL | SWT.BORDER);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 200;
		table.setLayoutData(data);
		createTableColumn(table);

		rows.add(new ROIStatisticRow(table, ClientMessages.A));
		rows.add(new ROIStatisticRow(table, ClientMessages.B));
		return table;
	}

	private ApplicationListener<ROIChangeEvent> roiListener = new ApplicationListener<ROIChangeEvent>() {
		@Override
		public void onApplicationEvent(ROIChangeEvent event) {
			dataset = event.getDataset();

			RectangularROI rectangularROI = event.getRoi();

			// Updates the ratios
			rows.stream().forEach(r -> r.processValue(rectangularROI));

			// There is only one bright row and is the one after the header
			double valueA = rows.get(0).getValue();

			// Updates the ratios
			rows.stream().forEach(r -> r.processRatio(valueA));
		}
	};

	private void createTableColumn(Table table) {
		ClientMessages[] headers = { ClientMessages.LOCK_VALUE, ClientMessages.ROI, ClientMessages.VALUE,
				ClientMessages.RATIO_B_A };
		IntStream.range(0, headers.length).forEach(c -> {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setWidth(100);
			column.setText(ClientMessagesUtility.getMessage(headers[c]));
		});
	}

	private interface ProcessableRow {
		public void processValue(IRectangularROI fluxRegion);

		public void processRatio(double bright);

		public double getValue();
	}

	private class ROIStatisticRow implements ProcessableRow {
		private final TableItem tableItem;
		private final ClientMessages name;
		private double value;

		private Button lockValue;
		private Label nameLabel;
		private Label valueLabel;
		private Label ratioLabel;

		private IRectangularROI fluxRegion;

		public ROIStatisticRow(Table table, ClientMessages name) {
			this.tableItem = new TableItem(table, SWT.NONE);
			this.name = name;
			addColumns();
		}

		private void addColumns() {
			Table table = tableItem.getParent();
			TableEditor editor = new TableEditor(table);
			lockValue = ClientSWTElements.createButton(table, SWT.CHECK, ClientMessages.EMPTY_MESSAGE,
					ClientMessages.EMPTY_MESSAGE);
			editor.grabHorizontal = true;
			editor.setEditor(lockValue, tableItem, 0);

			editor = new TableEditor(table);
			nameLabel = ClientSWTElements.createLabel(table, SWT.NONE, name);
			editor.grabHorizontal = true;
			editor.setEditor(nameLabel, tableItem, 1);

			editor = new TableEditor(table);
			valueLabel = ClientSWTElements.createLabel(table, SWT.NONE, ClientMessages.EMPTY_MESSAGE);
			editor.grabHorizontal = true;
			editor.setEditor(valueLabel, tableItem, 2);

			editor = new TableEditor(table);
			ratioLabel = ClientSWTElements.createLabel(table, SWT.NONE, ClientMessages.EMPTY_MESSAGE);
			editor.grabHorizontal = true;
			editor.setEditor(ratioLabel, tableItem, 3);
		}

		@Override
		public void processValue(IRectangularROI fluxRegion) {
			if (lockValue.getSelection()) {
				return;
			}
			this.fluxRegion = fluxRegion;
			updateValue();
		}

		@Override
		public void processRatio(double valueA) {
			this.ratioLabel.setText(Double.toString(value / valueA));
		}

		@Override
		public double getValue() {
			return value;
		}

		private void updateValue() {
			int[] xy = fluxRegion.getIntPoint();
			int[] length = fluxRegion.getIntLengths();

			int[] start = new int[] { xy[0], xy[1] };
			int[] end = new int[] { xy[0] + length[0], xy[1] + length[1] };
			int[] step = new int[] { 1, 1 };

			Dataset intDataset = DatasetUtils.convertToDataset(dataset.getSliceView(start, end, step));

			long count = IntStream.rangeClosed(0, intDataset.getSize()).parallel()
					.filter(i -> !Double.isNaN(intDataset.getElementDoubleAbs(i))).count();
			if (count == 0) {
				Status status = new Status(IStatus.ERROR, "PluginID", "Corrupted stream data ");
				// Display the dialog
				ErrorDialog.openError(Display.getCurrent().getActiveShell(), "Control Error",
						"Cannot calculate ROI intensity", status);
				return;
			}

			double totalVal = IntStream.rangeClosed(0, intDataset.getSize()).parallel()
					.filter(i -> !Double.isNaN(intDataset.getElementDoubleAbs(i)))
					.mapToDouble(intDataset::getElementDoubleAbs).sum();
			this.value = Math.round(totalVal / count);
			this.valueLabel.setText(Long.toString(Math.round(this.value)));
		}
	}
}
