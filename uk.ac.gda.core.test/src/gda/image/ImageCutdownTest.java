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
import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import gda.util.TestUtils;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;

public class ImageCutdownTest {

	boolean testing = true;

	String pngPath = new File("testfiles/gda/image/test_image.png").getAbsolutePath();
	String jpgPath = new File("testfiles/gda/image/test_image.jpg").getAbsolutePath();
	String tifPath = new File("testfiles/gda/image/test_image.tiff").getAbsolutePath();

	private final List<int[][]> validCoords = Arrays.asList(new int[][] {{1,1}, {2,2}}, new int[][] {{3,3}, {4,4}} );

	@Rule
	public ExpectedException expectExcept = ExpectedException.none();

	@Test
	public void typeReturnsExpectedValues() {
		try {
			assertEquals("png", new ImageCutdown("/some/directory/I.made/up.tif/fakeimage.png", "irrelevant", validCoords, "irrelevant", testing).imageTypeFromFileExtension(pngPath));
		} catch (IncorrectFileTypeException | InvalidCoordinatesException | ScanFileHolderException e) {
			fail("type threw an exception when it shouldn't have: " + e);
		}
	}

	@Test
	public void cutdownRejectsInvalidFileTypes() throws IncorrectFileTypeException {
		expectExcept.expect(IncorrectFileTypeException.class);
		try {
			new ImageCutdown("this/doesnt/have/a/valid/image.extension", "irrelevant", validCoords, "irrelevant");
		} catch (ScanFileHolderException | InvalidCoordinatesException e) {
			fail("expected exception did not occur, and ImageCutdown tried to load the invalid file");
		}
	}

	@Test
	public void cutdownRejectsInvalidCoords() throws InvalidCoordinatesException {
		expectExcept.expect(InvalidCoordinatesException.class);
		expectExcept.expect(new InvalidCoordsExceptionValsMatch(new int[][] {{3, 4, 5}, {1, 2}}, 1));

		List<int[][]> coordList = new ArrayList<>();
		coordList.add(new int[][] {{2, 3}, {4, 5}});
		coordList.add(new int[][] {{3, 4, 5}, {1, 2}}); //wrong
		coordList.add(new int[][] {{1, 2}, {3, 4}});

		try {
			new ImageCutdown("irrelevant.png", "irrelevant", coordList, null);
		} catch (ScanFileHolderException | IncorrectFileTypeException e) {
			fail("expected exception did not occur, but something else did: " + e);
		}
	}

	@Test
	public void dataHolderFromPathReturnsValidDatasetForEachFileType() {
		try {
			int testImageSize = 1275 * 1028;
			DataHolder dh1 =  new ImageCutdown(pngPath, "irrelevant", validCoords, null, testing).dataHolderFromPath();
			assertEquals(1, dh1.size());
			assertEquals(testImageSize, dh1.getDataset(0).getSize());
			DataHolder dh2 = new ImageCutdown(jpgPath, "irrelevant", validCoords, null, testing).dataHolderFromPath();
			assertEquals(1, dh2.size());
			assertEquals(testImageSize, dh2.getDataset(0).getSize());
			DataHolder dh3 = new ImageCutdown(tifPath, "irrelevant", validCoords, null, testing).dataHolderFromPath();
			assertEquals(1, dh3.size());
			assertEquals(testImageSize, dh3.getDataset(0).getSize());
		} catch (ScanFileHolderException | IncorrectFileTypeException | InvalidCoordinatesException e) {
			fail("expected exception did not occur, but something else did! " + e);
		}
	}

	@Test
	public void fullCutdownRun() {
		String input = new File("testfiles/gda/image/test_image.png").getAbsolutePath();
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
			ImageCutdown cutter = new ImageCutdown(input, saveDir, coordList, "ROI");
		} catch (InvalidCoordinatesException | ScanFileHolderException | IncorrectFileTypeException e) {
			fail("unexpected exception while testing: " + e);
		}
		File testImageResult1 = new File(saveDir + "test_image_ROI_0.png");
		File testImageResult2 = new File(saveDir + "test_image_ROI_1.png");
		File testImageResult3 = new File(saveDir + "test_image_ROI_2.png");
		assertTrue(testImageResult1.exists());
		assertTrue(testImageResult2.exists());
		assertTrue(testImageResult3.exists());
		assertEquals(16791, testImageResult1.length());
		assertEquals(2644, testImageResult2.length());
		assertEquals(8968, testImageResult3.length());
		testImageResult1.delete();
		testImageResult2.delete();
		testImageResult3.delete();
	}

	class InvalidCoordsExceptionValsMatch extends TypeSafeMatcher<InvalidCoordinatesException> {
	    private int[][] coords;
	    private int index;

	    public InvalidCoordsExceptionValsMatch(int[][] coords, int index) {
	        this.coords = coords;
	        this.index = index;
	    }

	    @Override
	    protected boolean matchesSafely(InvalidCoordinatesException invCoExcept) {
	    	boolean res = invCoExcept.index() == index;
	    	for (int i = 0; i < invCoExcept.coords().length; i++) {
	    		for (int j = 0; j < invCoExcept.coords()[i].length; j++) {
	    			if (!(invCoExcept.coords()[i][j] == coords[i][j])) {
	    				res = false;
	    				break;
	    			}
	    		}
	    	}
	        return res;
	    }

	    @Override
	    public void describeTo(Description description) {
	        description.appendText("expects coords: ")
	                .appendValue(coords.toString())
	                .appendText(" and index: ")
	                .appendValue(index);
	    }

	    @Override
	    protected void describeMismatchSafely(InvalidCoordinatesException invCoords, Description mismatchDescription) {
	        mismatchDescription.appendText("was:")
	                .appendValue(invCoords.coords().toString())
	                .appendText(" and: ")
	                .appendValue(index);
	    }
	}
}
