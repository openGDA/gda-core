/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package org.eclipse.scanning.device.ui.util;

import java.io.File;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Optional;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSerializationUtil {

	private static final Logger logger = LoggerFactory.getLogger(FileSerializationUtil.class);

	/**
	 * Saves an object to the given file.
	 * If a file already exists, a dialog is shown to ask the user to confirm that they wish
	 * to overwrite the file contents.
	 * If an error occurs, an error dialog is shown and the error is logged.
	 *
	 * @param object the object to save to file
	 * @param filePath the file path to save to
	 */
	public static <T> void saveToFile(T object, String filePath) {
		saveToFile(object, new File(filePath));
	}

	/**
	 * Saves an object to the given file.
	 * If a file already exists, a dialog is shown to ask the user to confirm that they wish
	 * to overwrite the file contents.
	 * If an error occurs, an error dialog is shown and the error is logged.
	 *
	 * @param object the object to save to file
	 * @param file the file to save to
	 */
	public static <T> void saveToFile(T object, File file) {
		if (file.exists() && !confirmOverwrite(file)) return;

		try {
			final String json = ServiceHolder.getMarshallerService().marshal(object);
			Files.write(file.toPath(), json.getBytes());
			file.setWritable(true, false);
		} catch (Exception e) {
			showErrorDialog(file, LoadSave.SAVE, e);
		}
	}

	/**
	 * Loads an object of the given class from the given file.
	 * If an error occurs, an error dialog is shown and the error is logged.
	 *
	 * @param clazz the class of the object to load
	 * @param filePath file file path to load from
	 */
	public static <T> Optional<T> loadFromFile(Class<T> clazz, String filePath) {
		return loadFromFile(clazz, new File(filePath));
	}

	/**
	 * Loads an object of the given class from the given file.
	 * If an error occurs, an error dialog is shown and the error is logged.
	 *
	 * @param clazz the class of the object to load
	 * @param file the file to load from
	 */
	public static <T> Optional<T> loadFromFile(Class<T> clazz, File file) {
		try {
			final String json = new String(Files.readAllBytes(file.toPath()));
			T object = ServiceHolder.getMarshallerService().unmarshal(json, clazz);
			return Optional.of(object);
		} catch (Exception e) {
			showErrorDialog(file, LoadSave.LOAD, e);
			return Optional.empty();
		}
	}

	private static final Shell getShell() {
		return Display.getCurrent().getActiveShell();
	}

	private static boolean confirmOverwrite(File file) {
		return MessageDialog.openConfirm(getShell(), "Confirm Overwrite",
				"Are you sure that you want to overwrite the file '" + file.getName() + "'?");
	}

	private enum LoadSave { LOAD, SAVE }

	private static void showErrorDialog(File file, LoadSave loadSave, Exception e) {
		final String errorMessage = MessageFormat.format("Could not {0} {1} file ''{2}''",
				loadSave.toString().toLowerCase(), loadSave == LoadSave.LOAD ?  "from" : "to", file.getName());
		MessageDialog.openError(getShell(), "Error", errorMessage + " See the log for more details");
		logger.error(errorMessage, e);
	}

}
