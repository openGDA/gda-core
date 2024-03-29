/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.device.ui.model;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.richbeans.widgets.internal.GridUtils;
import org.eclipse.richbeans.widgets.menu.CheckableActionGroup;
import org.eclipse.richbeans.widgets.table.ISeriesItemDescriptor;
import org.eclipse.richbeans.widgets.table.SeriesItemView;
import org.eclipse.scanning.api.IModelProvider;
import org.eclipse.scanning.api.IValidator;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.annotation.ui.FieldRole;
import org.eclipse.scanning.api.annotation.ui.FieldUtils;
import org.eclipse.scanning.api.annotation.ui.FieldValue;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.IBoundingBoxModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.ui.CommandConstants;
import org.eclipse.scanning.api.ui.auto.IModelViewer;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.model.ModelPersistAction.PersistType;
import org.eclipse.scanning.device.ui.util.PageUtil;
import org.eclipse.scanning.device.ui.util.ScanRegions;
import org.eclipse.scanning.device.ui.util.ViewUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 * Class for editing an operation model. Shows a table or other
 * relevant GUI for editing the model.
 *
 * This class simply listens to the current selection and shows a GUI for editing
 * it if the selection is an IOperation.
 *
 * You can also call setOperation(...) to programmatically set the editing operation.
 *
 * @author Matthew Gerring
 *
 */
public class ModelViewer<T> implements IModelViewer<T>, ISelectionListener, ISelectionProvider {

	private static final Logger logger = LoggerFactory.getLogger(ModelViewer.class);

	// UI
	private TableViewer viewer;	      // Edits beans with a table of values
	private TypeEditor<T>  typeEditor;   // Edits beans with TypeDescriptor custom editors
	private Composite   content;
	private Composite   validationComposite;
	private Label       validationMessage;
	private ModelPersistAction<T> save, load;

	/**
	 * Caution, the view site may be null.
	 */
	private IViewSite   site;

	// Model
	private T           model;

	// Validation
	private IValidator<Object> validator; // The generator or runnable device etc. for which we are editing the model
	private boolean   validationError = false;
	private ModelValidationException validationException;

	// Services
	private IRunnableDeviceService dservice;

	public ModelViewer() {
		super();
	}

	@Override
	public <V> void setViewSite(V site) {
		this.site = (IViewSite)site;
		if (site != null) this.site.getPage().addSelectionListener(this);
	}


	@Override
	public void dispose() {
		if (PageUtil.getPage()!=null) PageUtil.getPage().removeSelectionListener(this);
	}

