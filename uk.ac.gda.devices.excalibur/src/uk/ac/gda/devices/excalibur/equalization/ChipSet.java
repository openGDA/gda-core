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

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.io.ILazyLoader;

/**
 * Class used in manipulating data  in arrays that is from an array of chips
 */
public class ChipSet {
	public static final int chipHeight = 256;
	public static final int chipWidth = 256;
	public static final int chipPixels = chipHeight*chipWidth;
	
	/**
	 * 
	 * @param chipRow
	 * @return Returns value in row direction of top row of pixels in a chip. 0 is value for first row ( top most)
	 */
	public static int getChipTopPixel(long chipRow){
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
	
	/**
	 * 
	 * @param chipRow
	 * @return Returns value in row direction of bottom row of pixels in a chip. chipHeight-1 is value for first row ( top most)
	 */
	public static int getChipBottomPixel(int chipRow){
		return getChipTopPixel(chipRow)+chipHeight-1;
	}

	/**
	 * 
	 * @param chipColumn
	 * @return Returns value in column direction of left column of pixels in a chip. 0 is value for first column ( left most)
	 */
	public static int getChipLeftPixel(int chipColumn){
		return chipColumn*(chipWidth+3);
	}

	/**
	 * 
	 * @param chipColumn
	 * @return Returns value in column direction of right column of pixels in a chip. chipWidth -1 is value for first column ( left most)
	 */
	public static int getChipRightPixel(int chipColumn){
		return getChipLeftPixel(chipColumn) + chipWidth-1;
	}	
	
	/**
	 * 
	 * @param columns
	 * @return number of pixels per row of pixels of a chipset with number of columns = columns. If columns = 1 return chipWidth
	 */
	public static int getPixelsPerRow(int columns){
		return ChipSet.getChipRightPixel(columns-1)+1;
	}

	/**
	 * 
	 * @param rows
	 * @return number of pixels per column of pixels of a chipset with number of rows = rows. If rows = 1 return chipHeight
	 */
	public static int getPixelsPerCol(int rows){
		return ChipSet.getChipBottomPixel(rows-1)+1;
	}

	private final boolean[] present;
	List<Chip> chips;
	public final int columns;
	public final int rows;

	/**
	 * number of pixels per row of pixels of the chipset
	 * 
	 */
	public final long pixelsPerRow;

	
	/**
	 * number of pixels per column of pixels of a chipset
	 */
	public final long pixelsPerCol;
	public final int numChips;

	public ChipSet( int rows, int columns, boolean [] present){
		this.rows = rows;
		this.columns = columns;
		this.present = present != null ? present :  new boolean[rows*columns];
		if(present == null){
			Arrays.fill(this.present, true);
		}
		numChips =rows * columns;
		pixelsPerRow = ChipSet.getPixelsPerRow(columns);
		pixelsPerCol = ChipSet.getPixelsPerCol(rows);
	}
	public ChipSet( int rows, int columns){
		this.rows = rows;
		this.columns = columns;
		numChips =rows * columns;
		this.present = new boolean[rows*columns];
		Arrays.fill(present, true);
		pixelsPerRow = ChipSet.getPixelsPerRow(columns);
		pixelsPerCol = ChipSet.getPixelsPerCol(rows);
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

	public void checkLoaderShape(int[] shape) throws IllegalArgumentException{
		if( shape.length != 2){
			throw new IllegalArgumentException("shape.length != 2");
		}
		long fullHeight = shape[0];
		//check data is at least as large as required
		if(fullHeight < pixelsPerCol ){
			throw new IllegalArgumentException("fullHeight (" + fullHeight + ") < reqdHeight (" + pixelsPerCol +")");
		}
		long fullWidth = shape[1];
		if(fullWidth != pixelsPerRow){
			throw new IllegalArgumentException("fullWidth != reqdWidth: " + pixelsPerRow);
		}
	}
	
	public long[] getDims(){
		return new long[]{rows, columns};
	}
	
	/**
	 * 
	 * @return dimensions of the pixel array represented by this ChipSet
	 */
	public long[] getPixelsDims(){
		return new long[]{ pixelsPerCol, pixelsPerRow};
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
	
	/**
	 * 
	 * @return Returns value in row direction of top row of pixels in a chip. 0 is value for first row ( top most)
	 */
	int getChipTopPixel(){
		return ChipSet.getChipTopPixel(row);
	}
	
	/**
	 * 
	 * @return Returns value in row direction of bottom row of pixels in a chip. chipHeight-1 is value for first row ( top most)
	 */
	int getChipBottomPixel(){
		return ChipSet.getChipBottomPixel(row);
	}

	/**
	 * 
	 * @return Returns value in column direction of left column of pixels in a chip. 0 is value for first column ( left most)
	 */
	int getChipLeftPixel(){
		return ChipSet.getChipLeftPixel(column);
	}
	
	/**
	 * 
	 * @return Returns value in column direction of right column of pixels in a chip. chipWidth -1 is value for first column ( left most)
	 */
	int getChipRightPixel(){
		return ChipSet.getChipRightPixel(column);
	}
	
	public Iterator<Long> getPixelIndexIterator(){
		return new PixelIndexIterator(this);
	}
	
	/**
	 * @return number of pixels per row of pixels of the chipset of which this chip is a member
	 * 
	 */
	public long getPixelsPerRow(){
		return chipSet.pixelsPerRow;
	}

	public IDataset getDataset(ILazyLoader loader) throws IOException {
		return getDataset(loader,0);
	}

	public IDataset getDataset(ILazyLoader loader, int length3d) throws IOException {
		int dim = length3d == 0? 2  : 3;
		int[] start = new int[dim]; 
		int[] stop = new int[dim]; 
		int[] step= new int[dim];
		step[0]=step[1]=1;		
		start[0] = getChipTopPixel();
		stop[0] = (start[0]+ChipSet.chipHeight); //exclusive
		start[1] = getChipLeftPixel();
		stop[1] = (start[1] + ChipSet.chipWidth); //exclusive
		if( dim == 3){
			step[2] = 1;
			start[2] = 0;
			stop[3] = length3d;
		}
		return loader.getDataset(null, new SliceND(stop.clone(), start, stop, step));
	}
}

/**
 * Class that can be used to iterate through all the pixels of a chip
 * The value returned by next is the index into the 1d array that represents the pixels of the chipset
 * of which the chip is a member
 */
class PixelIndexIterator implements Iterator<Long> {

	private final Chip chip;
	int row=0;
	int col=-1;
	
	/**
	 * index into the 1d array that represents the pixels of the chipset of which the chip is a member
	 */
	long pixelindex;
	long pixelsStepBetweenRows;

	public PixelIndexIterator(Chip chip) {
		this.chip = chip;
		pixelindex = chip.getChipTopPixel()*chip.getPixelsPerRow() + chip.getChipLeftPixel()-1;//subtract 1 as the value is read after callig next which adds 1
	}

	@Override
	public boolean hasNext() {
		col+=1;
		pixelindex+=1;
		if( col == ChipSet.chipWidth){
			col=0;
			row++;
			pixelindex-=ChipSet.chipWidth;
			pixelindex+=chip.getPixelsPerRow();
		}
		return row<ChipSet.chipHeight;
	}
	@Override
	public Long next() {
		return pixelindex;
	}

	@Override
	public void remove() {
		//do nothing
	}

}
