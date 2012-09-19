/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.localtomo.util;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

import uk.ac.diamond.tomography.localtomo.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see uk.ac.diamond.tomography.localtomo.LocalTomoPackage
 * @generated
 */
public class LocalTomoAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static LocalTomoPackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public LocalTomoAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = LocalTomoPackage.eINSTANCE;
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
	protected LocalTomoSwitch<Adapter> modelSwitch =
		new LocalTomoSwitch<Adapter>() {
			@Override
			public Adapter caseBeamlineType(BeamlineType object) {
				return createBeamlineTypeAdapter();
			}
			@Override
			public Adapter caseClusterType(ClusterType object) {
				return createClusterTypeAdapter();
			}
			@Override
			public Adapter caseDocumentRoot(DocumentRoot object) {
				return createDocumentRootAdapter();
			}
			@Override
			public Adapter caseFilenameFmtType(FilenameFmtType object) {
				return createFilenameFmtTypeAdapter();
			}
			@Override
			public Adapter caseImagekeyencodingType(ImagekeyencodingType object) {
				return createImagekeyencodingTypeAdapter();
			}
			@Override
			public Adapter caseImgkeyNXSPathType(ImgkeyNXSPathType object) {
				return createImgkeyNXSPathTypeAdapter();
			}
			@Override
			public Adapter caseIxxType(IxxType object) {
				return createIxxTypeAdapter();
			}
			@Override
			public Adapter caseLocalTomoType(LocalTomoType object) {
				return createLocalTomoTypeAdapter();
			}
			@Override
			public Adapter caseNexusfileType(NexusfileType object) {
				return createNexusfileTypeAdapter();
			}
			@Override
			public Adapter caseQsubType(QsubType object) {
				return createQsubTypeAdapter();
			}
			@Override
			public Adapter caseSettingsfileType(SettingsfileType object) {
				return createSettingsfileTypeAdapter();
			}
			@Override
			public Adapter caseShutterClosedPhysType(ShutterClosedPhysType object) {
				return createShutterClosedPhysTypeAdapter();
			}
			@Override
			public Adapter caseShutterNXSPathType(ShutterNXSPathType object) {
				return createShutterNXSPathTypeAdapter();
			}
			@Override
			public Adapter caseShutterOpenPhysType(ShutterOpenPhysType object) {
				return createShutterOpenPhysTypeAdapter();
			}
			@Override
			public Adapter caseShutterType(ShutterType object) {
				return createShutterTypeAdapter();
			}
			@Override
			public Adapter caseStagePosNXSPathType(StagePosNXSPathType object) {
				return createStagePosNXSPathTypeAdapter();
			}
			@Override
			public Adapter caseStageRotNXSPathType(StageRotNXSPathType object) {
				return createStageRotNXSPathTypeAdapter();
			}
			@Override
			public Adapter caseTifimageType(TifimageType object) {
				return createTifimageTypeAdapter();
			}
			@Override
			public Adapter caseTifNXSPathType(TifNXSPathType object) {
				return createTifNXSPathTypeAdapter();
			}
			@Override
			public Adapter caseTomodoType(TomodoType object) {
				return createTomodoTypeAdapter();
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
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.localtomo.BeamlineType <em>Beamline Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.localtomo.BeamlineType
	 * @generated
	 */
	public Adapter createBeamlineTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.localtomo.ClusterType <em>Cluster Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.localtomo.ClusterType
	 * @generated
	 */
	public Adapter createClusterTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.localtomo.DocumentRoot <em>Document Root</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.localtomo.DocumentRoot
	 * @generated
	 */
	public Adapter createDocumentRootAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.localtomo.FilenameFmtType <em>Filename Fmt Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.localtomo.FilenameFmtType
	 * @generated
	 */
	public Adapter createFilenameFmtTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.localtomo.ImagekeyencodingType <em>Imagekeyencoding Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.localtomo.ImagekeyencodingType
	 * @generated
	 */
	public Adapter createImagekeyencodingTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.localtomo.ImgkeyNXSPathType <em>Imgkey NXS Path Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.localtomo.ImgkeyNXSPathType
	 * @generated
	 */
	public Adapter createImgkeyNXSPathTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.localtomo.IxxType <em>Ixx Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.localtomo.IxxType
	 * @generated
	 */
	public Adapter createIxxTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.localtomo.LocalTomoType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.localtomo.LocalTomoType
	 * @generated
	 */
	public Adapter createLocalTomoTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.localtomo.NexusfileType <em>Nexusfile Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.localtomo.NexusfileType
	 * @generated
	 */
	public Adapter createNexusfileTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.localtomo.QsubType <em>Qsub Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.localtomo.QsubType
	 * @generated
	 */
	public Adapter createQsubTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.localtomo.SettingsfileType <em>Settingsfile Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.localtomo.SettingsfileType
	 * @generated
	 */
	public Adapter createSettingsfileTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.localtomo.ShutterClosedPhysType <em>Shutter Closed Phys Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.localtomo.ShutterClosedPhysType
	 * @generated
	 */
	public Adapter createShutterClosedPhysTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.localtomo.ShutterNXSPathType <em>Shutter NXS Path Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.localtomo.ShutterNXSPathType
	 * @generated
	 */
	public Adapter createShutterNXSPathTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.localtomo.ShutterOpenPhysType <em>Shutter Open Phys Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.localtomo.ShutterOpenPhysType
	 * @generated
	 */
	public Adapter createShutterOpenPhysTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.localtomo.ShutterType <em>Shutter Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.localtomo.ShutterType
	 * @generated
	 */
	public Adapter createShutterTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.localtomo.StagePosNXSPathType <em>Stage Pos NXS Path Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.localtomo.StagePosNXSPathType
	 * @generated
	 */
	public Adapter createStagePosNXSPathTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.localtomo.StageRotNXSPathType <em>Stage Rot NXS Path Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.localtomo.StageRotNXSPathType
	 * @generated
	 */
	public Adapter createStageRotNXSPathTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.localtomo.TifimageType <em>Tifimage Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.localtomo.TifimageType
	 * @generated
	 */
	public Adapter createTifimageTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.localtomo.TifNXSPathType <em>Tif NXS Path Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.localtomo.TifNXSPathType
	 * @generated
	 */
	public Adapter createTifNXSPathTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.localtomo.TomodoType <em>Tomodo Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.localtomo.TomodoType
	 * @generated
	 */
	public Adapter createTomodoTypeAdapter() {
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

} //LocalTomoAdapterFactory
