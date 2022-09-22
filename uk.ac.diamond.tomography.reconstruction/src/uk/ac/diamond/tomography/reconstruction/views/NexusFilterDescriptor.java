/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.diamond.tomography.reconstruction.views;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

import uk.ac.diamond.tomography.reconstruction.INexusFilterDescriptor;

public class NexusFilterDescriptor implements INexusFilterDescriptor {
	private static final String[] EMPTY = new String[0];
	private String nexusFilterPath;
	private INexusFilterDescriptor.Operation nexusFilterOperation;
	private String[] nexusFilterOperands;

	private void setSettings(String nexusFilterPath, INexusFilterDescriptor.Operation nexusFilterOperation,
			String[] nexusFilterOperands) {
		// Verify conditions at this point so we don't violate non-null and similar contract returns on
		// INexusFilterDescriptor
		if (nexusFilterPath == null || nexusFilterOperation == null)
			throw new NullPointerException("Filter Path and Filter Operation must not be null");
		if (nexusFilterOperands == null) {
			nexusFilterOperands = EMPTY;
		}
		if (nexusFilterOperands.length != nexusFilterOperation.NUMBER_OF_OPERANDS) {
			throw new IllegalArgumentException("Operand count mismatch");
		}
		for (String string : nexusFilterOperands) {
			if (string == null)
				throw new NullPointerException("Filter Operands array entries must not be null");
		}

		this.nexusFilterPath = nexusFilterPath;
		this.nexusFilterOperation = nexusFilterOperation;
		this.nexusFilterOperands = nexusFilterOperands;
	}

	/**
	 * @param nexusFilterPath
	 *            the Nexus path to perform filtering on
	 * @param nexusFilterOperation
	 *            the operation to perform on the path
	 * @param nexusFilterOperands
	 *            filter operands must be same length as operations NUMBER_OF_OPERANDS field. If NUMBER_OF_OPERANDS == 0
	 *            <code>null</code> can be passed.
	 * @throws NullPointerException
	 *             if any of the required settings in the memento are missing
	 * @throws IllegalArgumentException
	 *             if any of the settings in the memento are incompatible/inconsitent
	 */
	public NexusFilterDescriptor(String nexusFilterPath, INexusFilterDescriptor.Operation nexusFilterOperation,
			String[] nexusFilterOperands) throws NullPointerException, IllegalArgumentException {
		setSettings(nexusFilterPath, nexusFilterOperation, nexusFilterOperands);
	}

	/**
	 * Create a NexusFilterDescriptor from a memento string (previously obtained from {@link #getMementoString()}
	 *
	 * @param string
	 *            memento string
	 * @throws WorkbenchException
	 *             if the memento string is an invalid memento
	 * @throws NullPointerException
	 *             if any of the required settings in the memento are missing
	 * @throws IllegalArgumentException
	 *             if any of the settings in the memento are incompatible/inconsitent
	 */
	public NexusFilterDescriptor(String string) throws WorkbenchException, NullPointerException,
			IllegalArgumentException {
		String nexusFilterPath;
		Operation nexusFilterOperation;
		String[] nexusFilterOperands;
		try (StringReader reader = new StringReader(string)) {

			IMemento memento = XMLMemento.createReadRoot(reader);
			nexusFilterPath = memento.getString("nexusFilterPath");
			nexusFilterOperation = Operation.valueOf(memento.getString("nexusFilterOperation"));
			IMemento ops = memento.getChild("nexusFilterOperands");
			String[] attributeKeys = ops.getAttributeKeys();
			nexusFilterOperands = new String[attributeKeys.length];
			for (int i = 0; i < nexusFilterOperands.length; i++) {
				nexusFilterOperands[i] = ops.getString("op" + i);
			}
		}
		setSettings(nexusFilterPath, nexusFilterOperation, nexusFilterOperands);
	}

	@Override
	public String getNexusFilterPath() {
		return nexusFilterPath;
	}

	@Override
	public Operation getNexusFilterOperation() {
		return nexusFilterOperation;
	}

	@Override
	public String[] getNexusFilterOperands() {
		return nexusFilterOperands;
	}

	@Override
	public String getMementoString() {
		XMLMemento memento = XMLMemento.createWriteRoot("root");
		memento.putString("nexusFilterPath", nexusFilterPath);
		memento.putString("nexusFilterOperation", nexusFilterOperation.toString());
		IMemento ops = memento.createChild("nexusFilterOperands");
		for (int i = 0; i < nexusFilterOperands.length; i++) {
			ops.putString("op" + i, nexusFilterOperands[i]);
		}
		try (StringWriter stringWriter = new StringWriter()) {
			memento.save(stringWriter);
			return stringWriter.toString();
		} catch (IOException e) {
			// unreachable because we are using a StringWriter
			return "";
		}
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.nexusFilterPath).append(this.nexusFilterOperation)
				.append(this.nexusFilterOperands).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof NexusFilterDescriptor))
			return false;
		NexusFilterDescriptor rhs = (NexusFilterDescriptor) obj;
		return new EqualsBuilder().append(this.nexusFilterPath, rhs.nexusFilterPath)
				.append(this.nexusFilterOperation, rhs.nexusFilterOperation)
				.append(this.nexusFilterOperands, rhs.nexusFilterOperands).isEquals();
	}

	@Override
	public String toString() {
		StringBuilder desc = new StringBuilder();
		desc.append(nexusFilterPath + " " + nexusFilterOperation.DESCRIPTION + " ");
		for (int i = 0; i < nexusFilterOperands.length; i++) {
			desc.append(nexusFilterOperands[i]);
			desc.append(" ");
		}
		return desc.toString();
	}
}
