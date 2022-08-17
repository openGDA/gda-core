/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package gda.mscan.processor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import gda.jython.InterfaceProvider;
import uk.ac.gda.api.io.IPathConstructor;

@ExtendWith(MockitoExtension.class)
public class ReRunFromFileElementProcessorTest {

	private final static String TEST_FILE_FOLDER = "testfiles/gda/mscan/processor";
	private ReRunFromFileElementProcessor processor;



	@Mock
	private IPathConstructor pathConstructor;

	@Test
	public void nullFilenamesAreRejected() {
		processor = new ReRunFromFileElementProcessor(null);
		assertThrows(IllegalArgumentException.class, () -> processor.process(null, List.of(processor), 0));
	}

	@Test
	public void blankFilenamesAreRejected() {
		processor = new ReRunFromFileElementProcessor("   ");
		assertThrows(IllegalArgumentException.class, () -> processor.process(null, List.of(processor), 0));
	}

	@Test
	public void nonZeroIndexIsRejected() {
		processor = new ReRunFromFileElementProcessor("dummy.nxs");
		assertThrows(IllegalArgumentException.class, () -> processor.process(null, List.of(processor), 1));
	}

	@Test
	public void emptyProcessorListIssRejected() {
		processor = new ReRunFromFileElementProcessor("dummy.nxs");
		assertThrows(IllegalArgumentException.class, () -> processor.process(null, List.of(), 0));
	}

	@Test
	public void tooLongProcessorListIsRejected() {
		processor = new ReRunFromFileElementProcessor("dummy.nxs");
		assertThrows(IllegalArgumentException.class, () -> processor.process(null, List.of(processor, processor), 0));
	}

	@Test
	public void invalidPathsAreRejected() {
		when(pathConstructor.createFromDefaultProperty()).thenReturn("/the/path");
		try(MockedStatic<InterfaceProvider> provider = Mockito.mockStatic(InterfaceProvider.class)) {
			provider.when(InterfaceProvider::getPathConstructor).thenReturn(pathConstructor);

			processor = new ReRunFromFileElementProcessor("dummy.nxs");
			assertThrows(IllegalArgumentException.class, () -> processor.process(null, List.of(processor), 0));
		}
	}

	@Test
	public void validFilesInVisitDirAreAccepted() {
		when(pathConstructor.createFromDefaultProperty()).thenReturn(TEST_FILE_FOLDER);
		try(MockedStatic<InterfaceProvider> provider = Mockito.mockStatic(InterfaceProvider.class)) {
			provider.when(InterfaceProvider::getPathConstructor).thenReturn(pathConstructor);

			processor = new ReRunFromFileElementProcessor("fake.nxs");
			processor.process(null, List.of(processor), 0);
			assertThat(processor.getElementValue(), is(TEST_FILE_FOLDER + File.separator + processor.getElement()));
		}
	}

	@Test
	public void validAbsoluteFilePathsAreAccepted() {
		try(MockedStatic<InterfaceProvider> provider = Mockito.mockStatic(InterfaceProvider.class)) {
			provider.when(InterfaceProvider::getPathConstructor).thenReturn(pathConstructor);

			processor = new ReRunFromFileElementProcessor(TEST_FILE_FOLDER + File.separator + "fake.nxs");
			processor.process(null, List.of(processor), 0);
			assertThat(processor.getElementValue(), is(processor.getElement()));
		}
	}
}
