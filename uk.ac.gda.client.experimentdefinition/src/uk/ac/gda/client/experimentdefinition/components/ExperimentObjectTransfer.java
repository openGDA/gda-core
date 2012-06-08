/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.client.experimentdefinition.components;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;

/**
 * Enables ExperimentObjects to be used in UI drag and drop.
 */
public class ExperimentObjectTransfer extends ByteArrayTransfer {

	private static final String MYTYPENAME = "scan_object";
	private static final int MYTYPEID = registerType(MYTYPENAME);
	private static ExperimentObjectTransfer _instance = new ExperimentObjectTransfer();

	public static ExperimentObjectTransfer getInstance() {
		return _instance;
	}

	private ExperimentObjectTransfer() {
	}

	@Override
	public void javaToNative(Object object, TransferData transferData) {
		if (object == null || !(object instanceof IExperimentObject)) {
			return;
		}

		if (isSupportedType(transferData)) {
			IExperimentObject myTypes = (IExperimentObject) object;
			byte[] buffer = ExperimentFactory.getManager(myTypes).getFile().getLocation()
					.toString().getBytes();
			super.javaToNative(buffer, transferData);
		}
	}

	@Override
	public Object nativeToJava(TransferData transferData) {

		if (isSupportedType(transferData)) {
			return ExperimentFactory.getExperimentEditorManager().getSelected();
		}
		return null;
	}

	@Override
	protected String[] getTypeNames() {
		return new String[] { MYTYPENAME };
	}

	@Override
	protected int[] getTypeIds() {
		return new int[] { MYTYPEID };
	}

}
