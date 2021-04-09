/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.client.widgets;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import uk.ac.diamond.daq.experiment.structure.URLFactory;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.images.ClientImages;

/**
 * Selects and consume one file.
 * <p>
 * Displays one button to select a file from a predefined directory.
 * This widget allows also to filter the file types in the directory and consume the selected file
 * </p>
 *
 * @author Maurizio Nagni
 */
public class SelectFileToolItem {

	private static final URLFactory urlFactory = new URLFactory();

	private final ToolItem toolItem;
	private final ClientMessages tooltip;
	private final String[] filterExtensions;
	private Supplier<URL> dirPathSupplier;
	private final Consumer<URL> consumeSelection;



	/**
	 * Instantiates a new tool item
	 *
	 * @param toolBar the parent toolBar
	 * @param tooltip the tooltip associated with the new tool item
	 * @param filterExtensions the suffix used to filter specific file types. May be {@code null}
	 * @param dirPathSupplier the source from where retrieve the files collection
	 * @param consumeSelection the action following the selection of a file
	 */
	public SelectFileToolItem(ToolBar toolBar, ClientMessages tooltip, String[] filterExtensions,
			Supplier<URL> dirPathSupplier, Consumer<URL> consumeSelection) {
		this.toolItem = new ToolItem(toolBar, SWT.PUSH);
		this.tooltip = tooltip;
		this.filterExtensions = filterExtensions;
		this.dirPathSupplier = dirPathSupplier;
		this.consumeSelection = consumeSelection;

		initialize();
	}

	public Supplier<URL> getDirPathSupplier() {
		return dirPathSupplier;
	}

	public String[] getFilterExtensions() {
		return filterExtensions;
	}

	private ToolItem getToolItem() {
		return toolItem;
	}

	private void initialize() {
		getToolItem().setImage(ClientSWTElements.getImage(ClientImages.SELECT_DOCUMENT));
		getToolItem().setToolTipText(ClientMessagesUtility.getMessage(tooltip));
		getToolItem().addSelectionListener(widgetSelectedAdapter(this::load));
	}

	private void load(SelectionEvent event) {
		load();
	}

	private void load() {
		FileDialog fileDialog = new FileDialog(getToolItem().getParent().getShell(), SWT.OPEN);
		Optional.ofNullable(dirPathSupplier.get())
			.map(URL::getFile)
			.ifPresent(fileDialog::setFilterPath);

		Optional.ofNullable(filterExtensions)
			.filter(ArrayUtils::isNotEmpty)
			.ifPresent(fileDialog::setFilterExtensions);

		Optional.ofNullable(fileDialog.open())
			.map(this::generateURL)
			.ifPresent(consumeSelection);
	}

	/**
	 * Generates a URL from a {@code String}
	 *
	 * @param path
	 *            the location to convert
	 * @return the converted string to URL, otherwise {@code null}
	 */
	private URL generateURL(String path) {
		try {
			return urlFactory.generateUrl(path);
		} catch (MalformedURLException e) {
			return null;
		}
	}
}
