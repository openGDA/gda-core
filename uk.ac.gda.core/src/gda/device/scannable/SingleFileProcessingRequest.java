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

package gda.device.scannable;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;

import gda.device.ProcessingRequestProvider;
import gda.jython.InterfaceProvider;
import uk.ac.gda.api.io.PathConstructor;

/**
 * A simple implementation of {@link ProcessingRequestProvider} that assumes
 * a single key mapping to a single configuration file.
 * <br>
 * The key used is fixed at creation time and will be used for all requests.
 * The configuration file can be relative to a configurable path template
 * (see {@link PathConstructor}), or absolute.
 */
public class SingleFileProcessingRequest extends AbstractScanHook implements ProcessingRequestProvider {

	/** The key used in the Processing Request */
	private final String key;
	/** Path template to provide the base for resolving relative paths */
	private String relativePathBase = "/";
	/**
	 * The path to the configuration file - can be relative (to {{@link #relativePathBase})
	 * or absolute
	 * */
	private String configurationFilePath;

	/** Create ProcessingRequstProvider relating to a given key */
	public SingleFileProcessingRequest(String name, String key) {
		super(name);
		this.key = key;
	}

	@Override
	public Map<String, Collection<Object>> getProcessingRequest() {
		if (configurationFilePath != null && !configurationFilePath.isEmpty()) {
			return singletonMap(key, singletonList(getPath()));
		} else {
			return emptyMap();
		}
	}

	/** Set the (possibly relative) path to the processing configuration file */
	public void setProcesingFile(String path) {
		configurationFilePath = path;
	}

	/** Get the absolute path of the configuration file, taking into account the relative base */
	private String getPath() {
		String base = InterfaceProvider.getPathConstructor().createFromTemplate(relativePathBase);
		return Paths.get(base).resolve(configurationFilePath).toString();
	}

	/** Get the current base used to resolve relative paths */
	public String getRelativePathBase() {
		return relativePathBase;
	}

	/** Set the base path used for resolving relative paths - must be absolute */
	public void setRelativePathBase(String relativePathBase) {
		if (relativePathBase == null) {
			relativePathBase = "/";
		}
		if (!relativePathBase.startsWith("/")) {
			throw new IllegalArgumentException("Base for relative paths must be absolute");
		}
		this.relativePathBase = relativePathBase;
	}
}
