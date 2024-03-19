/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.gda.server.exafs.scan.preparers;

import static org.junit.Assert.assertThrows;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import gda.device.Detector;
import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.server.exafs.scan.DetectorPreparer;
import uk.ac.gda.server.exafs.scan.DetectorPreparerDelegate;

public class DetectorPreparerDelegateTest {
	private IOutputParameters outputParams;
	private IDetectorParameters detParams;
	private IScanParameters scanParams;
	private String path;

	@Before
	public void setup() {
		outputParams = Mockito.mock(IOutputParameters.class);
		detParams = Mockito.mock(IDetectorParameters.class);
		scanParams = Mockito.mock(IScanParameters.class);
		path = "path";
	}

	@Test
	public void testDelegateOrder() throws Exception {
		DetectorPreparer detPrep1 = Mockito.mock(DetectorPreparer.class);
		DetectorPreparer detPrep2 = Mockito.mock(DetectorPreparer.class);

		DetectorPreparerDelegate delegate = new DetectorPreparerDelegate();
		delegate.setPreparers(List.of(detPrep1, detPrep2));

		delegate.runConfigure(scanParams, detParams, outputParams, path);
		delegate.runBeforeEachRepetition();
		delegate.runCompleteCollection();

		InOrder inorder = Mockito.inOrder(detPrep1, detPrep2);
		inorder.verify(detPrep1).configure(scanParams, detParams, outputParams, path);
		inorder.verify(detPrep2).configure(scanParams, detParams, outputParams, path);

		inorder.verify(detPrep1).beforeEachRepetition();
		inorder.verify(detPrep2).beforeEachRepetition();

		inorder.verify(detPrep1).completeCollection();
		inorder.verify(detPrep2).completeCollection();
	}

	@Test
	public void testDelegateOrderWithThrownExceptions() {
		DetectorPreparer detPrep1 = new ExceptionThrowingPreparer();
		DetectorPreparer detPrep2 = Mockito.mock(DetectorPreparer.class);

		DetectorPreparerDelegate delegate = new DetectorPreparerDelegate();
		delegate.setPreparers(List.of(detPrep1, detPrep2));

		// Switch on exception throwing
		delegate.setThrowExceptions(true);

		// Exception is thrown by detPrep1 and there should be no interactions with detPrep2
		InOrder inorder = Mockito.inOrder(detPrep2);
		assertThrows(Exception.class, () -> delegate.runConfigure(scanParams, detParams, outputParams, path));
		inorder.verifyNoMoreInteractions();

		assertThrows(Exception.class, () -> delegate.runBeforeEachRepetition());
		inorder.verifyNoMoreInteractions();
	}

	@Test
	public void testDelegateOrderWithCaughtExceptions() throws Exception {
		DetectorPreparer detPrep1 = Mockito.mock(DetectorPreparer.class);
		DetectorPreparer detPrep2 = new ExceptionThrowingPreparer();
		DetectorPreparer detPrep3 = Mockito.mock(DetectorPreparer.class);

		DetectorPreparerDelegate delegate = new DetectorPreparerDelegate();
		delegate.setPreparers(List.of(detPrep1, detPrep2, detPrep3));

		// Switch off exception throwing
		delegate.setThrowExceptions(false);

		// Exception is thrown by detPrep2 and caught, detPrep1 and 3 methods should get called
		InOrder inorder = Mockito.inOrder(detPrep1, detPrep3);
		delegate.runConfigure(scanParams, detParams, outputParams, path);
		delegate.runBeforeEachRepetition();
		delegate.runCompleteCollection();

		inorder.verify(detPrep1).configure(scanParams, detParams, outputParams, path);
		inorder.verify(detPrep3).configure(scanParams, detParams, outputParams, path);

		inorder.verify(detPrep1).beforeEachRepetition();
		inorder.verify(detPrep3).beforeEachRepetition();

		inorder.verify(detPrep1).completeCollection();
		inorder.verify(detPrep3).completeCollection();
	}

	/**
	 * DetectorPreparer for unit tests that throws an exception from the configure and beforeEachRepetition methods.
	 */
	private class ExceptionThrowingPreparer implements DetectorPreparer {

		@Override
		public void configure(IScanParameters scanBean, IDetectorParameters detectorBean, IOutputParameters outputBean,
				String experimentFullPath) throws Exception {
			throw new Exception();
		}

		@Override
		public void beforeEachRepetition() throws Exception {
			throw new Exception();
		}

		@Override
		public void completeCollection() {
		}

		@Override
		public Detector[] getExtraDetectors() {
			return null;
		}
	}
}
