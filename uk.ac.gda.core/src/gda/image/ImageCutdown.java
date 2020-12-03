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

package gda.image;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.january.dataset.Dataset;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.JPEGLoader;
import uk.ac.diamond.scisoft.analysis.io.JPEGSaver;
import uk.ac.diamond.scisoft.analysis.io.JavaImageLoader;
import uk.ac.diamond.scisoft.analysis.io.JavaImageSaver;
import uk.ac.diamond.scisoft.analysis.io.PNGLoader;
import uk.ac.diamond.scisoft.analysis.io.PNGSaver;
import uk.ac.diamond.scisoft.analysis.io.TIFFImageLoader;
import uk.ac.diamond.scisoft.analysis.io.TIFFImageSaver;

/**
 * ImageCutdown utility class for trimming images to a specified region or regions. Saves resultant images to a
 * specified directory.
 */
public class ImageCutdown {

	private final String imagePath;
	private final String imageName;
	private final String imageType;
	private final String saveDir;

	// "png"
	private static final Pattern PNG_MATCHER = Pattern.compile("^png$");
	public static final String PNG_EXTENSION = "png";
	// "jpg" or "jpeg"
	private static final Pattern JPEG_MATCHER = Pattern.compile("^jp(?:e)?g$");
	public static final String JPEG_EXTENSION = "jpg";
	// "tif" or "tiff"
	private static final Pattern TIF_MATCHER = Pattern.compile("^tif(?:f)?$");
	public static final String TIF_EXTENSION = "tiff";

	private static final String INCORRECT_FILETYPE_EXCEPTION = "Cutdown supports png, jpg & tiff. Incorrect file type provided: ";
	private static final String MALFORMED_COORDINATES_EXCEPTION = "Invalid coordinates %s provided at index %s";

	private static final int[] singleStep = { 1, 1 };

	/**
	 * Produces an ImageCutdown prepared with the file to be cut down, and the directory to save output images in. This
	 * can then be used to cut an image to the specified coordinates with the cutdown method.
	 * Output files will match NameOfInputFile<_SuffixIfNotNull>_indexOfCoords.InputFileExtension
	 *
	 * @param imagePath full path of image to be processed
	 *
	 * @param saveDir path of save folder/directory
	 *
	 */

	public ImageCutdown(String imagePath, String saveDir) {
		this.imagePath = imagePath;
		final String name = Paths.get(imagePath).getFileName().toString();
		imageName = name.substring(0, name.lastIndexOf('.'));
		imageType = imageTypeFromFileExtension(imagePath);
		this.saveDir = saveDir;
	}

	/**
	 * Calls cutdown with a single pair of coordinates Output files will match
	 * NameOfInputFile<_SuffixIfNotNull>_0.InputFileExtension therefore this should not be called multiple times with
	 * the same suffix unless intending to overwrite previous runs.
	 *
	 * @param coords
	 *            a 2x2 int array in the order {{ystart_pixels_from_top, xstart_pixels_from_left},
	 *            {yfinish_pixels_from_top, xfinish_pixels_from_left}}
	 * @param suffix
	 *            (optional) String to place after the original filename (will append _0 so will not overwrite original
	 *            image when null)
	 * @throws ScanFileHolderException
	 */

	public void cutdown(int[][] coords, String suffix) throws ScanFileHolderException {
		final List<int[][]> list = new ArrayList<>();
		list.add(coords);
		cutdown(list, suffix);
	}

	/**
	 * Calls cutdown with a multiple pairs of coordinates Output files will match
	 * NameOfInputFile<_SuffixIfNotNull>_index.InputFileExtension therefore this should not be called multiple times with
	 * the same suffix unless intending to overwrite previous runs.
	 *
	 * @param coordList
	 *            a list of 2x2 int array in the order {{ystart_pixels_from_top, xstart_pixels_from_left},
	 *            {yfinish_pixels_from_top, xfinish_pixels_from_left}}
	 * @param suffix
	 *            (optional) String to place after the original filename (will append _index so will not overwrite original
	 *            image when null)
	 * @throws ScanFileHolderException
	 */

	public void cutdown(List<int[][]> coordList, String suffix) throws ScanFileHolderException {
		for (int[][] coordSet : coordList) {
			if (!isValidCoords(coordSet)) {
				throw new IllegalArgumentException(
						String.format(MALFORMED_COORDINATES_EXCEPTION, coordSet, coordList.indexOf(coordSet)));
			}
		}
		final Dataset data = dataHolderFromPath().getDataset(0);
		final DataHolder result = new DataHolder();
		final String formattedName = imageName + ((suffix != null) ? "_" + suffix : "") + "_%s";

		for (int i = 0; i < coordList.size(); i++) {
			int[][] coords = coordList.get(i);
			result.addDataset(String.format(formattedName, i), data.getSlice(coords[0], coords[1], singleStep));
			saveDataHolder(result, Paths.get(saveDir, result.getName(0)).toString());
			result.clear();
		}
	}

	DataHolder dataHolderFromPath() throws ScanFileHolderException {
		final JavaImageLoader loader;
		switch (imageType) {
		case (PNG_EXTENSION):
			loader = new PNGLoader(imagePath);
			break;
		case (TIF_EXTENSION):
			loader = new TIFFImageLoader(imagePath);
			break;
		case (JPEG_EXTENSION):
			loader = new JPEGLoader(imagePath);
			break;
		default:
			throw new IllegalArgumentException(INCORRECT_FILETYPE_EXCEPTION + imageType);
		}
		return loader.loadFile();
	}

	private void saveDataHolder(DataHolder dataHolder, String savePath) throws ScanFileHolderException {
		final JavaImageSaver saver;
		savePath += ".%s";
		switch (imageType) {
		case (PNG_EXTENSION):
			saver = new PNGSaver(String.format(savePath, PNG_EXTENSION));
			break;
		case (TIF_EXTENSION):
			saver = new TIFFImageSaver(String.format(savePath, TIF_EXTENSION));
			break;
		case (JPEG_EXTENSION):
			saver = new JPEGSaver(String.format(savePath, JPEG_EXTENSION));
			break;
		default:
			throw new IllegalArgumentException(INCORRECT_FILETYPE_EXCEPTION + imageType);
		}
		saver.saveFile(dataHolder);
	}

	private boolean isValidCoords(int[][] coords) {
		return (2 == coords.length && 2 == coords[0].length && 2 == coords[1].length);
	}

	static String imageTypeFromFileExtension(String filePath) {
		final String type = filePath.substring(filePath.lastIndexOf(".") + 1);
		Matcher matcher = PNG_MATCHER.matcher(type);
		if (matcher.matches())
			return PNG_EXTENSION;
		matcher = TIF_MATCHER.matcher(type);
		if (matcher.matches())
			return TIF_EXTENSION;
		matcher = JPEG_MATCHER.matcher(type);
		if (matcher.matches())
			return JPEG_EXTENSION;
		throw new IllegalArgumentException(INCORRECT_FILETYPE_EXCEPTION + type);
	}
}