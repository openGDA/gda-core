/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.excalibur.equalization;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Class used in manipulating data  in arrays that is from an array of chips
 */
public class ChipSet {
	public static final int chipHeight = 256;
	public static final int chipWidth = 256;
	public static final int chipPixels = chipHeight*chipWidth;
	
	public static long getChipTopPixel(long chipRow){
		switch ((int)chipRow){
		case 0:
			return 0;
		case 1:
			return chipHeight+3;
		case 2: 
			return 2*chipHeight + 3 + 124;
		case 3:
			return 3*chipHeight + 3 + 124 + 3;
		case 4:
			return 4*chipHeight + 3 + 124 + 3 + 124;
		case 5:
			return 5*chipHeight + 3 + 124 + 3 + 124 + 3;
		default:
			throw new IllegalArgumentException("chipRow must be between 0 and 5");
		}
	}
	
	public static long getChipBottomPixel(long chipRow){
		return getChipTopPixel(chipRow)+chipHeight-1;
	}

	public static long getChipLeftPixel(long chipColumn){
		return chipColumn*(chipWidth+3);
	}
	public static long getChipRightPixel(long chipColumn){
		return getChipLeftPixel(chipColumn) + chipWidth-1;
	}	
	
	public static long getPixelsPerRow(long columns){
		return ChipSet.getChipRightPixel(columns-1)+1;
	}
	

	private final boolean[] present;
	List<Chip> chips;
	public final int columns;
	public final int rows;
	public final long pixelsPerRow;

	ChipSet( int rows, int columns, boolean [] present){
		this.rows = rows;
		this.columns = columns;
		this.present = present;
		this.pixelsPerRow = ChipSet.getPixelsPerRow(columns);
		
		
	}
	
	public List<Chip> getChips(){
		if( chips == null){
			List<Chip> tmp = new Vector<Chip>();
			for( int i=0; i< rows; i++){
				for( int j=0; j< columns; j++){
					if( present[i*columns + j])
						tmp.add( new Chip(this, columns, i,j));
				}
			}
			chips = tmp;
		}
		return chips;
	}
}
class Chip {

	public final int row;
	public final int column;
	public final int index;
	private final ChipSet chipSet;
	

	public Chip(ChipSet chipSet, int columns, int row, int column) {
		this.chipSet = chipSet;
		this.row = row;
		this.column = column;
		index = row * columns + column;
	}
	
	/**
	 * 
	 * @return index into 2d array of chips 
	 */
	int getIndex(){
		return index;
	}
	
	long getChipTopPixel(){
		return ChipSet.getChipTopPixel(row);
	}
	
	long getChipBottomPixel(){
		return ChipSet.getChipBottomPixel(row);
	}

	long getChipLeftPixel(){
		return ChipSet.getChipLeftPixel(column);
	}
	long getChipRightPixel(){
		return ChipSet.getChipRightPixel(column);
	}
	
	public Iterator<Long> getPixelIndexIterator(){
		return new PixelIndexIterator(this);
	}
	
	public long getPixelsPerRow(){
		return chipSet.pixelsPerRow;
	}
	

}


class PixelIndexIterator implements Iterator<Long> {

	private final Chip chip;
	int row=0;
	int col=0;
	long pixelindex;
	long pixelsStepBetweenRows;

	public PixelIndexIterator(Chip chip) {
		this.chip = chip;
		pixelindex = chip.getChipTopPixel() + chip.getChipLeftPixel();
	}

	@Override
	public boolean hasNext() {
		return row<ChipSet.chipHeight;
	}

	@Override
	public Long next() {
		col+=1;
		pixelindex+=1;
		if( col == ChipSet.chipWidth){
			col=0;
			row++;
			pixelindex-=ChipSet.chipWidth;
			pixelindex+=chip.getPixelsPerRow();
		}
		return pixelindex;
	}

	@Override
	public void remove() {
		//do nothing
	}

}
