/*-
 * Copyright © 2013 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.gda.devices.bssc.ui;

import static java.lang.Boolean.TRUE;
import static uk.ac.gda.devices.bssc.beans.BSSCSessionBean.BSSC_PLATES;
import static uk.ac.gda.devices.hatsaxs.SampleListStats.getRuntimeString;
import static uk.ac.gda.devices.hatsaxs.ui.Column.ColumnType.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.eclipse.gef.dnd.SimpleObjectTransfer;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.widgets.FieldComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.jython.InterfaceProvider;
import uk.ac.gda.devices.bssc.beans.BSSCSessionBean;
import uk.ac.gda.devices.bssc.beans.TitrationBean;
import uk.ac.gda.devices.hatsaxs.beans.LocationBean;
import uk.ac.gda.devices.hatsaxs.beans.Plate;
import uk.ac.gda.devices.hatsaxs.ui.Column;
import uk.ac.gda.devices.hatsaxs.ui.Column.ColumnHelper;
import uk.ac.gda.devices.hatsaxs.ui.HatsaxsMenu;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public class MeasurementsFieldComposite extends FieldComposite {

	private static final Logger logger = LoggerFactory.getLogger(MeasurementsFieldComposite.class);
	private static final String SAMPLE_TIME_OVERHEAD = "gda.devices.bssc.overhead";
	private static final String MAX_SAMPLE_VOLUME = "gda.devices.bssc.max_sample_volume";

	Object value = null;
	private Table table;
	private Label sampleCount;
	private Label totalRuntime;
	private final TableViewer tableViewer;
	private Composite composite_1;
	private final RichBeanEditorPart rbeditor;

	/** Collection of colours to be disposed of when the view is disposed */
	private Collection<Runnable> colourClearing = new ArrayList<>();

	private boolean isStaff;

	private Map<String, Column<TitrationBean,?>> columns;

	Color okay, warning;

	private static final SimpleObjectTransfer TRANSFER = new SimpleObjectTransfer() {
		private final String TYPE_NAME = "uk.ac.gda.devices.bssc.ui.TitrationBeanTransfer" + System.currentTimeMillis(); //$NON-NLS-1$
		private final int TYPE_ID = registerType(TYPE_NAME);

		@Override
		protected int[] getTypeIds() {
			return new int[] { TYPE_ID };
		}

		@Override
		protected String[] getTypeNames() {
			return new String[] { TYPE_NAME };
		}
	};

	public MeasurementsFieldComposite(Composite parent, int style, RichBeanEditorPart editor) {
		super(parent, style);
		this.rbeditor = editor;
		this.isStaff = InterfaceProvider.getBatonStateProvider().getMyDetails().getAuthorisationLevel() >= 3;
		if (isStaff) {
			logger.debug("Running biosaxs editor with staff authorisation");
		}
		final Display display = Display.getCurrent();
		okay = null;
		warning = new Color(display, 255, 160, 30);

		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);

		final Composite comp = new Composite(this, SWT.NONE);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(layoutData);
		TableColumnLayout layout = new TableColumnLayout();
		comp.setLayout(layout);

		tableViewer = new TableViewer(comp, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		ColumnViewerToolTipSupport.enableFor(tableViewer);
		table = tableViewer.getTable();
		table.setLayoutData(layoutData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		new HatsaxsMenu<TitrationBean>(tableViewer);
		
		// This listener lets the background colour of cells be kept even when the row is selected
		// (instead of being replaced by the selected foreground)
		table.addListener(SWT.EraseItem, (event) -> {
				event.detail &= ~SWT.HOT;
				if ((event.detail & SWT.SELECTED) == 0)
					return;
				GC gc = event.gc;
				Rectangle rect = event.getBounds();
				gc.setForeground(display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
				gc.setBackground(display.getSystemColor(SWT.COLOR_LIST_SELECTION));
				gc.fillRectangle(rect);
				event.detail &= ~SWT.SELECTED;
		});


		columns = new LinkedHashMap<>();
		columns.put("", new Column<TitrationBean, String>(5, tableViewer, rbeditor, READ_ONLY, true) {

			@Override
			public String getRealValue(TitrationBean element) {
				return "";
			}

			@Override
			public void setNewValue(TitrationBean element, String value) {
			}
			@Override
			protected Color getColour(TitrationBean element) {
				LocationBean location = element.getLocation();
				Plate plate = location.getConfig().getPlate(location.getPlate());
				Color colour = plate.getProperty("colour");
				if (colour == null) {
					Display display = Display.getDefault();
					Color newColour = new Color(display, (RGB)plate.getProperty("rgb"));
					plate.setProperty("colour", colour);
					colourClearing.add(() -> {
						plate.setProperty("colour", null);
						newColour.dispose();
					});
					colour = newColour;
				}
				return colour;
			}
		});
		columns.putAll(getLocationColumns("", new ColumnHelper<TitrationBean, LocationBean>() {
			@Override
			public LocationBean getValue(TitrationBean target) {
				return target.getLocation();
			}
			@Override
			public void setValue(TitrationBean target, LocationBean value) {
				target.setLocation(value);
			}
			@Override
			public Color bGColor(TitrationBean element) {
				if (!getValue(element).isValid()) {
					return warning;
				}
				return okay;
			}
		}));
		columns.put("Sample Name",new Column<TitrationBean, String>(100, tableViewer, rbeditor, TEXT) {
			@Override
			public String getRealValue(TitrationBean element) {
				return element.getSampleName();
			}
			@Override
			public void setNewValue(TitrationBean element, String value) {
				element.setSampleName(value);
			}
		});
		columns.put("Viscosity", new Column<TitrationBean, String>(100, tableViewer, rbeditor, "low", "medium", "high") {
			@Override
			public String getRealValue(TitrationBean element) {
				return element.getViscosity();
			}
			@Override
			public void setNewValue(TitrationBean element, String value) {
				element.setViscosity(value);
			}
		});
		columns.put("isBuffer", new Column<TitrationBean, Boolean>(40, tableViewer, rbeditor, BOOL) {
			@Override
			public Boolean getRealValue(TitrationBean element) {
				return element.isBuffer();
			}

			@Override
			public void setNewValue(TitrationBean element, Boolean value) {
				element.setBuffer(value);
			}

		});
		columns.put("Buffers", new Column<TitrationBean, String>(40, tableViewer, rbeditor, TEXT) {
			@Override
			public String getRealValue(TitrationBean element) {
				return element.getBuffers();
			}
			@Override
			public void setNewValue(TitrationBean element, String value) {
				element.setBuffers(value);
			}
			@Override
			protected String getStringValue(TitrationBean element) {
				boolean buf = element.isBuffer();
				return buf ? "--" : element.getBuffers();
			}
		});
//		columns.putAll(getLocationColumns("Buffer\n", new ColumnHelper<TitrationBean, LocationBean>() {
//			@Override
//			public LocationBean getValue(TitrationBean target) {
//				return target.getBufferLocation();
//			}
//			@Override
//			public void setValue(TitrationBean target, LocationBean value) {
//				target.setBufferLocation(value);
//			}
//			@Override
//			public Color bGColor(TitrationBean element) {
//				if (!getValue(element).isValid()) {
//					return warning;
//				}
//				return okay;
//			}
//		}));
		columns.put("Recoup", new Column<TitrationBean, Boolean>(50, tableViewer, rbeditor, BOOL) {
			@Override
			public Boolean getRealValue(TitrationBean element) {
				return element.getRecouperateLocation() != null;
			}
			@Override
			public void setNewValue(TitrationBean element, Boolean value) {
				element.setRecouperateLocation(TRUE.equals(value) ? new LocationBean(BSSC_PLATES) : null);
			}
		});
		Map<String, Column<TitrationBean,?>> recoup = getLocationColumns("Recoup\n", new ColumnHelper<TitrationBean, LocationBean>() {
			@Override
			public LocationBean getValue(TitrationBean target) {
				return target.getRecouperateLocation();
			}
			@Override
			public void setValue(TitrationBean target, LocationBean value) {
				target.setRecouperateLocation(value);
			}
		});
		columns.putAll(recoup);

		columns.put("Time per\nFrame", new Column<TitrationBean, Double>(40, tableViewer, rbeditor, DOUBLE) {
			@Override
			public Double getRealValue(TitrationBean element) {
				return element.getTimePerFrame();
			}
			@Override
			public void setNewValue(TitrationBean element, Double value) {
				element.setTimePerFrame(value);
				updateTotals();
			}
		});
		columns.get("Time per\nFrame").setOutputFormat("%5.3f s");
		columns.put("Frames", new Column<TitrationBean, Integer>(40, tableViewer, rbeditor, INTEGER) {
			@Override
			public Integer getRealValue(TitrationBean element) {
				return element.getFrames();
			}
			@Override
			public void setNewValue(TitrationBean element, Integer value) {
				element.setFrames(value);
				updateTotals();
			}
		});
		columns.put("Exposure\nTemperature", new Column<TitrationBean, Double>(40, tableViewer, rbeditor, DOUBLE) {
			@Override
			public Double getRealValue(TitrationBean element) {
				return element.getExposureTemperature();
			}
			@Override
			public void setNewValue(TitrationBean element, Double value) {
				element.setExposureTemperature(value);
			}
		});
		columns.get("Exposure\nTemperature").setOutputFormat("%4.1f \u00B0C");

		columns.put("Sample\nVolume", new Column<TitrationBean, Double>(40, tableViewer, rbeditor, DOUBLE) {
			@Override
			public Double getRealValue(TitrationBean element) {
				return element.getSampleVolume();
			}

			private double maxSampleVolume() {
				return LocalProperties.getDouble(MAX_SAMPLE_VOLUME, Double.MAX_VALUE);
			}

			private boolean isValidSampleVolume(TitrationBean element) {
				return element.getSampleVolume() <= maxSampleVolume();
			}

			@Override
			public void setNewValue(TitrationBean element, Double value) {
				var max = maxSampleVolume();
				if (value > max) {
					logger.warn(String.format("Attempted to set sample volume %f but this was larger than max (%f); setting max.", value, max));
					value = max;
				}
				element.setSampleVolume(value);
			}

			@Override
			protected Color getColour(TitrationBean element) {
				if (!isValidSampleVolume(element)) {
					return warning;
				}
				return super.getColour(element);
			}
			@Override
			protected String getToolTip(TitrationBean element) {
				if (!isValidSampleVolume(element)) {
					return String.format("Maximum sample volume is %f", LocalProperties.getDouble(MAX_SAMPLE_VOLUME, Double.MAX_VALUE));
				}
				return super.getToolTip(element);
			}
		});
		columns.put("Mode", new Column<TitrationBean, String>(40, tableViewer, rbeditor, TitrationBean.MODES.keySet().toArray(new String[]{})) {
			private int a = 0;
			@Override
			public String getRealValue(TitrationBean element) {
				return element.getMode();
			}
			@Override
			public void setNewValue(TitrationBean element, String value) {
				element.setMode(value);
			}
			@Override
			protected Color getColour(TitrationBean element) {
				if (validKeyMode(element)) {
					return null;
				}
				return warning;
			}
			@Override
			protected String getToolTip(TitrationBean element) {
				if (TitrationBean.MODES.get(element.getMode())) {
					a++;
					System.out.println(a);
					return "Key must be present and >2 samples must be\ndefined with matching key/mode";
				}
				return null;
			}
		});
		columns.put("Key", new Column<TitrationBean, String>(40, tableViewer, rbeditor, TEXT) {

			@Override
			public String getRealValue(TitrationBean element) {
				return element.getKey();
			}

			@Override
			public void setNewValue(TitrationBean element, String value) {
				element.setKey(value);
			}

		});
		columns.put("Delay",  new Column<TitrationBean, Double>(40, tableViewer, rbeditor, DOUBLE) {
			@Override
			public Double getRealValue(TitrationBean element) {
				return element.getDelay();
			}
			@Override
			public void setNewValue(TitrationBean element, Double value) {
				element.setDelay(value);
				updateTotals();
			}
		});
		columns.put("Move", new Column<TitrationBean, Boolean>(25, tableViewer, rbeditor, BOOL) {
			@Override
			public Boolean getRealValue(TitrationBean element) {
				return element.getMove();
			}
			@Override
			public void setNewValue(TitrationBean element, Boolean value) {
				element.setMove(value);
			}
		});
		if (isStaff) {
			columns.put("Visit", new Column<TitrationBean, String>(70, tableViewer, rbeditor, TEXT) {
				private boolean validVisit(TitrationBean element) {
					String visit = element.getVisit();
					HashMap<String, String> overrides = new HashMap<>();
					overrides.put("visit", visit);
					String visitPath = InterfaceProvider.getPathConstructor().createFromProperty(LocalProperties.GDA_VISIT_DIR, overrides);
					File visitDir = new File(visitPath);
					return visitDir.exists() && visitDir.isDirectory();
				}
				@Override
				public String getRealValue(TitrationBean element) {
					return element.getVisit();
				}
				@Override
				public void setNewValue(TitrationBean element, String value) {
					element.setVisit(value);
				}

				@Override
				protected Color getColour(TitrationBean element) {
					if (!validVisit(element)) {
						logger.error("visit doesn't exist");
						return warning;
					} else {
						return super.getColour(element);
					}
				}

				@Override
				protected String getToolTip(TitrationBean element) {
					if (!validVisit(element)) {
						return "Visit directory does not exist\n     (or can't be written to)";
					}
					return super.getToolTip(element);
				}
			});
			columns.put("Username", new Column<TitrationBean, String>(70, tableViewer, rbeditor, TEXT) {
				@Override
				public String getRealValue(TitrationBean element) {
					return element.getUsername();
				}
				@Override
				public void setNewValue(TitrationBean element, String value) {
					element.setUsername(value);
				}
			});
		}

		columns.forEach((name, column) -> column.addToTable(name, tableViewer));
		DragSource source = new DragSource(table, DND.DROP_MOVE | DND.DROP_COPY);
		source.setTransfer(new Transfer[] { TRANSFER, TextTransfer.getInstance() });

		source.addDragListener(new DragSourceAdapter() {
			@Override
			public void dragSetData(DragSourceEvent event) {
				// Get the selected items in the drag source
				DragSource ds = (DragSource) event.widget;
				Table table = (Table) ds.getControl();
				TableItem[] selection = table.getSelection();

				if (TRANSFER.isSupportedType(event.dataType)) {
					List<TitrationBean> data = new ArrayList<TitrationBean>();
					try {
						for (TableItem element : selection) {
							TitrationBean oldBean = (TitrationBean) element.getData();
							TitrationBean copiedBean = (TitrationBean) BeanUtils.cloneBean(oldBean);
							copiedBean.setLocation((LocationBean) BeanUtils.cloneBean(oldBean.getLocation()));
//							copiedBean.setBufferLocation((LocationBean) BeanUtils.cloneBean(oldBean.getBufferLocation()));
							data.add(copiedBean);
						}
					} catch (Exception e) {
						logger.error("error cloning titrationbean for outgoing drag and drop", e);
					}
					event.data = data;
				} else if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
					StringBuffer buff = new StringBuffer();
					for (TableItem element : selection) {
						buff.append(((TitrationBean) element.getData()).getSampleName());
					}
					event.data = buff.toString();
				}
			}

			@Override
			public void dragFinished(DragSourceEvent event) {
				super.dragFinished(event);
				if ((event.detail & DND.DROP_MOVE) != 0) {
					logger.debug(event.toString());
					DragSource ds = (DragSource) event.widget;
					Table table = (Table) ds.getControl();
					TableItem[] selection = table.getSelection();
					if (selection.length == 0)
						logger.debug("selection empty");
					for (TableItem element : selection) {
						if (!getList().remove(element.getData()))
							logger.debug("data not there or not removed");
					}
					updateTotals();
					tableViewer.refresh();
				} else {
					logger.debug("not move");
				}
			}
		});

		// Create the drop target
		DropTarget target = new DropTarget(table, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);
		target.setTransfer(new Transfer[] { TRANSFER });
		target.addDropListener(new DropTargetAdapter() {
			@Override
			public void dragEnter(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					event.detail = (event.operations & DND.DROP_MOVE) != 0 ? DND.DROP_MOVE : DND.DROP_NONE;
				}

				for (TransferData dataType : event.dataTypes) {
					if (TRANSFER.isSupportedType(dataType)) {
						event.currentDataType = dataType;
					}
				}
			}

			@Override
			public void dragOver(DropTargetEvent event) {
				event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
			}

			@Override
			public void drop(DropTargetEvent event) {
				if (TRANSFER.isSupportedType(event.currentDataType)) {
					@SuppressWarnings("unchecked")
					List<TitrationBean> data = (List<TitrationBean>) event.data;
					List<TitrationBean> list = getList();

					int before = 0;

					Widget intoitem = event.item;
					if (intoitem == null) {
						before = list.size();
					} else {
						before = list.indexOf(intoitem.getData());
					}

					list.addAll(before, data);

					updateTotals();
					tableViewer.refresh();
				}
			}
		});
//
		tableViewer.setContentProvider(new ArrayContentProvider());

		composite_1 = new Composite(this, SWT.FILL);
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.marginWidth = 0;
		rowLayout.marginTop = 0;
		rowLayout.marginBottom = 0;
		rowLayout.marginLeft = 0;
		rowLayout.marginRight = 0;
		rowLayout.spacing = 5;
		composite_1.setLayout(rowLayout);

		Label label = new Label(composite_1, SWT.NONE);
		label.setText("Number of Samples:");
		sampleCount = new Label(composite_1, SWT.NONE);
		sampleCount.setText("000000000000");//ensures label is long enough
		sampleCount.setText("0");

		Label runtimeLabel = new Label(composite_1, SWT.NONE);
		runtimeLabel.setText("Total Runtime");

		totalRuntime = new Label(composite_1, SWT.NONE);
		totalRuntime.setText("00:00:00");
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public void setValue(Object value) {
		this.value = value;
		updateTotals();
		tableViewer.setInput(value);
	}

	@SuppressWarnings("unchecked")
	private List<TitrationBean> getList() {
		if (value == null)
			setValue(new ArrayList<TitrationBean>());
		return (List<TitrationBean>) value;
	}

	public void deleteSelection() {
		if (table.getSelectionIndices().length == 0) {
			return;
		}
		int[] selectionIndices = table.getSelectionIndices();
		Arrays.sort(selectionIndices);
		for (int i = selectionIndices.length - 1; i >= 0; i--) {
			getList().remove(selectionIndices[i]);
		}
		rbeditor.valueChangePerformed(new ValueEvent("", ""));
		updateTotals();
		tableViewer.refresh();
	}

	public void addSample() {
		if (table.getSelectionIndices().length == 0) {
			getList().add(new TitrationBean());
		} else {
			int[] selectionIndices = table.getSelectionIndices();
			List<TitrationBean> toadd = new ArrayList<TitrationBean>(table.getSelectionIndices().length);
			for (int i : selectionIndices) {
				try {
					TitrationBean oldBean = getList().get(i);
					TitrationBean copiedBean = (TitrationBean) BeanUtils.cloneBean(oldBean);
					copiedBean.setLocation((LocationBean) BeanUtils.cloneBean(oldBean.getLocation()));
//					copiedBean.setBufferLocation((LocationBean) BeanUtils.cloneBean(oldBean.getBufferLocation()));
					copiedBean.setBuffers(oldBean.getBuffers());
					if (oldBean.getRecouperateLocation() != null) {
						copiedBean.setRecouperateLocation((LocationBean) BeanUtils.cloneBean(oldBean
								.getRecouperateLocation()));
					}

					toadd.add(copiedBean);
				} catch (Exception e) {
				}
			}
			getList().addAll(selectionIndices[selectionIndices.length - 1] + 1, toadd);
		}
		updateTotals();
		tableViewer.refresh();
		rbeditor.valueChangePerformed(new ValueEvent("", ""));
	}

	private Map<String,Column<TitrationBean,?>> getLocationColumns(final String prefix, final ColumnHelper<TitrationBean, LocationBean> helper) {
		Map<String,Column<TitrationBean,?>> columns = new LinkedHashMap<>();

		Column<TitrationBean, String> plateColumn = new Column<TitrationBean, String>(40, tableViewer, rbeditor, CHOICE) {
			private ColumnHelper<TitrationBean, LocationBean> help = helper;
			@Override
			public String getRealValue(TitrationBean element) {
				LocationBean loc = help.getValue(element);
				return loc == null ? "--" : LocationBean.getPlateText(loc.getPlate());
			}
			@Override
			public void setNewValue(TitrationBean element, String value) {
				LocationBean loc = helper.getValue(element);
				if (loc == null) {
					loc = new LocationBean(BSSCSessionBean.BSSC_PLATES);
					helper.setValue(element,loc);
				}
				loc.setPlate(value);
			}
			@Override
			protected String getStringValue(TitrationBean element) {
				return getRealValue(element);
			}
			@Override
			protected Color getColour(TitrationBean tb) {
				return helper.bGColor(tb);
			}
			@Override
			protected String getToolTip(TitrationBean tb) {
				return helper.toolTip(tb);
			}
		};
		String[] plateArray = BSSCSessionBean.BSSC_PLATES.getAvailablePlates();
		plateColumn.setInput(plateArray);
		columns.put(prefix + "Plate", plateColumn);
		var rowColumn = new Column<TitrationBean, Character>(40, tableViewer, rbeditor, CHOICE) {
			@Override
			public Character getRealValue(TitrationBean element) {
				LocationBean loc = helper.getValue(element);
				if (loc == null) {
					return null;
				}
				short currentPlate = loc.getPlate();
				Plate plate = BSSCSessionBean.BSSC_PLATES.getPlate(currentPlate);
				setInput(plate.getRows());
				return loc.getRow();
			}
			@Override
			public void setNewValue(TitrationBean element, Character value) {
				LocationBean loc = helper.getValue(element);
				if (loc == null) {
					loc = new LocationBean(BSSCSessionBean.BSSC_PLATES);
					helper.setValue(element, loc);
				}
				loc.setRow(value);
			}
			@Override
			protected String getStringValue(TitrationBean bean) {
				Character row = getRealValue(bean);
				return row == null ? "--" : String.valueOf(row);
			}
			@Override
			protected Color getColour(TitrationBean tb) {
				return helper.bGColor(tb);
			}
			@Override
			protected String getToolTip(TitrationBean tb) {
				return helper.toolTip(tb);
			}
		};
		rowColumn.setAdapter(value -> {
			var str = value.toString();
			if (str.length() == 1) {
				return str.charAt(0);
			}
			throw new IllegalArgumentException("Row must be single character");
		});
		columns.put(prefix + "Row", rowColumn);
		var column = new Column<TitrationBean, Integer>(40,tableViewer, rbeditor, CHOICE) {
			@Override
			public Integer getRealValue(TitrationBean element) {
				LocationBean loc = helper.getValue(element);
				if (loc == null) {
					return null;
				}
				short currentPlate = loc.getPlate();
				Plate plate = BSSCSessionBean.BSSC_PLATES.getPlate(currentPlate);
				setInput(plate.getColumns());
				return (int) loc.getColumn();
			}
			@Override
			public void setNewValue(TitrationBean element, Integer value) {
				LocationBean loc = helper.getValue(element);
				if (loc == null) {
					loc = new LocationBean(BSSCSessionBean.BSSC_PLATES);
					helper.setValue(element,loc);
				}
				short col = value.shortValue();
				loc.setColumn(col);
			}
			@Override
			protected String getStringValue(TitrationBean bean) {
				Integer plate = getRealValue(bean);
				return plate == null ? "--" : String.valueOf(plate);
			}
			@Override
			protected Color getColour(TitrationBean tb) {
				return helper.bGColor(tb);
			}
			@Override
			protected String getToolTip(TitrationBean tb) {
				return helper.toolTip(tb);
			}
		};
		column.setAdapter(v -> v instanceof Integer ? (Integer)v : Integer.parseInt(v.toString()));
		columns.put(prefix + "Column", column);
		return columns;
	}

	private boolean validKeyMode(TitrationBean sample) {
		String key = sample.getKey();
		String mode = sample.getMode();
		int matches = 0;

		if (!TitrationBean.MODES.get(mode)) {
			//validation is not required
			return true;
		}

		if (key == null || key.isEmpty()) {
			return false;
		}
		for (TitrationBean tb : this.getList()) {
			if (tb.getMode().equals(mode) && tb.getKey().equals(key)) {
				matches++;
			}
		}
		return matches >= 2;
	}

	private void updateTotals() {
		List<TitrationBean> samples = getList();
		sampleCount.setText(String.valueOf(samples.size()));
		int overheadPerSample = LocalProperties.getAsInt(SAMPLE_TIME_OVERHEAD, 90);
		totalRuntime.setText(getRuntimeString(samples,
				tb -> tb.getTimePerFrame() * tb.getFrames() + tb.getDelay(),
				overheadPerSample));
	}
	@Override
	public void dispose() {
		super.dispose();
		colourClearing.forEach(Runnable::run);
	}
}
