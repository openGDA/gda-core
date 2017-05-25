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
import java.util.Collections;
import java.util.List;

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
 * ImageCutdown utility class for trimming images to a specified region or regions.
 * Saves resultant images to a specified directory.
 * Optionally takes a suffix which will be appended to the file names.
 */
public class ImageCutdown {

	private final String imagePath;
	private final String imageName;
	private final String imageType;
	private final String saveDir;


	/* Produces a single trimmed image
	 *
	 * @param imagePath
	 *             full path of image to be processed
	 * @param saveDir
	 *             path of save folder/directory
	 * @param coords
	 *             start and end coordinates of region of interest
	 *             e.g. new int[][] {{50, 50}, {100, 100}}
	 */
	public ImageCutdown(String imagePath, String saveDir, int[][] coords) throws IncorrectFileTypeException, InvalidCoordinatesException, ScanFileHolderException {
		this(imagePath, saveDir, Collections.singletonList(coords), null, false);
	}

	/* Produces a series of images from a single image. Images will be numbered.
	 *
	 * @param imagePath
	 *             full path of image to be processed
	 * @param saveDir
	 *             path of save folder/directory
	 * @param coords
	 *             start and end coordinates of region of interest
	 *             e.g. new int[][] {{50, 50}, {100, 100}}
	 */
	public ImageCutdown(String imagePath, String saveDir, List<int[][]> coords) throws IncorrectFileTypeException, InvalidCoordinatesException, ScanFileHolderException {
		this(imagePath, saveDir, coords, null, false);
	}

	/* Produces a single trimmed image, with suffix appended to file name
	 *
	 * @param imagePath
	 *             full path of image to be processed
	 * @param saveDir
	 *             path of save folder/directory
	 * @param coords
	 *             List of start and end coordinates of region of interest
	 *             e.g. new int[][] {{50, 50}, {100, 100}}
	 * @param suffix
	 *             suffix to be appended to filename
	 */
	public ImageCutdown(String imagePath, String saveDir, int[][] coords, String suffix) throws IncorrectFileTypeException, InvalidCoordinatesException, ScanFileHolderException {
		this(imagePath, saveDir, Collections.singletonList(coords), suffix, false);
	}

	/* Produces multiple trimmed images, with a given suffix and number appended to filename.
	 *
	 * @param imagePath
	 *             full path of image to be processed
	 * @param saveDir
	 *             path of save folder/directory
	 * @param coords
	 *             start and end coordinates of region of interest
	 *             e.g. new int[][] {{50, 50}, {100, 100}}
	 * @param suffix
	 *             suffix to be appended to filename
	 */
	public ImageCutdown(String imagePath, String saveDir, List<int[][]> coords, String suffix) throws IncorrectFileTypeException, InvalidCoordinatesException, ScanFileHolderException {
		this(imagePath, saveDir, coords, suffix, false);
	}

	ImageCutdown(String imagePath, String saveDir, List<int[][]> coords, String suffix, boolean testing) throws IncorrectFileTypeException, InvalidCoordinatesException, ScanFileHolderException {
		this.imagePath = imagePath;
		String snakeSuffix = coords.size() > 1 ? "_" + suffix + "_" : "_" + suffix;
		String name = Paths.get(imagePath).getFileName().toString();
		String nameWithoutExtension = name.substring(0, name.lastIndexOf('.'));
		imageName = nameWithoutExtension + snakeSuffix;
		imageType = imageTypeFromFileExtension(imagePath);
		this.saveDir = saveDir;

		if (!testing) {
			cutdown(coords);
		}
	}

	void cutdown(List<int[][]> coordList) throws InvalidCoordinatesException, ScanFileHolderException, IncorrectFileTypeException {
		for (int[][] coordSet : coordList) {
			if (!isValidCoords(coordSet)) {
				throw new InvalidCoordinatesException("Invalid coordinates " + coordSet.toString() + " provided at index: " + coordList.indexOf(coordSet), coordSet, coordList.indexOf(coordSet));
			}
		}
		Dataset data = dataHolderFromPath().getDataset(0);
		DataHolder result = new DataHolder();

		if (coordList.size() > 1) {
			for (int i = 0; i < coordList.size(); i++) {
				result.addDataset(imageName + i, singleImageSingleROI(data, coordList.get(i)));
				saveDataHolder(result, Paths.get(saveDir, result.getName(0)).toString());
				result.clear();
			}
		} else {
			result.addDataset(imageName, singleImageSingleROI(data, coordList.get(0)));
			saveDataHolder(result, Paths.get(saveDir, result.getName(0)).toString());
		}
	}

	Dataset singleImageSingleROI(Dataset data, int[][] coords) {
		int[] singleStep = new int[] {1,1};
		return data.getSlice(coords[0], coords[1], singleStep);
	}

	DataHolder dataHolderFromPath() throws ScanFileHolderException, IncorrectFileTypeException {
		JavaImageLoader loader;
		if (imageType.equals("png")) {
			loader = new PNGLoader(imagePath);
		} else if (imageType.equals("tif")) {
			loader = new TIFFImageLoader(imagePath);
		} else if (imageType.equals("jpg")) {
			loader = new JPEGLoader(imagePath);
		} else throw new IncorrectFileTypeException("Cutdown supports png, jpg & tif. Incorrect file type provided: " + imageType);
		return loader.loadFile();
	}

	void saveDataHolder(DataHolder dataHolder, String savePath) throws ScanFileHolderException, IncorrectFileTypeException {
 		JavaImageSaver saver;
		if (imageType.equals("png")) {
			saver = new PNGSaver(savePath + ".png");
		} else if (imageType.equals("tif") || (imageType.equals("tiff"))) {
			saver = new TIFFImageSaver(savePath + ".tif");
		} else if (imageType.equals("jpg") || imageType.equals("jpeg")) {
			saver = new JPEGSaver(savePath + ".jpg");
		} else throw new IncorrectFileTypeException("Cutdown supports png, jpg & tiff. Incorrect file type provided: " + imageType);
			saver.saveFile(dataHolder);
	}

	boolean isValidCoords(int[][] coords){
		return (2 == coords.length && 2 == coords[0].length && 2 == coords[1].length);
	}

	String imageTypeFromFileExtension(String filePath) throws IncorrectFileTypeException {
		String type = filePath.substring(filePath.lastIndexOf(".") + 1);
		if (type.equals("png")) {
			return type;
		} else if (type.equals("tif") || type.equals("tiff")) {
			return "tif";
		} else if ( type.equals("jpg") || type.equals("jpeg")) {
			return "jpg";
		} else throw new IncorrectFileTypeException("Cutdown supports png, jpg & tif. Incorrect file type provided: " + type);
	}
}