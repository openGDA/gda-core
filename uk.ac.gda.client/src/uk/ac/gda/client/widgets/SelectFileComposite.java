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

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.experiment.structure.URLFactory;
import uk.ac.gda.client.composites.ButtonGroupFactoryBuilder;
import uk.ac.gda.ui.tool.ClientMessages;
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
public class SelectFileComposite implements CompositeFactory {

	private static final Logger logger = LoggerFactory.getLogger(SelectFileComposite.class);

	private static final URLFactory urlFactory = new URLFactory();

	private Composite container;

	private final ClientMessages title;
	private final ClientMessages tooltip;
	private final String[] filterExtensions;
	private final URL dirPath;
	private final Consumer<URL> consumeSelection;

	private SelectFileComposite(ClientMessages title, ClientMessages tooltip, String[] filterExtensions,
			URL dirPath, Consumer<URL> consumeSelection) {
		this.title = title;
		this.tooltip = tooltip;
		this.filterExtensions = filterExtensions;
		this.dirPath = dirPath;
		this.consumeSelection = consumeSelection;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		ButtonGroupFactoryBuilder builder = new ButtonGroupFactoryBuilder();
		builder.addButton(title, tooltip,
				widgetSelectedAdapter(this::load), ClientImages.ADD);
		container = builder.build().createComposite(parent, SWT.NONE);
		return container;
	}

	private void load(SelectionEvent event) {
		load();
	}

	private void load() {
		FileDialog fileDialog = new FileDialog(container.getShell(), SWT.OPEN);
		Optional.ofNullable(dirPath)
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

	public static class SelectFileCompositeBuilder {
		private ClientMessages title;
		private ClientMessages tooltip;
		private String[] filterExtensions;
		private URL dirPath;
		private Consumer<URL> consumeSelection;

		public SelectFileCompositeBuilder setLayout(ClientMessages title, ClientMessages tooltip) {
			this.title = title;
			this.tooltip = tooltip;
			return this;
		}

		public SelectFileCompositeBuilder setLogic(URL dirPath, Consumer<URL> consumeSelection) {
			this.dirPath = dirPath;
			this.consumeSelection = consumeSelection;
			return this;
		}

		public SelectFileCompositeBuilder setFilter(String[] filterExtensions) {
			this.filterExtensions = filterExtensions;
			return this;
		}

		public SelectFileComposite build() {
			return new SelectFileComposite(title, tooltip,
					Optional.ofNullable(filterExtensions)
						.orElse(new String[0]),
					dirPath, consumeSelection);
		}
	}
}