	@Override
	public <U> U createPartControl(U ancestor) {

		this.content = new Composite((Composite)ancestor, SWT.NONE);
		content.setLayout(new GridLayout(1, false));
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridUtils.removeMargins(content);

		this.viewer = createTableViewer();

		this.typeEditor = new TypeEditor<>(this, content,  SWT.NONE);
		typeEditor.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridUtils.setVisible(typeEditor, false);

		this.validationComposite = new Composite(content, SWT.NONE);
		validationComposite.setLayout(new GridLayout(2, false));
		validationComposite.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));

		final Label error = new Label(validationComposite, SWT.NONE);
		error.setImage(Activator.getImageDescriptor("icons/error.png").createImage());
		error.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false));

		this.validationMessage = new Label(validationComposite, SWT.WRAP);
		validationMessage.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
		validationMessage.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
		GridUtils.setVisible(validationComposite, false);

		if (PageUtil.getPage()!=null) {
			ISelection selection = PageUtil.getPage().getSelection();
			processWorkbenchSelection(selection); // If model view is selected later but something it can process is the page selection...

		}

		createActions();

		return (U)content;
	}

	private TableViewer createTableViewer() {
		TableViewer tableViewer = new TableViewer(content, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
		tableViewer.setContentProvider(createContentProvider());

		tableViewer.getTable().setLinesVisible(true);
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		// resize the row height using a MeasureItem listener
		tableViewer.getTable().addListener(SWT.MeasureItem, new Listener() {
	        @Override
			public void handleEvent(Event event) {
	            event.height = 24;
	        }
	    });

	    //added 'event.height=rowHeight' here just to check if it will draw as I want
		tableViewer.getTable().addListener(SWT.EraseItem, new Listener() {
	        @Override
			public void handleEvent(Event event) {
	            event.height=24;
	        }
	    });

		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(tableViewer, new FocusCellOwnerDrawHighlighter(tableViewer));
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(tableViewer) {
			@Override
			protected boolean isEditorActivationEvent(
					ColumnViewerEditorActivationEvent event) {
				// TODO see AbstractComboBoxCellEditor for how list is made visible
				return super.isEditorActivationEvent(event)
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && (event.keyCode == KeyLookupFactory
								.getDefault().formalKeyLookup(
										IKeyLookup.ENTER_NAME)));
			}
		};

		TableViewerEditor.create(tableViewer, focusCellManager, actSupport,
				ColumnViewerEditor.TABBING_HORIZONTAL
						| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
						| ColumnViewerEditor.TABBING_VERTICAL
						| ColumnViewerEditor.KEYBOARD_ACTIVATION);



		createColumns(tableViewer);
		createDropTarget(tableViewer);

		tableViewer.getTable().addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.F1) {
					// TODO Help!
				}
				if (e.character == SWT.DEL) {
					try {
						Object ob = ((IStructuredSelection)tableViewer.getSelection()).getFirstElement();
						((FieldValue)ob).set(null);
						tableViewer.setSelection(new StructuredSelection(ob));
						refresh(); // Must do global refresh because might effect units of other parameters.
					} catch (Exception ne) {
						logger.error("Cannot delete item "+tableViewer.getSelection(), ne);
					}

				}
			}
		});

		return tableViewer;
	}

	private void createActions() {

		if (site==null) return;

		List<IContributionManager> mans = Arrays.asList(site.getActionBars().getToolBarManager(), site.getActionBars().getMenuManager());

		// TODO We should be able to switch around different roles
		// and show parameters in the table depending on role.
		CheckableActionGroup group = new CheckableActionGroup();
		createFieldRoleActions(group);

		this.save = new ModelPersistAction<T>(typeEditor, PersistType.SAVE);
		this.load = new ModelPersistAction<T>(typeEditor, PersistType.LOAD);
		ViewUtil.addGroups("save", mans, save, load);
	}

	private void createFieldRoleActions(CheckableActionGroup group) {

		FieldRole[] roles = FieldRole.values();
		for (FieldRole role : roles) {
			IAction action = new Action(role.getLabel(), IAction.AS_CHECK_BOX) {
				@Override
				public void run() {
					setFieldRole(role);
				}
			};
			//action.setImageDescriptor(newImage);
			group.add(action);
		}
	}

	private void setFieldRole(FieldRole simple) {
		// TODO Filter table!

	}

	@Override
	public <U> U getControl() {
		return (U)content;
	}

	private void createDropTarget(TableViewer viewer) {

		final Table table = (Table)viewer.getControl();

		// Create drop target for file paths.
		DropTarget target = new DropTarget(table, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);
		final TextTransfer textTransfer = TextTransfer.getInstance();
		final FileTransfer fileTransfer = FileTransfer.getInstance();
		Transfer[] types = new Transfer[] {fileTransfer, textTransfer};
		target.setTransfer(types);
		target.addDropListener(new DropTargetAdapter() {

			private boolean checkLocation(DropTargetEvent event) {

				if (event.item==null || !(event.item instanceof Item)) {
					return false;
				}

				Item item = (Item)event.item;

				// will accept text but prefer to have files dropped
				Rectangle bounds = ((TableItem)item).getBounds(1);
				Point coordinates = new Point(event.x, event.y);
				coordinates = table.toControl(coordinates);
				if (!bounds.contains(coordinates)) {
					return false;
				}
				return true;
			}

			@Override
			public void drop(DropTargetEvent event) {

				String path = null;
				if (textTransfer.isSupportedType(event.currentDataType)) {
					path = (String)event.data;
				}
				if (fileTransfer.isSupportedType(event.currentDataType)){
					String[] files = (String[])event.data;
					path = files[0];
				}
				if (path==null) return;

				if (!checkLocation(event)) return;

				TableItem item = (TableItem)event.item;

				FieldValue field = (FieldValue)item.getData();
				if (field!=null) {
					if (field.isFileProperty()) {
						try {
							field.set(path);
							refresh();
						} catch (Exception e) {
							logger.error("Cannot set the field "+field.getName()+" with value "+path, e);
						}
					}
				}
			}
		});
	}

	private void createColumns(TableViewer viewer) {

        TableViewerColumn var   = new TableViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Name");
		var.getColumn().setWidth(200);
		var.setLabelProvider(new EnableIfColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((FieldValue)element).getDisplayName();
			}
		});

		var   = new TableViewerColumn(viewer, SWT.LEFT, 1);
		var.getColumn().setText("Value");
		var.getColumn().setWidth(300);

		final ColumnLabelProvider prov = new ModelFieldLabelProvider(this);
		var.setLabelProvider(prov);
		var.setEditingSupport(new ModelFieldEditingSupport(this, viewer));
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void refresh() {
		validate(); // Must be first because refresh() then rerenders the values.
		viewer.refresh();
	}

	@Override
	public void updateModel(T model) {
		T old      = this.model;
		this.model = model;
		validate();
		setSelection(new StructuredSelection(model));
		setSeriesItem(old, model);
	}

	private void setSeriesItem(T old, T model) {

		// Find a view reference claiming to edit the thing which we changed.
		IViewReference ref = Arrays.stream(PageUtil.getPage(site).getViewReferences())
				                      .filter(des->isScanView(des))
				                      .findFirst()
				                      .orElse(null);
		if (ref!=null) {
			SeriesItemView view = (SeriesItemView)ref.getPart(false);
			ISeriesItemDescriptor des = view.find(d->isDescriptor(d, old));
			if (des!=null) {
				try {
					@SuppressWarnings("unchecked")
					IPointGenerator<Object> gen = (IPointGenerator<Object>)des.getSeriesObject();
					gen.setModel(model);
				} catch (Exception ne) {
					logger.error("Problem setting the model of a descriptor after the model changed!", ne);
				}
			}
		}
	}

	private boolean isScanView(IViewReference des) {
		IViewPart part = des.getView(false);
		if (part instanceof SeriesItemView) {
			SeriesItemView view = (SeriesItemView)part;
			return view.isSeriesOf(IPointGenerator.class);
		}
		return false;
	}

	private boolean isDescriptor(ISeriesItemDescriptor des, Object oldModel) {
		try {
			IPointGenerator<?> gen = (IPointGenerator<?>)des.getSeriesObject();
			return gen.getModel().equals(oldModel);
		} catch (Exception e) {
			return false;
		}
	}

	private void validate() {

		if (validator==null) {
			validationError = false;
		} else {
			try {
				validator.validate(model);
				validationError = false;

			} catch (Exception ne) {
				validationException = ne instanceof ModelValidationException ? (ModelValidationException)ne : null;
				if (ne.getMessage()!=null) validationMessage.setText(ne.getMessage());
				validationError = true;
			}
		}
		GridUtils.setVisible(validationComposite, validationError);
		validationComposite.getParent().layout(new Control[]{validationComposite});
	}

	@Override
	public boolean isValid() {
		try {
			validator.validate(model);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		processWorkbenchSelection(selection);
	}

	private void processWorkbenchSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object ob = ((IStructuredSelection)selection).getFirstElement();
			processObject(ob);
		}
	}

	private void processObject(Object ob) {

		if (ob==null) return;
		try {
			if (site != null) site.getActionBars().getStatusLineManager().setErrorMessage(null);

			if (ob instanceof IValidator) setValidator((IValidator<?>)ob);

			// Special case for device information, we read the latest
			if (ob instanceof DeviceInformation) {
				String name = ((DeviceInformation<?>)ob).getName();
				if (dservice==null) dservice = ServiceProvider.getService(IEventService.class)
						.createRemoteService(new URI(CommandConstants.getScanningBrokerUri()), IRunnableDeviceService.class);
				IRunnableDevice<?> device = dservice.getRunnableDevice(name);
				setValidator(device);
			}
			if (ob instanceof IModelProvider) setModel(((IModelProvider<T>)ob).getModel());
			if (ob instanceof IScanPathModel) setModel((T)ob);

			if (ob instanceof IROI && getModel() instanceof IBoundingBoxModel) {
				try {
	                BoundingBox box = ScanRegions.createBoxFromPlot((IScanPointGeneratorModel) model);
				((IBoundingBoxModel)getModel()).setBoundingBox(box);
				refresh();
				} catch (Exception ne) {
					logger.info("Unable to process box from plot!", ne);
				}
			}

		} catch (Exception ne) {
			logger.error("Cannot set model for object "+ob, ne);
			if (site != null) site.getActionBars().getStatusLineManager().setErrorMessage("Cannot connect to server "+ne.getMessage());
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setValidator(IValidator<?> v) {
		if (viewer.getTable().isDisposed()) return;
		this.validator = (IValidator<Object>)v;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setModel(T model) throws Exception {
		if (viewer.getTable().isDisposed()) return;
		if (viewer.isCellEditorActive())    return;
		this.model = model;

		if (save!=null) this.save.setModelClass((Class<T>)model.getClass());
		if (load!=null) this.load.setModelClass((Class<T>)model.getClass());

		// Switch UI as appropriate
		if (typeEditor.isCustomEditor(model)) {
			GridUtils.setVisible(viewer.getTable(), false);
			GridUtils.setVisible(typeEditor,        true);
			typeEditor.setModel(model);
			content.layout(new Control[]{typeEditor});
			// Intentionally no viewer.refresh() in this case.
		} else {
			GridUtils.setVisible(viewer.getTable(), true);
			GridUtils.setVisible(typeEditor,        false);
		    viewer.setInput(model);
			content.layout(new Control[]{viewer.getTable()});
		}
		validate();
	}

	@Override
	public T getModel() {
		return model;
	}

	private IContentProvider createContentProvider() {
		return new IStructuredContentProvider() {
			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

			}

			@Override
			public Object[] getElements(Object inputElement) {

				Object model = null;
				if (inputElement instanceof IPointGenerator) {
					IPointGenerator<IScanPathModel> op = (IPointGenerator<IScanPathModel>)inputElement;
					model = op.getModel();
				} else {
					model = inputElement;
				}
				try {
					final Collection<FieldValue>  col = FieldUtils.getModelFields(model);
					return col.toArray(new FieldValue[col.size()]);
				} catch (Exception ne) {
					return new FieldValue[]{};
				}
			}
		};
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		viewer.addSelectionChangedListener(listener);
	}

	@Override
	public ISelection getSelection() {
		return viewer.getSelection();
	}

	@Override
	public void removeSelectionChangedListener( ISelectionChangedListener listener) {
		viewer.removeSelectionChangedListener(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		viewer.setSelection(selection);
	}

	Composite getTable() {
		return viewer.getTable();
	}

	public boolean isValidationError() {
		return validationError;
	}

	public boolean isValidationError(FieldValue field) {
		if (!validationError) return false;
		if (validationException!=null) {
			return validationException.isField(field); // There is a validation error and this field is it.
		}
		return validationError;
	}


	public void setValidationError(boolean validationError) {
		this.validationError = validationError;
	}

	@Override
	public void applyEditorValue() {
		if (!viewer.isCellEditorActive()) return;
		viewer.applyEditorValue();
	}

}
