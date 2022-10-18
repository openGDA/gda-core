/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package gda.jython.batoncontrol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import gda.jython.JythonServerFacade;

final class BatonBasedActionApproverTest {

	private BatonBasedActionApprover approver;

	private JythonServerFacade jsf;
	private Boolean approvalGranted;
	private String denialReason;

	@BeforeEach
	void setUp() {
		denialReason = "Not set: Fix the test set up!";
	}

	@AfterEach
	void tearDown() {
		approver = null;
		jsf = null;
		approvalGranted = null;
	}

	@Test
	void testWhenBatonIsHeldThatApprovalIsGranted() {
		givenABatonBasedApprover();
		givenTheBatonIsHeld();
		whenApprovalIsChecked();
		assertTrue(approvalGranted);
	}

	@Test
	void testWhenApprovalIsGrantedThatDenialReasonIsEmpty() {
		givenABatonBasedApprover();
		givenTheBatonIsHeld();
		whenApprovalIsChecked();
		assertTrue(denialReason.isEmpty());
	}

	@Test
	void testWhenBatonIsNotHeldThatApprovalIsDenied() {
		givenABatonBasedApprover();
		givenTheBatonIsNotHeld();
		whenApprovalIsChecked();
		assertFalse(approvalGranted);
	}

	@Test
	void testWhenApprovalIsDeniedThatReasonIsExplained() {
		givenABatonBasedApprover();
		givenTheBatonIsNotHeld();
		whenApprovalIsChecked();
		assertEquals("Baton was not held", denialReason);
	}

	private void givenABatonBasedApprover() {
		jsf = Mockito.mock(JythonServerFacade.class);
		approver = new BatonBasedActionApprover(jsf);
	}

	private void givenTheBatonIsHeld() {
		when(jsf.isBatonHeld()).thenReturn(true);
	}

	private void givenTheBatonIsNotHeld() {
		when(jsf.isBatonHeld()).thenReturn(false);
	}

	private void whenApprovalIsChecked() {
		approvalGranted = approver.actionApproved();
		denialReason = approver.getDenialReason();
	}
}
