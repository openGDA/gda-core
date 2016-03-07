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

package gda.device.scannable;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.Callable;

public class RealPositionCallable implements Callable<Double>, Serializable {
	private RealPositionReader reader;
	private int index;
	public RealPositionCallable(RealPositionReader reader, int index)
	{
		this.reader = reader;
		this.index = index;
	}

	@Override
	public Double call() throws Exception {
		return (Double)this.reader.get(index);
	}

	@SuppressWarnings("unused")
	private void writeObject(java.io.ObjectOutputStream out)throws IOException
    {

    }
	@SuppressWarnings("unused")
	private void readObject( java.io.ObjectInputStream in)throws IOException, ClassNotFoundException
    {

    }

	@Override
	public String toString() {
		return "RealPositionCallable [index=" + index + "]";
	}

}
