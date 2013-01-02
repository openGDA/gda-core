/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.tomography.parameters.util;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.ecore.EObject;

import uk.ac.gda.tomography.parameters.*;
import uk.ac.gda.tomography.parameters.AlignmentConfiguration;
import uk.ac.gda.tomography.parameters.DetectorBin;
import uk.ac.gda.tomography.parameters.DetectorProperties;
import uk.ac.gda.tomography.parameters.DetectorRoi;
import uk.ac.gda.tomography.parameters.Module;
import uk.ac.gda.tomography.parameters.MotorPosition;
import uk.ac.gda.tomography.parameters.Parameters;
import uk.ac.gda.tomography.parameters.StitchParameters;
import uk.ac.gda.tomography.parameters.TomoExperiment;
import uk.ac.gda.tomography.parameters.TomoParametersPackage;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage
 * @generated
 */
public class TomoParametersAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static TomoParametersPackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TomoParametersAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = TomoParametersPackage.eINSTANCE;
		}
	}

	/**
	 * Returns whether this factory is applicable for the type of the object.
	 * <!-- begin-user-doc -->
	 * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
	 * <!-- end-user-doc -->
	 * @return whether this factory is applicable for the type of the object.
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object object) {
		if (object == modelPackage) {
			return true;
		}
		if (object instanceof EObject) {
			return ((EObject)object).eClass().getEPackage() == modelPackage;
		}
		return false;
	}

	/**
	 * The switch that delegates to the <code>createXXX</code> methods.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected TomoParametersSwitch<Adapter> modelSwitch =
		new TomoParametersSwitch<Adapter>() {
			@Override
			public Adapter caseAlignmentConfiguration(AlignmentConfiguration object) {
				return createAlignmentConfigurationAdapter();
			}
			@Override
			public Adapter caseDetectorBin(DetectorBin object) {
				return createDetectorBinAdapter();
			}
			@Override
			public Adapter caseDetectorProperties(DetectorProperties object) {
				return createDetectorPropertiesAdapter();
			}
			@Override
			public Adapter caseDetectorRoi(DetectorRoi object) {
				return createDetectorRoiAdapter();
			}
			@Override
			public Adapter caseModule(Module object) {
				return createModuleAdapter();
			}
			@Override
			public Adapter caseMotorPosition(MotorPosition object) {
				return createMotorPositionAdapter();
			}
			@Override
			public Adapter caseParameters(Parameters object) {
				return createParametersAdapter();
			}
			@Override
			public Adapter caseStitchParameters(StitchParameters object) {
				return createStitchParametersAdapter();
			}
			@Override
			public Adapter caseTomoExperiment(TomoExperiment object) {
				return createTomoExperimentAdapter();
			}
			@Override
			public Adapter caseScanCollected(ScanCollected object) {
				return createScanCollectedAdapter();
			}
			@Override
			public Adapter defaultCase(EObject object) {
				return createEObjectAdapter();
			}
		};

	/**
	 * Creates an adapter for the <code>target</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param target the object to adapt.
	 * @return the adapter for the <code>target</code>.
	 * @generated
	 */
	@Override
	public Adapter createAdapter(Notifier target) {
		return modelSwitch.doSwitch((EObject)target);
	}


	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.gda.tomography.parameters.TomoExperiment <em>Tomo Experiment</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.gda.tomography.parameters.TomoExperiment
	 * @generated
	 */
	public Adapter createTomoExperimentAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.gda.tomography.parameters.ScanCollected <em>Scan Collected</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.gda.tomography.parameters.ScanCollected
	 * @generated
	 */
	public Adapter createScanCollectedAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.gda.tomography.parameters.Module <em>Module</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.gda.tomography.parameters.Module
	 * @generated
	 */
	public Adapter createModuleAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.gda.tomography.parameters.MotorPosition <em>Motor Position</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.gda.tomography.parameters.MotorPosition
	 * @generated
	 */
	public Adapter createMotorPositionAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.gda.tomography.parameters.DetectorBin <em>Detector Bin</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.gda.tomography.parameters.DetectorBin
	 * @generated
	 */
	public Adapter createDetectorBinAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.gda.tomography.parameters.DetectorProperties <em>Detector Properties</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.gda.tomography.parameters.DetectorProperties
	 * @generated
	 */
	public Adapter createDetectorPropertiesAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.gda.tomography.parameters.DetectorRoi <em>Detector Roi</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.gda.tomography.parameters.DetectorRoi
	 * @generated
	 */
	public Adapter createDetectorRoiAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration <em>Alignment Configuration</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration
	 * @generated
	 */
	public Adapter createAlignmentConfigurationAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.gda.tomography.parameters.Parameters <em>Parameters</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.gda.tomography.parameters.Parameters
	 * @generated
	 */
	public Adapter createParametersAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.gda.tomography.parameters.StitchParameters <em>Stitch Parameters</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.gda.tomography.parameters.StitchParameters
	 * @generated
	 */
	public Adapter createStitchParametersAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for the default case.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @generated
	 */
	public Adapter createEObjectAdapter() {
		return null;
	}

} //TomoParametersAdapterFactory
