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

package uk.ac.gda.api.acquisition;

import java.util.function.Predicate;

import uk.ac.gda.common.entity.Document;

/**
 * Predicates for Acquisition documents
 *
 * @author Maurizio Nagni
 */
public class AcquisitionPredicates {


	/**
	 * Check if a {@link Document} is an {@link Acquisition} document
	 *
	 * @param <T> a class extending {@link Acquisition}
	 * @return {@code true} if the document is an {@code Acquisition}, {@code false} otherwise
	 */
	public static final <T extends Document> Predicate<T> isAcquisitionInstance() {
    	return Acquisition.class::isInstance;
    }

    /**
     * Check if the the document is an {@code AcquisitionType#TOMOGRAPHY}
     * @param <T> a document extending {@link Acquisition}
     * @return {@code true} if {@link Acquisition#getType()} is of type {@link AcquisitionType#TOMOGRAPHY}, {@code false} otherwise
     */
    public static final <T extends Acquisition<?>> Predicate<T> isTomographyType() {
    	return a -> AcquisitionType.TOMOGRAPHY.equals(a.getType());
    }

    /**
     * Check if the the document is an {@code AcquisitionType#DIFFRACTION}
     * @param <T> a document extending {@link Acquisition}
     * @return {@code true} if {@link Acquisition#getType()} is of type {@link AcquisitionType#DIFFRACTION}, {@code false} otherwise
     */
    public static final <T extends Acquisition<?>> Predicate<T> isDiffractionType() {
    	return a -> AcquisitionType.DIFFRACTION.equals(a.getType());
    }
}
