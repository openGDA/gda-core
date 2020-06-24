/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package gda.function.lookup;

import static gda.configuration.properties.LocalProperties.GDA_CONFIG;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import gda.configuration.properties.LocalProperties;
import gda.factory.FindableConfigurableBase;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

public abstract class AbstractColumnFile extends FindableConfigurableBase implements IObservable {

	public static final String LOOKUP_TABLE_DIRECTORY_PROPERTY = "gda.function.columnDataFile.lookupDir";

	protected static final String COMMENT_MARK = "#";
	protected static final String COLUMN_DELIMETER = "[, \t]+";

	private String directory = getDefaultLookup();
	private String filename;

	private ObservableComponent observableComponent = new ObservableComponent();

	/**
	 * @param string
	 * @return an array of token positions
	 */
	protected static int[] calculateDecimalPlaces(String[] string) {
		return stream(string)
				.mapToInt(AbstractColumnFile::decimalPlaces)
				.toArray();
	}

	private static int decimalPlaces(String value) {
		 // characters to right of '.' (or 0 if none present)
		if (value == null || value.isEmpty()) return 0;
		return (value.length() - value.indexOf('.') - 1) % value.length();
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		requireNonNull(filename, "Filename must not be null");
		this.filename = filename;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		requireNonNull(directory, "Directory must not be null");
		this.directory = directory;
	}

	public String getPath() {
		if (filename == null) {
			throw new IllegalStateException("Filename has not been set");
		}
		return new File(directory, filename).getAbsolutePath();
	}

	protected String getDefaultLookup() {
		String gdaConfig = LocalProperties.get(GDA_CONFIG);
		String lookupTableFolder = LocalProperties.get(LOOKUP_TABLE_DIRECTORY_PROPERTY,
				gdaConfig + File.separator + "lookupTables");
		return new File(lookupTableFolder).getAbsolutePath();
	}

	/**
	 * Read the column file, filtering out comment lines and empty lines.
	 * @return stream of lines each of which split by {@link #COLUMN_DELIMETER}
	 */
	protected Stream<String[]> readLines() throws IOException {
		return Files.lines(Paths.get(getPath()))
				.filter(line -> !line.isEmpty())
				.filter(line -> !line.startsWith(COMMENT_MARK))
				.map(String::trim)
				.map(line -> line.split(COLUMN_DELIMETER));
	}
}