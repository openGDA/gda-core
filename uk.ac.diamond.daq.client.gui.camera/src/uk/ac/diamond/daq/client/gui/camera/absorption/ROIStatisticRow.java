package uk.ac.diamond.daq.client.gui.camera.absorption;

import java.util.UUID;

import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientSWTElements;

/**
 * Projects a {@link IRectangularROI} over a {@link IDataset} and displays the calculated properties as row in a {@link Table}.
 * The {@link #getRowID()} is used to define the {@link IRectangularROI} associated with this instance.
 * 
 * @author Maurizio Nagni
 *
 */
class ROIStatisticRow {
	private final UUID rowID = UUID.randomUUID();
	private final TableItem tableItem;
	private final ClientMessages name;
	private long value;

	private Label valueLabel;
	private Label ratioLabel;

	private IDataset dataset;
	private IRectangularROI fluxRegion;

	/**
	 * @param table the {@link Table} where attach the {@link TableItem} 
	 * @param name the name of the row
	 */
	public ROIStatisticRow(Table table, ClientMessages name) {
		this.tableItem = new TableItem(table, SWT.NONE);
		this.name = name;
		addColumns();
	}

	/**
	 * @return the row element
	 */
	public TableItem getTableItem() {
		return tableItem;
	}

	/**
	 * @return the row unique ID
	 */
	public UUID getRowID() {
		return rowID;
	}

	public ClientMessages getName() {
		return name;
	}

	/**
	 * Calculate the intensity of the {@code fluxRegion} respect to the {@code dataset}
	 * @param fluxRegion
	 * @param dataset
	 */
	public void processValue(IRectangularROI fluxRegion, IDataset dataset) {
		this.fluxRegion = fluxRegion;
		this.dataset = dataset;
		updateValue();
	}

	/**
	 * Calculate the ration of this region intensity com
	 * @param valueA
	 */
	public void processRatio(double valueA) {
		this.ratioLabel.setText(Double.toString(value / valueA));
	}

	public long getValue() {
		return value;
	}

	private void addColumns() {
		Table table = tableItem.getParent();

		TableEditor editor = new TableEditor(table);
		Label nameLabel = ClientSWTElements.createLabel(table, SWT.NONE, name);
		editor.grabHorizontal = true;
		editor.setEditor(nameLabel, tableItem, 0);

		editor = new TableEditor(table);
		valueLabel = ClientSWTElements.createLabel(table, SWT.NONE, ClientMessages.EMPTY_MESSAGE);
		editor.grabHorizontal = true;
		editor.setEditor(valueLabel, tableItem, 1);

		editor = new TableEditor(table);
		ratioLabel = ClientSWTElements.createLabel(table, SWT.NONE, ClientMessages.EMPTY_MESSAGE);
		editor.grabHorizontal = true;
		editor.setEditor(ratioLabel, tableItem, 2);
	}

	private void updateValue() {
		int[] xy = fluxRegion.getIntPoint();
		int[] length = fluxRegion.getIntLengths();

		int[] start = new int[] { xy[0], xy[1] };
		int[] end = new int[] { xy[0] + length[0], xy[1] + length[1] };
		int[] step = new int[] { 1, 1 };

		Dataset intDataset = DatasetUtils.convertToDataset(dataset.getSliceView(start, end, step));
		this.value = (int) intDataset.sum(true);
		this.valueLabel.setText(Long.toString(this.value));
	}
}
