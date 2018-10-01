/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.meta;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.detector.NXDetectorData;
import gda.jython.InterfaceProvider;
import uk.ac.diamond.daq.scm.api.events.NcdMetaType;
import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;

public class NcdMaskLink extends PerVisitExternalNcdMetadata {
	private static final String DEFAULT_INTERNAL_PATH = "/entry/mask/mask";
	private static final Logger logger = LoggerFactory.getLogger(NcdMaskLink.class);
	private String maskName = "pixel_mask";

	public NcdMaskLink() {
		setMetaType(NcdMetaType.MASK);
	}

	@Override
	public void setExternalFile(String filepath, String internal) {
		super.setExternalFile(filepath, Optional.ofNullable(internal).orElse(DEFAULT_INTERNAL_PATH));
	}

	@Override
	protected void checkFile(String path, String internal) {
		TreeFile tree;
		try {
			internal = internal == null ? DEFAULT_INTERNAL_PATH : internal;
			tree = new HDF5Loader(path).loadTree();
			NodeLink maskNode = tree.findNodeLink(internal);
			if (maskNode == null || !maskNode.isDestinationData()) {
				throw new IllegalArgumentException(String.format("Sub path %s does not exist in file", internal));
			}
		} catch (ScanFileHolderException e) {
			throw new IllegalArgumentException("Mask file does not exist", e);
		}
	}

	@Override
	public void write(NXDetectorData dataTree, String treeName) {
		logger.debug("Writing file link to {}", getFilepath());
		HDF5Loader hdf5Loader = new HDF5Loader(getFilepath());
		try {
			hdf5Loader.loadMetadata(null);
		} catch (IOException e) {
			logger.error("Could not load metadata from mask file", e);
			InterfaceProvider.getTerminalPrinter().print("Could not read metadata from mask file: " + e.getMessage());
			return;
		}
		int[] dims = dataTree.getDetTree(treeName).getNode("data").getData().chunkDimensions;
		int[] mdims = hdf5Loader.getMetadata().getDataShapes().get(getInternalPath());
		if (dims != null) {
			int len = dims.length;
			if (!(dims[len-2] == mdims[0] && dims[len-1] == mdims[1])) {
				String msg = String.format("%s - mask dimensions (%s) do not match detector data (%s)",
						getName(),
						Arrays.toString(mdims),
						Arrays.toString(dims));
				logger.error(msg);
				logger.info("{} - Mask file was {}#{}", getName(), getFilepath(), getInternalPath());
				InterfaceProvider.getTerminalPrinter().print(msg);
				return;
			}
		} else {
			logger.warn("{} - Couldn't read dimensions from detector data", getName());
		}
		dataTree.addExternalFileLink(treeName, maskName, String.format("nxfile://%s#%s", getFilepath(), getInternalPath()), false, true);
	}

}
