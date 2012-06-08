/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.excalibur.config.model.util;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

import uk.ac.gda.excalibur.config.model.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see uk.ac.gda.excalibur.config.model.ExcaliburConfigPackage
 * @generated
 */
public class ExcaliburConfigAdapterFactory extends AdapterFactoryImpl {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String copyright = "\nCopyright © 2011 Diamond Light Source Ltd.\n\nThis file is part of GDA.\n\nGDA is free software: you can redistribute it and/or modify it under the\nterms of the GNU General Public License version 3 as published by the Free\nSoftware Foundation.\n\nGDA is distributed in the hope that it will be useful, but WITHOUT ANY\nWARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS\nFOR A PARTICULAR PURPOSE. See the GNU General Public License for more\ndetails.\n\nYou should have received a copy of the GNU General Public License along\nwith GDA. If not, see <http://www.gnu.org/licenses/>.";
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static ExcaliburConfigPackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ExcaliburConfigAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = ExcaliburConfigPackage.eINSTANCE;
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
	protected ExcaliburConfigSwitch<Adapter> modelSwitch =
		new ExcaliburConfigSwitch<Adapter>() {
			@Override
			public Adapter caseAnperModel(AnperModel object) {
				return createAnperModelAdapter();
			}
			@Override
			public Adapter caseArrayCountsModel(ArrayCountsModel object) {
				return createArrayCountsModelAdapter();
			}
			@Override
			public Adapter caseBaseNode(BaseNode object) {
				return createBaseNodeAdapter();
			}
			@Override
			public Adapter caseExcaliburConfig(ExcaliburConfig object) {
				return createExcaliburConfigAdapter();
			}
			@Override
			public Adapter caseGapModel(GapModel object) {
				return createGapModelAdapter();
			}
			@Override
			public Adapter caseMasterConfigAdbaseModel(MasterConfigAdbaseModel object) {
				return createMasterConfigAdbaseModelAdapter();
			}
			@Override
			public Adapter caseMasterConfigNode(MasterConfigNode object) {
				return createMasterConfigNodeAdapter();
			}
			@Override
			public Adapter caseMasterModel(MasterModel object) {
				return createMasterModelAdapter();
			}
			@Override
			public Adapter caseMpxiiiChipRegModel(MpxiiiChipRegModel object) {
				return createMpxiiiChipRegModelAdapter();
			}
			@Override
			public Adapter caseMpxiiiGlobalRegModel(MpxiiiGlobalRegModel object) {
				return createMpxiiiGlobalRegModelAdapter();
			}
			@Override
			public Adapter casePixelModel(PixelModel object) {
				return createPixelModelAdapter();
			}
			@Override
			public Adapter caseReadoutNode(ReadoutNode object) {
				return createReadoutNodeAdapter();
			}
			@Override
			public Adapter caseReadoutNodeFemModel(ReadoutNodeFemModel object) {
				return createReadoutNodeFemModelAdapter();
			}
			@Override
			public Adapter caseSummaryAdbaseModel(SummaryAdbaseModel object) {
				return createSummaryAdbaseModelAdapter();
			}
			@Override
			public Adapter caseSummaryNode(SummaryNode object) {
				return createSummaryNodeAdapter();
			}
			@Override
			public Adapter caseFixModel(FixModel object) {
				return createFixModelAdapter();
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
	 * Creates a new adapter for an object of class '{@link uk.ac.gda.excalibur.config.model.AnperModel <em>Anper Model</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel
	 * @generated
	 */
	public Adapter createAnperModelAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.gda.excalibur.config.model.ArrayCountsModel <em>Array Counts Model</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.gda.excalibur.config.model.ArrayCountsModel
	 * @generated
	 */
	public Adapter createArrayCountsModelAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.gda.excalibur.config.model.BaseNode <em>Base Node</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.gda.excalibur.config.model.BaseNode
	 * @generated
	 */
	public Adapter createBaseNodeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.gda.excalibur.config.model.ExcaliburConfig <em>Excalibur Config</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.gda.excalibur.config.model.ExcaliburConfig
	 * @generated
	 */
	public Adapter createExcaliburConfigAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.gda.excalibur.config.model.GapModel <em>Gap Model</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.gda.excalibur.config.model.GapModel
	 * @generated
	 */
	public Adapter createGapModelAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.gda.excalibur.config.model.MasterConfigAdbaseModel <em>Master Config Adbase Model</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.gda.excalibur.config.model.MasterConfigAdbaseModel
	 * @generated
	 */
	public Adapter createMasterConfigAdbaseModelAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.gda.excalibur.config.model.MasterConfigNode <em>Master Config Node</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.gda.excalibur.config.model.MasterConfigNode
	 * @generated
	 */
	public Adapter createMasterConfigNodeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.gda.excalibur.config.model.MasterModel <em>Master Model</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.gda.excalibur.config.model.MasterModel
	 * @generated
	 */
	public Adapter createMasterModelAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel <em>Mpxiii Chip Reg Model</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel
	 * @generated
	 */
	public Adapter createMpxiiiChipRegModelAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel <em>Mpxiii Global Reg Model</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel
	 * @generated
	 */
	public Adapter createMpxiiiGlobalRegModelAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.gda.excalibur.config.model.PixelModel <em>Pixel Model</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.gda.excalibur.config.model.PixelModel
	 * @generated
	 */
	public Adapter createPixelModelAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.gda.excalibur.config.model.ReadoutNode <em>Readout Node</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.gda.excalibur.config.model.ReadoutNode
	 * @generated
	 */
	public Adapter createReadoutNodeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel <em>Readout Node Fem Model</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel
	 * @generated
	 */
	public Adapter createReadoutNodeFemModelAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.gda.excalibur.config.model.SummaryAdbaseModel <em>Summary Adbase Model</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.gda.excalibur.config.model.SummaryAdbaseModel
	 * @generated
	 */
	public Adapter createSummaryAdbaseModelAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.gda.excalibur.config.model.SummaryNode <em>Summary Node</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.gda.excalibur.config.model.SummaryNode
	 * @generated
	 */
	public Adapter createSummaryNodeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.gda.excalibur.config.model.FixModel <em>Fix Model</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.gda.excalibur.config.model.FixModel
	 * @generated
	 */
	public Adapter createFixModelAdapter() {
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

} //ExcaliburConfigAdapterFactory
