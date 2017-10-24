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

package uk.ac.gda.client.plotting;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.ResourceComposite;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.plotting.model.LineTraceProviderNode;
import uk.ac.gda.client.plotting.model.LineTraceProviderNode.TraceStyleDetails;
import uk.ac.gda.client.plotting.model.Node;
import uk.ac.gda.client.plotting.model.ScanNode;

public class ScanDataPlotterComposite extends ResourceComposite {
	private static final int HIGHLIGHTED_LINE_WIDTH = 2;

	private static final Logger logger = LoggerFactory.getLogger(ScanDataPlotterComposite.class);

	private IPlottingSystem<Composite> plottingSystem;

	private DataPlotterCheckedTreeViewer dataTreeViewer;
	private final Node rootDataNode;

	private final DataBindingContext dataBindingCtx = new DataBindingContext();
	private final IObservableList selectedList = new WritableList(new ArrayList<Node>(), Node.class);

	private Binding selectionBinding;

	private boolean clearPlotOnStartOfScan = true;


	public ScanDataPlotterComposite(Composite parent, int style, ViewPart parentView, Node rootDataNode) {
		super(parent, style);
		this.rootDataNode = rootDataNode;
		this.setLayout(UIHelper.createGridLayoutWithNoMargin(1, false));
		try {
			plottingSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			UIHelper.showError("Unable to create plotting system", e);
			logger.error("Unable to create plotting system", e);
			return;
		}
		final SashForm composite = new SashForm(this, SWT.HORIZONTAL);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Composite plot = new Composite(composite, SWT.None);
		plot.setLayout(new FillLayout());
		plottingSystem.createPlotPart(plot, parentView.getTitle(),
				parentView.getViewSite().getActionBars(), PlotType.XY, parentView);
		plottingSystem.getSelectedXAxis().setAxisAutoscaleTight(true);

		createDataTree(composite);
		composite.setWeights(new int[] {3, 1});
		setupDataSelection();
	}

