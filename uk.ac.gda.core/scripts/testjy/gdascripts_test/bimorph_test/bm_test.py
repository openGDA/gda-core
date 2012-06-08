import unittest
from gda.configuration.properties import LocalProperties
from gdascripts.bimorph import bimorph
from gdascripts.bimorph import ellipse
import inspect

class TestBimorph(unittest.TestCase):
	
	def setUp(self):
		classLocation = inspect.getfile( inspect.currentframe() )
		classLocation = classLocation[:classLocation.rfind("/")] + "/test files"
		LocalProperties.set("gda.data.scan.datawriter.datadir", classLocation);

	def jama_array_to_list(self, array):
		
		newList = []
		for element in array:
			frontBracket = str(element).index('[')
			endBracket = str(element).index(']')
			newV = str(element)[frontBracket+1:endBracket]
			newList.append(float(newV))
		
		return newList

	def test_vfm_nt1949_1_a_no_fixed_offset(self):
		LocalProperties.set("gda.data.scan.datawriter.dataFormat", None);
		bimorphScannable = None
		mirror_type = "vfm"
		numberOfElectrodes = 8
		voltageIncrement = 50
		files=[7321, 7322, 7323, 7324, 7325, 7326, 7327, 7328, 7329]
		error_file = files[0]
		desiredFocSize = 0
		user_offset = "n"
		beamOffset = 0
		bm_voltages=[1325, 1325, 1050, 1160, 1075, 885, 685, 750]
		ro = bimorph.runOptimisation(bimorphScannable, mirror_type,numberOfElectrodes,voltageIncrement,files,error_file,desiredFocSize,user_offset,bm_voltages, beamOffset)
		voltages_array = ro.voltages
		voltages=self.jama_array_to_list(voltages_array)
		expected_voltages = [21.707917175781574, 68.0918192414288, 76.01760025878745, 46.145353978649865, 67.62697538776101, 52.51030110033116, 46.51513486372197, 579.8717882201148, 15.744209710729299]
		self.assertEqual(voltages, expected_voltages)

	def test_vfm_nt1949_1_b_with_fixed_offset(self):
		LocalProperties.set("gda.data.scan.datawriter.dataFormat", None);
		bimorphScannable = None
		mirror_type = "vfm"
		numberOfElectrodes = 8
		voltageIncrement = 50
		files=[6246, 6247, 6248, 6249, 6250, 6251, 6252, 6253, 6254]
		error_file = files[0]
		desiredFocSize = 0
		user_offset = "y"
		beamOffset = -5
		bm_voltages=[1083, 1137, 1020, 989, 816, 776, 1142, 878]
		ro = bimorph.runOptimisation(bimorphScannable, mirror_type,numberOfElectrodes,voltageIncrement,files,error_file,desiredFocSize,user_offset,bm_voltages, beamOffset)
		voltages_array = ro.voltages
		voltages=self.jama_array_to_list(voltages_array)
		expected_voltages = [-14.352611346065475, 31.987402263072497, 21.48022308487782, -40.989431094166704, -41.33015530208741, -10.890009414552086, 54.54739724739742, 20.75377740455611]
		self.assertEqual(voltages, expected_voltages)

	def test_hfm_nt1949_1_c_check_correct_column_read(self):
		LocalProperties.set("gda.data.scan.datawriter.dataFormat", None);
		bimorphScannable = None
		mirror_type = "hfm"
		numberOfElectrodes = 14
		voltageIncrement = 50
		files=[7303,7304,7305,7306,7307,7308,7309,7310,7311,7312,7313,7314,7315,7316,7317]
		error_file = files[0]
		desiredFocSize = 0
		user_offset = "y"
		beamOffset = -1.414895769
		bm_voltages=[229,227,175,223,141,69,427,315,313,171,123,127,205,202]
		ro = bimorph.runOptimisation(bimorphScannable, mirror_type,numberOfElectrodes,voltageIncrement,files,error_file,desiredFocSize,user_offset,bm_voltages, beamOffset)
		voltages_array = ro.voltages
		voltages=self.jama_array_to_list(voltages_array)
		expected_voltages = [100.00589474706288, 6.896231396943069, -29.706214390105597 , -31.13889080038476, -55.70335904877516, 33.45884555122911, -45.864391108988215, 31.887245899450804, -31.94216464119976, -33.70011375417677, -31.746432810030754, 75.20662031160917, -2.3970642349464555, 35.88915448467056]
		self.assertEqual(voltages, expected_voltages)

	def test_vfm_cm1843_9_a_with_different_fixed_offsets_a(self):
		LocalProperties.set("gda.data.scan.datawriter.dataFormat", None);
		bimorphScannable = None
		mirror_type = "vfm"
		numberOfElectrodes = 8
		voltageIncrement = 50
		files=[6246, 6247, 6248, 6249, 6250, 6251, 6252, 6253, 6254]
		error_file = files[0]
		desiredFocSize = 0
		user_offset = "y"
		beamOffset = 0
		bm_voltages=[1083, 1137, 1020, 989, 816, 776, 1142, 878]
		ro = bimorph.runOptimisation(bimorphScannable, mirror_type,numberOfElectrodes,voltageIncrement,files,error_file,desiredFocSize,user_offset,bm_voltages, beamOffset)
		voltages_array = ro.voltages
		voltages=self.jama_array_to_list(voltages_array)
		expected_voltages = [-8.883061970213086, -1.261110506131546, -16.85721282341746, -71.50859584889629, -38.519920608649066, -6.748858597933538, 46.83169889148378, 27.71528585683874]
		self.assertEqual(voltages, expected_voltages)
		
	def test_vfm_cm1843_9_b_with_different_fixed_offsets_b(self):
		LocalProperties.set("gda.data.scan.datawriter.dataFormat", None);
		bimorphScannable = None
		mirror_type = "vfm"
		numberOfElectrodes = 8
		voltageIncrement = 50
		files=[6246, 6247, 6248, 6249, 6250, 6251, 6252, 6253, 6254]
		error_file = files[0]
		desiredFocSize = 0
		user_offset = "y"
		beamOffset = -5
		bm_voltages=[1083, 1137, 1020, 989, 816, 776, 1142, 878]
		ro = bimorph.runOptimisation(bimorphScannable, mirror_type,numberOfElectrodes,voltageIncrement,files,error_file,desiredFocSize,user_offset,bm_voltages, beamOffset)
		voltages_array = ro.voltages
		voltages=self.jama_array_to_list(voltages_array)
		expected_voltages = [-14.352611346065475, 31.987402263072497, 21.48022308487782, -40.989431094166704, -41.33015530208741, -10.890009414552086, 54.54739724739742, 20.75377740455611]
		self.assertEqual(voltages, expected_voltages)
		
	def test_hfm_cm1902_1_a_non_zero_beamsize(self):
		LocalProperties.set("gda.data.scan.datawriter.dataFormat", None);
		bimorphScannable = None
		mirror_type = "hfm"
		numberOfElectrodes = 7
		voltageIncrement = 50
		files=[494, 495, 496, 497, 498, 499, 500, 501]
		error_file = 522
		desiredFocSize = 120
		user_offset = "n"
		beamOffset = -5
		bm_voltages=[164, 341, 518, 891, 638, 250, -200]
		ro = bimorph.runOptimisation(bimorphScannable, mirror_type,numberOfElectrodes,voltageIncrement,files,error_file,desiredFocSize,user_offset,bm_voltages, beamOffset)
		voltages_array = ro.voltages
		voltages=self.jama_array_to_list(voltages_array)
		expected_voltages = [-9.381482992943564, -41.114537012601346, -80.83318874605551, -74.54479274150891, -156.47014639747533, -99.74506922894444, -73.05411599733563, -29.008854974660963]
		self.assertEqual(voltages, expected_voltages)
		
	def test_hfm_cm1902_1_a_non_zero_beamsize_with_offset(self):
		LocalProperties.set("gda.data.scan.datawriter.dataFormat", None);
		bimorphScannable = None
		mirror_type = "hfm"
		numberOfElectrodes = 7
		voltageIncrement = 50
		files=[494, 495, 496, 497, 498, 499, 500, 501]
		error_file = 522
		desiredFocSize = 120
		user_offset = "y"
		beamOffset = 9.381482993
		bm_voltages=[164, 341, 518, 891, 638, 250, -200]
		ro = bimorph.runOptimisation(bimorphScannable, mirror_type,numberOfElectrodes,voltageIncrement,files,error_file,desiredFocSize,user_offset,bm_voltages, beamOffset)
		voltages_array = ro.voltages
		voltages=self.jama_array_to_list(voltages_array)
		expected_voltages = [-41.11453701262047, -80.83318874606086, -74.54479274145046, -156.47014639756856, -99.74506922893444, -73.05411599732427, -29.0088549746502]
		self.assertEqual(voltages, expected_voltages)
		
	def test_ellipse_calculation(self):
		
		LocalProperties.set("gda.data.scan.datawriter.dataFormat", None);
		el = ellipse.EllipseCalculator(6.5,46.5,0.4,3,46.5,0.27,2,-1.0,0.4,-0.15,0.12,0.01,"peak2d_peaky")	
		el.calcSlopesNoFile()
		
		beamPositions = []
		for pos in el.getBeamPositions():
			beamPositions.append(round(pos, 2))

		self.assertEquals(beamPositions, [-0.15,-0.14,-0.13,-0.12,-0.11,-0.1,-0.09,-0.08,-0.07,-0.06,-0.05,-0.04,-0.03,-0.02,-0.01,0.0,0.01,0.02,0.03,0.04,0.05,0.06,0.07,0.08,0.09,0.1,0.11,0.12])
		
		errors = []
		for e in el.getErrors():
			errors.append(round(e, 6))
	
		self.assertEquals(errors, [0.966437,0.858933,0.756076,0.658116,0.565314,0.477945,0.396298,0.320679,0.251409,0.188826,0.133288,0.085170,0.044869,0.012806,-0.010578,-0.024815,-0.029410,-0.023840,-0.007553,0.020037,0.059551,0.111646,0.177018,0.256407,0.350598,0.460425,0.586773,0.730586])
	
	def disabled_test_vfm_nt1949_1_a_no_fixed_offset_nexus(self):
		""" Test disabled, since it requires LD_LIBARY_PATH to be set, and the batch test harness cannot do that (established after numerous experiments)
			It can still be run from within Eclipse provided LD_LIBARY_PATH is set correctly (you need to remove the "disabled_" prefix from the method name)
		"""
		LocalProperties.set("gda.data.scan.datawriter.dataFormat", "NexusDataWriter");
		bimorphScannable = None
		mirror_type = "vfm"
		numberOfElectrodes = 12
		voltageIncrement = 50
		files=[22607,22608,22609,22610,22611,22612,22613,22614,22615,22616,22617,22618,22619]
		error_file = 22619
		desiredFocSize = 0
		user_offset = "n"
		beamOffset = 0
		bm_voltages=[15,15,15,15,15,15,15,15,15,15,15,15]
		ro = bimorph.runOptimisation(bimorphScannable, mirror_type,numberOfElectrodes,voltageIncrement,files,error_file,desiredFocSize,user_offset,bm_voltages, beamOffset)
		
	def test_vfm_no_min_max(self):
		print "test_vfm_no_min_max"
		LocalProperties.set("gda.data.scan.datawriter.dataFormat", None);
		bimorphScannable = None
		mirror_type = "vfm"
		numberOfElectrodes = 16
		voltageIncrement = 200
		files=[1703,1704,1705,1706,1707,1708,1709,1710,1711,1712,1713,1714,1715,1716,1717,1718,1719]
		error_file = files[0]
		desiredFocSize = 0
		user_offset = "n"
		beamOffset = 0
		bm_voltages=[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
		ro = bimorph.runOptimisation(bimorphScannable, mirror_type,numberOfElectrodes,voltageIncrement,files,error_file,desiredFocSize,user_offset,bm_voltages, beamOffset)
		voltages_array = ro.voltages
		voltages=self.jama_array_to_list(voltages_array)
		expected_voltages = [2.908266803226027, 738.7636771196698, -33.86422141589075, -93.46613140770464, -294.47223693353016, 372.7810464757556, 271.82186538663143, -641.5647933494206, 326.8124925999065, 519.8722901081794, -660.6127137075771, -99.58268118422892, 322.95408301131374, 152.15752332986295, -310.2560956752073, 379.20672013112346, 1049.276902112866]
		self.assertEqual(voltages, expected_voltages)
		
	def test_vfm_min_max(self):
		print "test_vfm_min_max"
		LocalProperties.set("gda.data.scan.datawriter.dataFormat", None);
		bimorphScannable = None
		mirror_type = "vfm"
		numberOfElectrodes = 16
		voltageIncrement = 200
		files=[1703,1704,1705,1706,1707,1708,1709,1710,1711,1712,1713,1714,1715,1716,1717,1718,1719]
		error_file = files[0]
		desiredFocSize = 0
		user_offset = "n"
		beamOffset = 0
		bm_voltages=[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
		ro = bimorph.runOptimisation(bimorphScannable, mirror_type,numberOfElectrodes,voltageIncrement,files,error_file,desiredFocSize,user_offset,bm_voltages, beamOffset, minSlitPos=-0.5, maxSlitPos=1.09)
		voltages_array = ro.voltages
		voltages=self.jama_array_to_list(voltages_array)

def suite():
	return unittest.TestLoader().loadTestsFromTestCase(TestBimorph)

if __name__ == '__main__':
	unittest.main()