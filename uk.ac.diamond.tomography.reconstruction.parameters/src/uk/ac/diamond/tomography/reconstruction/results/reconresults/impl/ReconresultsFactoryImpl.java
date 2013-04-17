/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.results.reconresults.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import uk.ac.diamond.tomography.reconstruction.results.reconresults.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ReconresultsFactoryImpl extends EFactoryImpl implements ReconresultsFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static ReconresultsFactory init() {
		try {
			ReconresultsFactory theReconresultsFactory = (ReconresultsFactory)EPackage.Registry.INSTANCE.getEFactory("http://diamond.org/reconresults"); 
			if (theReconresultsFactory != null) {
				return theReconresultsFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new ReconresultsFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ReconresultsFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case ReconresultsPackage.RECON_RESULTS: return createReconResults();
			case ReconresultsPackage.RECONSTRUCTION_DETAIL: return createReconstructionDetail();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ReconResults createReconResults() {
		ReconResultsImpl reconResults = new ReconResultsImpl();
		return reconResults;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ReconstructionDetail createReconstructionDetail() {
		ReconstructionDetailImpl reconstructionDetail = new ReconstructionDetailImpl();
		return reconstructionDetail;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ReconresultsPackage getReconresultsPackage() {
		return (ReconresultsPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static ReconresultsPackage getPackage() {
		return ReconresultsPackage.eINSTANCE;
	}

} //ReconresultsFactoryImpl
