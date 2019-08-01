package uk.ac.gda.tomography.scan.editor.view;

import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.tomography.scan.editor.TomographySWTElements;

public class ItemsViewer<T extends Object> extends CompositeTemplate<Map<String, T>> {

	private static final String PLUGIN_ID = "uk.ac.diamond.daq.beamline.k11";

	private static final Logger logger = LoggerFactory.getLogger(ItemsViewer.class);

	private ItemViewerController<T> controller;

	private List itemsList;
	private Button add;
	private Button edit;
	private Button delete;

	public ItemsViewer(Composite parent, int style, Map<String, T> templateData, ItemViewerController<T> controller) {
		super(parent, style, templateData);
		this.controller = controller;
	}

	public int getSelectedIndex() {
		return itemsList.getSelectionIndex();
	}

	@Override
	protected void createElements(int labelStyle, int textStyle) {
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(this);
		GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(this);
		itemsList = TomographySWTElements.createList(this, textStyle, new String[] {}, null, new Point(3, 1), new Point(500, 0));

		add = TomographySWTElements.createButton(this, SWT.PUSH, TomographyMessages.ADD, null);
		add.setImage(TomographySWTElements.getImage(getPluginId(), "icons/plus.png"));
		edit = TomographySWTElements.createButton(this, SWT.PUSH, TomographyMessages.EDIT, null);
		edit.setImage(TomographySWTElements.getImage(getPluginId(), "icons/pencil.png"));
		delete = TomographySWTElements.createButton(this, SWT.PUSH, TomographyMessages.DELETE, null);
		delete.setImage(TomographySWTElements.getImage(getPluginId(),"icons/cross.png"));
	}

	@Override
	protected void initialiseElements() {
		getTemplateData().keySet().forEach(itemsList::add);
	}

	@Override
	protected void bindElements() {
		add.addListener(SWT.Selection, event -> {
			T item = getController().createItem();
			getTemplateData().put(getController().getItemName(item), item);
		});

		edit.addListener(SWT.Selection, event -> {
			if (itemsList.getSelectionIndex() < 0) {
				return;
			}
			getController().editItem(getTemplateData().get(itemsList.getItem(itemsList.getSelectionIndex())));
		});

		delete.addListener(SWT.Selection, event -> {
			if (itemsList.getSelectionIndex() < 0) {
				return;
			}
			getController().deleteItem(getTemplateData().get(itemsList.getItem(itemsList.getSelectionIndex())));
			itemsList.remove(itemsList.getSelectionIndex());
		});

		itemsList.addListener(SWT.Selection, event -> {
			if (itemsList.getSelectionIndex() < 0) {
				return;
			}
			logger.debug(String.format("Event Selected: %s", itemsList.getSelectionIndex()));
			notifyListeners(SWT.Selection, new Event());
		});
	}

	public ItemViewerController<T> getController() {
		return controller;
	}

	private String getPluginId() {
		return PLUGIN_ID;
	}


}
