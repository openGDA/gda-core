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

package gda.data.scan.datawriter;

import gda.scan.IScanDataPoint;

public class TwoDScanRowReverser {
	private int noOfRows;
	private int noOfColumns;
	private boolean reverseOdd;
	private boolean reverseEven;

	public void indexData(IScanDataPoint sdp) {
		int currentRowNumber = findRowNumber(sdp.getCurrentPointNumber());
		if (isOdd(currentRowNumber) && isReverseOdd())
			sdp.setCurrentPointNumber(reversePointNumber(sdp.getCurrentPointNumber()));
		else if (isEven(currentRowNumber) && isReverseEven())
			sdp.setCurrentPointNumber(reversePointNumber(sdp.getCurrentPointNumber()));
	}

	public int reversePointNumber(int currentPointNumber) {
		int xIndex = currentPointNumber % noOfColumns;
		for (int i = 0; i < noOfColumns; i++) {
			if (xIndex == i) {
				int correctNumber = currentPointNumber + ((noOfColumns - 1) - (i * 2));
				return correctNumber;
			}
		}
		return currentPointNumber;
	}

	private int findRowNumber(int currentPointNumber) {
		int rowNumber = currentPointNumber / noOfColumns;
		return rowNumber;
	}

	private boolean isEven(int currentRowNumber) {
		if (currentRowNumber % 2 == 0)
			return true;
		return false;
	}

	private boolean isOdd(int currentRowNumber) {
		if (currentRowNumber % 2 == 0)
			return false;
		return true;
	}

	public int getNoOfRows() {
		return noOfRows;
	}

	public void setNoOfRows(int noOfRows) {
		this.noOfRows = noOfRows;
	}

	public int getNoOfColumns() {
		return noOfColumns;
	}

	public void setNoOfColumns(int noOfColumns) {
		this.noOfColumns = noOfColumns;
	}

	public boolean isReverseOdd() {
		return reverseOdd;
	}

	public void setReverseOdd(boolean reverseOdd) {
		this.reverseOdd = reverseOdd;
	}

	public boolean isReverseEven() {
		return reverseEven;
	}

	public void setReverseEven(boolean reverseEven) {
		this.reverseEven = reverseEven;
	}

}