	private void setupDataSelection() {
		rootDataNode.getChildren().addListChangeListener(new IListChangeListener() {
			@Override
			public void handleListChange(ListChangeEvent event) {
				event.diff.accept(new ListDiffVisitor() {
					@Override
					public void handleRemove(int index, Object element) {
						//
					}
					@Override
					public void handleAdd(int index, Object element) {
						recentlyAddedSpectraList.clear();
						if (clearPlotOnStartOfScan) {
							for (Object obj : dataTreeViewer.getCheckedElements()) {
								dataTreeViewer.updateCheckSelection(obj, false);
							}
						}
					}
				});
			}
		});
		rootDataNode.addPropertyChangeListener(Node.DATA_ADDED_PROP_NAME, new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				Node node = (Node) evt.getNewValue();

				// Only expand if there have been no lnI0It spectra added
				if (recentlyAddedSpectraList.size() == 0) {
					dataTreeViewer.expandToLevel(node.getParent(), AbstractTreeViewer.ALL_LEVELS);
				}
				if (node instanceof LineTraceProviderNode && dataTreeViewer.getChecked(node)) {
					addTrace((LineTraceProviderNode) node);
				}
			}
		});

		rootDataNode.addPropertyChangeListener(Node.SCAN_ADDED_PROP_NAME, new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				Node node = (Node) evt.getNewValue();
				dataTreeViewer.setSelection(new StructuredSelection(node), true);
			}
		});

		rootDataNode.addPropertyChangeListener(Node.DATA_CHANGED_PROP_NAME, new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				Node node = (Node) evt.getNewValue();
				if (node instanceof LineTraceProviderNode && dataTreeViewer.getChecked(node)) {
					updateTrace((LineTraceProviderNode) node);
				}
			}
		});
	}

	private int maxNumberOfAcquiredSpectraToPlot = 20;
	private ArrayDeque<Node> recentlyAddedSpectraList = new ArrayDeque<Node>();

	/**
	 * De-select items in tree node to reduce number of spectra that are plotted during data acquisition.
	 *
	 * @param node
	 * @since 28/9/2016
	 */
	private void setPlotSelectionStatus(Node node) {
		int numSpectraToPlot = maxNumberOfAcquiredSpectraToPlot / 2;
		// Remove excess items from start of checked nodes list and de-select them in tree view
		if (recentlyAddedSpectraList.size() > maxNumberOfAcquiredSpectraToPlot) {
			while (recentlyAddedSpectraList.size() > numSpectraToPlot) {
				// remove node at *start* of list
				Node removedNode = recentlyAddedSpectraList.remove();
				// de-select item in tree view
				updateDataItemNode(removedNode, false);
				dataTreeViewer.updateCheckSelection(removedNode, false, false);
			}
		}
		recentlyAddedSpectraList.add(node); // add new node to *end* of list
	}

	/**
	 * Validator used to check input in 'Max. number of spectra' dialog box (see {@link #getMaxNumSpectraDialog}).
	 */
	class IntegerValidator implements IInputValidator {
		/**
		 * Validates a string to make sure it's an integer > 0. Returns null for no error, or string with error message
		 *
		 * @param newText
		 * @return String
		 */
		@Override
		public String isValid(String newText) {
			Integer value = null;
			try {
				value = Integer.valueOf(newText);
			} catch (NumberFormatException nfe) {
				// swallow, value==null
			}
			if (value == null || value < 1) {
				return "Number should be an integer > 0";
			}
			return null;
		}
	}

	/**
	 * Return Action with dialog box to allow user to set the maximum number of spectra that will be plotted during data acquisition.
	 *
	 * @return
	 * @since 28/9/2016
	 */
	private Action getMaxNumSpectraDialog() {
		return new Action("Change number of plotted spectra") {
			@Override
			public void run() {
				InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), "Set maximum number of spectra to plot during acqusition",
						"Enter maximum number of spectra to show in plot when aquiring data", String.valueOf(maxNumberOfAcquiredSpectraToPlot),
						new IntegerValidator());
				if (dlg.open() == Window.OK) {
					// User clicked OK; update the label with the input
					Integer userInputInteger = Integer.valueOf(dlg.getValue());
					if (userInputInteger == null || userInputInteger < 1) {
						logger.info("Problem converting user input into number of spectra to plot {}", dlg.getValue());
					} else {
						maxNumberOfAcquiredSpectraToPlot = userInputInteger;
					}
				}
			}
		};
	}
	private void updateTrace(LineTraceProviderNode lineTraceProvider) {
		ILineTrace trace = (ILineTrace) plottingSystem.getTrace(((Node) lineTraceProvider).getIdentifier());
		if (trace != null) {
			trace.setData(lineTraceProvider.getXAxisDataset(), lineTraceProvider.getYAxisDataset());
			plottingSystem.getAxes().get(0).setTitle(lineTraceProvider.getXAxisDataset().getName());
			plottingSystem.getAxes().get(1).setTitle(lineTraceProvider.getYAxisDataset().getName());
			plottingSystem.repaint();
		}
	}

	private void addTrace(LineTraceProviderNode lineTraceProvider) {
		ILineTrace trace = (ILineTrace) plottingSystem.getTrace(((Node) lineTraceProvider).getIdentifier());
		if (trace == null) {
			trace = plottingSystem.createLineTrace(((Node) lineTraceProvider).getIdentifier());
			TraceStyleDetails traceDetails = lineTraceProvider.getTraceStyle();
			if (traceDetails.getColorHexValue() != null) {
				trace.setTraceColor(getTraceColor(traceDetails.getColorHexValue()));
			}
			int lineWidth = traceDetails.getLineWidth();
			if (lineTraceProvider.isHighlighted()) {
				lineWidth += HIGHLIGHTED_LINE_WIDTH;
			}
			trace.setLineWidth(lineWidth);
			trace.setTraceType(traceDetails.getTraceType());
			trace.setPointSize(traceDetails.getPointSize());
			trace.setPointStyle(traceDetails.getPointStyle());
			plottingSystem.addTrace(trace);
		}
		trace.setData(lineTraceProvider.getXAxisDataset(), lineTraceProvider.getYAxisDataset());
		plottingSystem.getSelectedXAxis().setTitle(lineTraceProvider.getXAxisDataset().getName());
		plottingSystem.repaint();
	}

	private final Map<String, Color> nodeColors = new HashMap<String, Color>();

	private Color getTraceColor(String colorValue) {
		Color color = null;
		if (!nodeColors.containsKey(colorValue)) {
			color = UIHelper.convertHexadecimalToColor(colorValue, Display.getDefault());
			nodeColors.put(colorValue, color);
		} else {
			color = nodeColors.get(colorValue);
		}
		return color;
	}

	IObservableFactory dataObservableFactory = new IObservableFactory() {
		/** This receives the data and adds it to the plot view */
		@Override
		public IObservable createObservable(Object target) {
			if (target instanceof LineTraceProviderNode) {
				if (((LineTraceProviderNode) target).isPlotByDefault()) {
					setPlotSelectionStatus((Node) target); // de-select some of the earlier plots if necessary
					dataTreeViewer.updateCheckSelection(target, true); // fires CheckStateChangedEvent
				}
			}
			if (target instanceof Node) {
				return (((Node) target).getChildren());
			}
			return null;
		}
	};

	private void createDataTree(final SashForm parent) {
		Composite dataTreeParent = new Composite(parent, SWT.None);
		dataTreeParent.setLayout(UIHelper.createGridLayoutWithNoMargin(1, false));
		CoolBar composite = new CoolBar(dataTreeParent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		CoolItem toolbarCoolItem = new CoolItem(composite, SWT.NONE);
		ToolBar tb = new ToolBar(composite, SWT.FLAT);

		ToolItem backToolItem = new ToolItem(tb, SWT.NONE);
		backToolItem.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_BACK));
		backToolItem.setToolTipText("Collapse all");

		ToolItem forwardToolItem = new ToolItem(tb, SWT.NONE);
		forwardToolItem.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_FORWARD));
		forwardToolItem.setToolTipText("Expand all");

		ToolItem highlightOnSelectionToolItem = new ToolItem(tb, SWT.CHECK);
		highlightOnSelectionToolItem.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ELCL_SYNCED));
		highlightOnSelectionToolItem.setToolTipText("Highlight selected line trace");

		ToolItem clearPlotOnStartOfScanToolItem = new ToolItem(tb, SWT.CHECK);
		clearPlotOnStartOfScanToolItem.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ETOOL_CLEAR));
		clearPlotOnStartOfScanToolItem.setSelection(clearPlotOnStartOfScan);
		clearPlotOnStartOfScanToolItem.setToolTipText("Clear plot on start of scan");

		Point p = tb.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		tb.setSize(p);
		Point p2 = toolbarCoolItem.computeSize(p.x, p.y);
		toolbarCoolItem.setControl(tb);
		toolbarCoolItem.setSize(p2);

		backToolItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				dataTreeViewer.collapseAll();
			}
		});

		forwardToolItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				dataTreeViewer.expandAll();
			}
		});

		highlightOnSelectionToolItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!((ToolItem) event.widget).getSelection()) {
					selectedList.clear();
					if (selectionBinding != null) {
						dataBindingCtx.removeBinding(selectionBinding);
						selectionBinding.dispose();
						selectionBinding = null;
					}
				} else {
					selectionBinding = dataBindingCtx.bindList(
							ViewersObservables.observeMultiSelection(dataTreeViewer), selectedList);
				}
			}
		});

		clearPlotOnStartOfScanToolItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				clearPlotOnStartOfScan = ((ToolItem) event.widget).getSelection();
			}
		});


		dataTreeViewer = new DataPlotterCheckedTreeViewer(dataTreeParent, SWT.MULTI);
		dataTreeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		dataTreeViewer.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				Object element = cell.getElement();
				cell.setText(element.toString());
				if (element instanceof LineTraceProviderNode) {
					LineTraceProviderNode item = ((LineTraceProviderNode) element);
					String color = item.getTraceStyle().getColorHexValue();
					if (color != null) {
						cell.setForeground(getTraceColor(color));
					}
					return;
				}
			}
		});
		dataTreeViewer.setContentProvider(new ObservableListTreeContentProvider(dataObservableFactory, null));
		dataTreeViewer.setInput(rootDataNode);
		dataTreeViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				Node dataNode = (Node) event.getElement();
				updateSelection(dataNode, event.getChecked());
			}

			private void updateSelection(Node dataNode, boolean checked) {
				if (dataNode.getChildren() == null) {
					updateDataItemNode(dataNode, checked);
				} else {
					for (Object childDataNode : dataNode.getChildren()) {
						updateSelection((Node) childDataNode, checked);
					}
				}
			}
		});

		selectedList.addListChangeListener(new IListChangeListener() {
			@Override
			public void handleListChange(ListChangeEvent event) {
				event.diff.accept(new ListDiffVisitor() {
					@Override
					public void handleRemove(int index, Object element) {
						updateSelection(element, false);
					}

					@Override
					public void handleAdd(int index, Object element) {
						updateSelection(element, true);
					}

					private void updateSelection(Object element, boolean highlighted) {
						if (element instanceof LineTraceProviderNode && dataTreeViewer.getChecked(element)) {
							LineTraceProviderNode lineTraceProvider = (LineTraceProviderNode) element;
							lineTraceProvider.setHighlighted(highlighted);
							removeTrace(lineTraceProvider.getIdentifier());
							addTrace(lineTraceProvider);
						}
					}
				});
			}
		});
		registerViewerContextMenu();
	}

	private void registerViewerContextMenu() {
		final MenuManager menuMgr = new MenuManager();

		Menu menu = menuMgr.createContextMenu(dataTreeViewer.getControl());
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				if (!rootDataNode.getChildren().isEmpty()) {

					menuMgr.add(getMaxNumSpectraDialog());

					menuMgr.add(new Action("Remove All") {
						@Override
						public void run() {
							// Check with user for confirmation - to prevent accidently removing all the plots.
							boolean reallyRemoveAll = MessageDialog.openQuestion(Display.getCurrent().getActiveShell(), "Remove all plots",
									"Do you really want to remove all the plots ?");
							if ( !reallyRemoveAll )
								return;
							for(Object obj : rootDataNode.getChildren()) {
								Node nodeToRemove = (Node) obj;
								if (dataTreeViewer.getChecked(nodeToRemove)) {
									dataTreeViewer.updateCheckSelection(nodeToRemove, false);
								}
							}
							rootDataNode.getChildren().clear();
						}
					});
				}
				if (dataTreeViewer.getSelection().isEmpty() || !isSelectedOnSameNodeType(dataTreeViewer.getSelection())) {
					return;
				}
				final IStructuredSelection selection = (IStructuredSelection) dataTreeViewer.getSelection();
				// FIXME shouldn't have ScanDataNode reference here
				if (selection.getFirstElement() instanceof ScanNode) {
					menuMgr.add(new Action("Remove") {
						@Override
						public void run() {
							Iterator<?> iterator = selection.iterator();
							while(iterator.hasNext()) {
								Node nodeToRemove = (Node) iterator.next();
								if (dataTreeViewer.getChecked(nodeToRemove)) {
									dataTreeViewer.updateCheckSelection(nodeToRemove, false);
								}
								rootDataNode.removeChild(nodeToRemove);
							}
						}
					});
				} else if (selection.getFirstElement() instanceof LineTraceProviderNode) {
					menuMgr.add(new Action("Change appearance") {
						@Override
						public void run() {
							TraceStyleDetails traceStyle = null;
							if (selection.size() == 1) {
								traceStyle = ((LineTraceProviderNode) selection.getFirstElement()).getTraceStyle();
							}
							TraceStyleDialog dialog = new TraceStyleDialog(ScanDataPlotterComposite.this.getShell(), traceStyle);
							dialog.create();
							if (dialog.open() == Window.OK) {
								Iterator<?> iterator = selection.iterator();
								while(iterator.hasNext()) {
									Object selectedNode = iterator.next();
									if (selectedNode instanceof LineTraceProviderNode) {
										LineTraceProviderNode nodeToChange = (LineTraceProviderNode) selectedNode;
										try {
											TraceStyleDetails newTraceStyleDetails = new TraceStyleDetails();
											BeanUtils.copyProperties(newTraceStyleDetails, dialog.getTraceStyle());
											nodeToChange.setTraceStyle(newTraceStyleDetails);
											if (dataTreeViewer.getChecked(nodeToChange)) {
												removeTrace(nodeToChange.getIdentifier());
												addTrace(nodeToChange);
											}
											dataTreeViewer.update(nodeToChange, null);
										} catch (IllegalAccessException | InvocationTargetException e) {
											logger.error("Unable to copy properties", e);
										}
									}
								}
							}
						}
					});
				}
			}

			private boolean isSelectedOnSameNodeType(ISelection iSelection) {
				if (!(iSelection instanceof IStructuredSelection)) {
					return false;
				}
				IStructuredSelection selection = (IStructuredSelection) dataTreeViewer.getSelection();
				Object firstSelection = selection.getFirstElement();
				Iterator<?> iterator = selection.iterator();
				while(iterator.hasNext()) {
					if (!(iterator.next().getClass() == firstSelection.getClass())) {
						return false;
					}
				}
				return true;
			}
		});
		menuMgr.setRemoveAllWhenShown(true);
		dataTreeViewer.getControl().setMenu(menu);
	}

	private void updateDataItemNode(Node dataItemNode, boolean isAdded) {
		if (dataItemNode instanceof LineTraceProviderNode) {
			LineTraceProviderNode lineTraceProvider = (LineTraceProviderNode) dataItemNode;
			if (isAdded) {
				addTrace(lineTraceProvider);
				lineTraceProvider.getScanNode().addSelection(lineTraceProvider.getLabel());
			} else {
				removeTrace(lineTraceProvider.getIdentifier());
				lineTraceProvider.getScanNode().removeSelection(lineTraceProvider.getLabel());
			}
		}
	}

	private void removeTrace(String identifier) {
		ILineTrace trace = (ILineTrace) plottingSystem.getTrace(identifier);
		if (trace != null) {
			plottingSystem.removeTrace(trace);
		}
	}

	@Override
	protected void disposeResource() {
		plottingSystem.dispose();
		for (Color color : nodeColors.values()) {
			color.dispose();
		}
	}

	public IPlottingSystem<Composite> getPlottingSystem() {
		return plottingSystem;
	}
}
