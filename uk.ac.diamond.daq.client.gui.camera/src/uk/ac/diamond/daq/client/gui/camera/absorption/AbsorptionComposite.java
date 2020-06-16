package uk.ac.diamond.daq.client.gui.camera.absorption;

import static uk.ac.gda.ui.tool.ClientMessages.ABSORPTION;
import static uk.ac.gda.ui.tool.ClientMessages.BRIGHT;
import static uk.ac.gda.ui.tool.ClientMessages.DARK;

import java.util.stream.IntStream;

import org.eclipse.richbeans.widgets.menu.MenuAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.swtdesigner.SWTResourceManager;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.liveview.CameraPlotter;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;

/**
 * Allows to compare the intensities of two regions from a camera stream.
 * 
 * <p>
 * Creates a context {@link MenuAction} named <i>Absorption</i> which contains
 * other two other menu {@link MenuAction}: <i>Bright</i> and <i>Dark</i> For
 * each section, the user can select, then draw, different shapes: <i>Box</i>,
 * <i>Point</i> and <i>Circle</i>
 * </p>
 *
 * <p>
 * Creates also a two rows {@link Table}, one per region implemented as
 * {@link ROIStatisticRow} Each row contains four columns:
 * <ul>
 * <li>A check box to lock the calculated intensity ratio</li>
 * <li>The region name</li>
 * <li>The region intensity</li>
 * <li>The ratio with the other region, if existing</li>
 * </ul>
 * </p>
 * 
 * @author Maurizio Nagni
 * @see ROIStatisticRow, AbsorptionElement
 *
 */
public class AbsorptionComposite implements CompositeFactory {

	private final CameraPlotter cameraPlotter;

	public AbsorptionComposite(CameraPlotter cameraPlotter) {
		this.cameraPlotter = cameraPlotter;
	}

	@Override
	public Composite createComposite(final Composite parent, int style) {
		// Creates a table
		Table table = new Table(parent, SWT.VIRTUAL | SWT.BORDER);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 200;
		table.setLayoutData(data);
		createTableColumns(table);

		// Creates rows for the table
		ROIStatisticRow bright = new ROIStatisticRow(table, ClientMessages.BRIGHT);
		ROIStatisticRow dark = new ROIStatisticRow(table, ClientMessages.DARK);

		// Associates row to ratio
		bright.calculateRatioWith(dark.getIntensity(), ClientMessages.DARK);
		dark.calculateRatioWith(bright.getIntensity(), ClientMessages.BRIGHT);

		// Creates the context menu for absorption regions
		MenuAction absorption = new MenuAction(ClientMessagesUtility.getMessage(ABSORPTION));
		// Creates the bright region action and associates it the table row consumer
		absorption.add(new AbsorptionElement(BRIGHT, SWTResourceManager.getColor(SWT.COLOR_YELLOW), cameraPlotter,
				bright.getProcessEvent()));
		// Creates the dark region action and associates it the table row consumer
		absorption.add(new AbsorptionElement(DARK, SWTResourceManager.getColor(SWT.COLOR_RED), cameraPlotter,
				dark.getProcessEvent()));
		// append the menuAction to the existing context menu
		cameraPlotter.getPlottingSystem().getPlotActionSystem().addPopupAction(absorption);

		return table;
	}

	private void createTableColumns(Table table) {
		ClientMessages[] headers = { ClientMessages.LOCK_VALUE, ClientMessages.SELECTION, ClientMessages.INTENSITY,
				ClientMessages.RATIO };
		IntStream.range(0, headers.length).forEach(c -> {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setWidth(100);
			column.setText(ClientMessagesUtility.getMessage(headers[c]));
		});
	}
}
