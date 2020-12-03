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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.RGBDataset;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import gda.util.TestUtils;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.PNGLoader;

public class ImageCutdownTest {

	private static final String IMAGE_DIR = "testfiles/gda/image/";
	private static final String IMAGE_NAME = "test_image";
	private static final String FULL_IMAGE = IMAGE_DIR + IMAGE_NAME + ".";

	@Rule
	public ExpectedException expectExcept = ExpectedException.none();

	@Test
	public void typeReturnsExpectedValues() {
		String[] validFileExtensions = new String[] {".png", ".jpg", ".jpeg", ".tif", ".tiff"};
		for (String extension : validFileExtensions) {
			ImageCutdown.imageTypeFromFileExtension(IMAGE_NAME + extension);
		}
	}

	@Test
	public void cutdownRejectsInvalidFileTypes() {
		try {
			new ImageCutdown("this/doesnt/have/a/valid/image.extension", null);
		} catch (IllegalArgumentException e) {
			if (e.getMessage().equals("Cutdown supports png, jpg & tiff. Incorrect file type provided: extension")) return;
			fail("expected exception did not occur, and ImageCutdown tried to load the invalid file");
		}
	}

	@Test
	public void cutdownRejectsInvalidCoords() throws ScanFileHolderException {
		expectExcept.expect(IllegalArgumentException.class);

		List<int[][]> coordList = new ArrayList<>();
		coordList.add(new int[][] {{2, 3}, {4, 5}});
		coordList.add(new int[][] {{3, 4, 5}, {1, 2}}); //wrong
		coordList.add(new int[][] {{1, 2}, {3, 4}});

		ImageCutdown imageCutdown = new ImageCutdown("irrelevant.png", null);
		imageCutdown.cutdown(coordList, null);
	}

	@Test
	public void dataHolderFromPathReturnsValidDatasetForEachFileType() {
		try {
			int testImageSize = 1275 * 1028;
			DataHolder dh1 =  new ImageCutdown(FULL_IMAGE + ImageCutdown.PNG_EXTENSION, null).dataHolderFromPath();
			assertEquals(1, dh1.size());
			assertEquals(testImageSize, dh1.getDataset(0).getSize());
			DataHolder dh2 = new ImageCutdown(FULL_IMAGE + ImageCutdown.JPEG_EXTENSION, null).dataHolderFromPath();
			assertEquals(1, dh2.size());
			assertEquals(testImageSize, dh2.getDataset(0).getSize());
			DataHolder dh3 = new ImageCutdown(FULL_IMAGE + ImageCutdown.TIF_EXTENSION, null).dataHolderFromPath();
			assertEquals(1, dh3.size());
			assertEquals(testImageSize, dh3.getDataset(0).getSize());
		} catch (ScanFileHolderException | IllegalArgumentException e) {
			fail("expected exception did not occur, but something else did! " + e);
		}
	}

	@Test
	public void fullCutdownRun() throws ScanFileHolderException {
		String input = new File(FULL_IMAGE + ImageCutdown.PNG_EXTENSION).getAbsolutePath();
		String saveDir = TestUtils.generateDirectorynameFromClassname(ImageCutdownTest.class.getCanonicalName());
		try {
			TestUtils.makeScratchDirectory(saveDir);
		} catch (Exception e1) {
			fail("failed to create scratchdir for test files: " + e1);
		}

		List<int[][]> coordList = new ArrayList<>();
		coordList.add(new int[][] {{200, 200}, {300, 300}});
		coordList.add(new int[][] {{100, 50}, {150, 200}});
		coordList.add(new int[][] {{75, 650}, {175, 750}});

		try {
			ImageCutdown cutter = new ImageCutdown(input, saveDir);
			cutter.cutdown(coordList, "ROI");
		} catch (ScanFileHolderException | IllegalArgumentException e) {
			fail("unexpected exception while testing: " + e);
		}
		/*
		 * Switching jdk appears to have changes how the image is codified, all metadata and image contents are the same
		 * though, so we check what really matters- the contents of the file- rather than the size of the file. We check
		 * this by making sure the RGB content is the same as for a manually cut down image.
		 */
		final String testImageLoc = saveDir + IMAGE_NAME + "_ROI_%s.png";
		final String expectedImageLoc = IMAGE_DIR + IMAGE_NAME + "_trimmed%s.png";
		for (int i = 0; i < 3; i++) {
			final File testImage = new File(String.format(testImageLoc, i));
			assertTrue(testImage.exists());
			final PNGLoader expected = new PNGLoader(String.format(expectedImageLoc, i));
			final PNGLoader actual = new PNGLoader(String.format(testImageLoc, i));
			for (String key : expected.loadFile().getDatasetShapes().keySet()) {
				RGBDataset actualDS = DatasetUtils.cast(RGBDataset.class, actual.loadFile().getDataset(key)) ;
				Dataset expectedDS = expected.loadFile().getDataset(key);
				if (!(expectedDS instanceof RGBDataset)) assertPaletteConsistentWithRGB(expectedDS, actualDS);
				else assertEquals(expectedDS, actualDS);
			}
			assertTrue(testImage.delete());
		}
	}

	/**
	 * Sometimes files chopped off are simple enough that [when saved manually] they are saved as palettes
	 * instead of RGB values. To prove these are the same as what we cut off, we check that each palette image
	 * maps to the same RGB value in the cutdown image.
	 */
	private void assertPaletteConsistentWithRGB(Dataset dSExpected, RGBDataset dSActual) {
		final Map<Integer, Integer> paletteToColourHash = new HashMap<>();
		int[] shape = dSExpected.getShape();
		for (int i = 0; i < shape[0]; i++) {
			for (int j = 0; j < shape[1]; j++) {
				Integer actual = hashColour(dSActual, i, j);
				Integer expected = paletteToColourHash.put(dSExpected.getInt(i, j), actual);
				if (expected != null) assertEquals(expected, actual);
			}
		}

	}

	private Integer hashColour(RGBDataset dataset, int i, int j) {
		return (dataset.getRed(i, j) * 255 + dataset.getGreen(i, j)) * 255 + dataset.getBlue(i, j);
	}

}
