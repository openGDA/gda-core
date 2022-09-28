from itertools import cycle, count
import unittest
from mock import MagicMock, Mock, call, patch
from gda.configuration.properties import LocalProperties
from bimorphtest import roughly
import bimorph
import ellipse
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
        
#     def test_ellipse_method0(self):
#         LocalProperties.set("gda.data.scan.datawriter.dataFormat", None);
#         pixel_size = 6.5
#         p_1 = 46.5
#         q_1 = 0.4
#         theta_1 = 3
#         p_2 = 46.5
#         q_2 = 0.27
#         theta_2 = 2
#         i_sign = -1
#         detector_distance = 0.4
#         slit_start = -0.15
#         slit_end = 0.12
#         slit_step = 0.01
#         column = "peak2d_peaky"
#         inv = 1
#         method = 0
#         el = ellipse.EllipseCalculator(pixel_size, p_1, q_1, theta_1, p_2, q_2, theta_2, i_sign, detector_distance, slit_start, slit_end, slit_step, column, inv, method)    
#         el.calcSlopes()
#         beamPositions = []
#         for pos in el.getBeamPositions():
#             beamPositions.append(round(pos, 2))
#         self.assertEquals(beamPositions, [-0.15,-0.14,-0.13,-0.12,-0.11,-0.1,-0.09,-0.08,-0.07,-0.06,-0.05,-0.04,-0.03,-0.02,-0.01,0.0,0.01,0.02,0.03,0.04,0.05,0.06,0.07,0.08,0.09,0.1,0.11,0.12])
#         errors = []
#         for e in el.getErrors():
#             errors.append(round(e, 6))
#         self.assertEquals(errors, [0.966437,0.858933,0.756076,0.658116,0.565314,0.477945,0.396298,0.320679,0.251409,0.188826,0.133288,0.085170,0.044869,0.012806,-0.010578,-0.024815,-0.029410,-0.023840,-0.007553,0.020037,0.059551,0.111646,0.177018,0.256407,0.350598,0.460425,0.586773,0.730586])

#     def test_ellipse_method1(self):
#         LocalProperties.set("gda.data.scan.datawriter.dataFormat", None);
#         pixel_size = 4.65
#         p_1 = 45.1
#         q_1 = 0.4
#         theta_1 = 3
#         p_2 = 45.1
#         q_2 = 0.25
#         theta_2 = 2
#         i_sign = 1.0
#         detector_distance = 0.4
#         slit_start = 3.0
#         slit_end = 3.9
#         slit_step = 0.01
#         column = "peak2d_peaky"
#         inv = 1
#         method = 1
#         el = ellipse.EllipseCalculator(pixel_size, p_1, q_1, theta_1, p_2, q_2, theta_2, i_sign, detector_distance, slit_start, slit_end, slit_step, column, inv, method)    
#         el.calcCamPos()
#         beamPositions = []
#         for pos in el.getBeamPositions():
#             beamPositions.append(round(pos, 2))
#         vals=[round(x * 0.01, 2) for x in range(300, 391)]
#         self.assertEquals(vals, beamPositions)
#         errors = []
#         for e in el.getErrors():
#             errors.append(round(e, 8))
#             vals=[-97.13370606    ,-100.44586078    ,-103.60163457    ,-106.61296492    ,-109.49055120    ,-112.24401507    ,-114.88203607    ,-117.41246708    ,-119.84243284    ,-122.17841453    ,-124.42632260    ,-126.59155986    ,-128.67907616    ,-130.69341610    ,-132.63876068    ,-134.51896390    ,-136.33758492    ,-138.09791645    ,-139.80300981    ,-141.45569729    ,-143.05861192    ,-144.61420513    ,-146.12476256    ,-147.59241822    ,-149.01916715    ,-150.40687685    ,-151.75729760    ,-153.07207174    ,-154.35274206    ,-155.60075945    ,-156.81748984    ,-158.00422043    ,-159.16216551    ,-160.29247160    ,-161.39622231    ,-162.47444268    ,-163.52810317    ,-164.55812338    ,-165.56537539    ,-166.55068690    ,-167.51484407    ,-168.45859419    ,-169.38264808    ,-170.28768238    ,-171.17434162    ,-172.04324014    ,-172.89496392    ,-173.73007221    ,-174.54909909    ,-175.35255490    ,-176.14092758    ,-176.91468396    ,-177.67427084    ,-178.42011615    ,-179.15262993    ,-179.87220530    ,-180.57921932    ,-181.27403385    ,-181.95699633    ,-182.62844047    ,-183.28868699    ,-183.93804424    ,-184.57680881    ,-185.20526609    ,-185.82369083    ,-186.43234763    ,-187.03149141    ,-187.62136788    ,-188.20221395    ,-188.77425813    ,-189.33772093    ,-189.89281517    ,-190.43974639    ,-190.97871310    ,-191.50990712    ,-192.03351387    ,-192.54971264    ,-193.05867683    ,-193.56057420    ,-194.05556714    ,-194.54381283    ,-195.02546352    ,-195.50066666    ,-195.96956514    ,-196.43229746    ,-196.88899787    ,-197.33979658    ,-197.78481992    ,-198.22419041    ,-198.65802702    ,-199.08644520]    
#         self.assertEquals(errors, vals)
        
#     def test_ellipse_method2(self):
#         LocalProperties.set("gda.data.scan.datawriter.dataFormat", None);
#         i_sign = -1
#         inv = +1
#         p_1 = 215.0
#         q_1 = 0.403
#         theta_1 = 2.91
#         p_2 = 215.0
#         q_2 = 0.270
#         theta_2 = 2.40
#         detector_distance = 0.403
#         pixel_size = 0.19
#         slit_start=-0.18
#         slit_end=0.15
#         slit_step=0.01
#         column = "peak2d_peaky"
#         method = 1
#         el = ellipse.EllipseCalculator(pixel_size, p_1, q_1, theta_1, p_2, q_2, theta_2, i_sign, detector_distance, slit_start, slit_end, slit_step, column, inv, method)    
#         error = el.calcCamPos2()
#         vals=[-152.95537584088277, -146.0067144566203, -138.79892000444625, -131.321053601543, -123.57008690659926, -115.53703219966715, -107.21323801069101, -98.58946668603264, -89.65963325954903, -80.41555979624799, -70.84411798829083, -60.93899238131607, -50.68674169402116, -40.07888499076346, -29.1094410205496, -17.764287806377116, -6.008645856356755, 6.131089050918185, 18.678721615770744, 31.665749537210882, 45.089467512247644, 58.97606647492202, 73.33186095629578, 88.1814554391088, 103.53571610603831, 119.41707143257197, 135.84112959299773, 152.82755061183082, 170.3979097901861, 188.57536950936245, 207.382618204007, 226.84377404531574, 246.9849629193695, 267.8331781177888]
#         self.assertEquals(error, vals)
        
    def test_vfm_no_min_max(self):
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

class TestBimorphOptimisation(unittest.TestCase):
    def setUp(self):
        self.mirror = Mock()
        self.mirror.maxSafeVoltage = 20
        self.mirror.minSafeVoltage = 10
        self.mirror.maxSafeVoltDiff = 4
        self.voltageController = Mock()
        self._voltages = [10, 13, 16, 19]
        self.voltageController.getVoltage.side_effect = lambda x: self._voltages[x]
        self.voltageController.setVoltage.side_effect = lambda x, v: self._voltages.__setitem__(x, v)
        self.mirror.numberOfElectrodes = len(self._voltages)
        self.centroidReader = Mock()
        self.centroidReader.getValue.side_effect = cycle((1,2,3,4))
        self.slitMotor = Mock()
        self.bo = bimorph.BimorphOptimiser(False, # verbose
                                      self.mirror,
                                      self.voltageController,
                                      self.centroidReader,
                                      [7,8,9,10], # slitPos
                                      self.slitMotor,
                                      4.0, # beamOffset
                                      True, # auto offset
                                      True, # auto dist
                                      1.2) # scaling factor

    def test_get_voltage(self):
        electrode = 2
        self.bo.getVoltage(electrode)
        self.voltageController.getVoltage.assert_called_once_with(electrode)

    def test_set_voltage(self):
        self.bo.setVoltage(0, 12)
        self.voltageController.setVoltage.assert_called_once_with(0, 12)

    def test_set_voltage_out_of_range(self):
        with self.assertRaises(ValueError):
            self.bo.setVoltage(0, 23)
        self.voltageController.setVoltage.assert_not_called()
        with self.assertRaises(ValueError):
            self.bo.setVoltage(0, 8)
        self.voltageController.setVoltage.assert_not_called()

    def test_set_voltage_large_diff(self):
        with self.assertRaises(ValueError):
            self.bo.setVoltage(1, 11)
        self.voltageController.setVoltage.assert_not_called()
        with self.assertRaises(ValueError):
            self.bo.setVoltage(1, 15)
        self.voltageController.setVoltage.assert_not_called()

    @unittest.expectedFailure # setVoltage is currently broken for first/last voltages
    def test_set_first_last_voltage(self):
        self.bo.setVoltage(0, 10.2)
        self.voltageController.setVoltage.assert_called_once_with(0, roughly(10.2))
        self.bo.setVoltage(3, 18.2)
        self.voltageController.setVoltage.assert_called_once_with(0, roughly(18.2))

    def test_increment_voltage(self):
        self.bo.incrementVoltage(1, 0.3)
        self.voltageController.setVoltage.assert_called_once_with(1, roughly(13.3))

    def test_increment_voltage_large_diff(self):
        with self.assertRaises(ValueError):
            self.bo.incrementVoltage(1, 1.4)
        self.voltageController.setVoltage.assert_not_called()

    def test_increment_voltage_outside_limit(self):
        with self.assertRaises(ValueError):
            self.bo.incrementVoltage(0, -0.5)
        self.voltageController.setVoltage.assert_not_called()

    def test_desired_centroids(self):
        """I have no idea what this method does"""
        desired_centroids = self.bo.getDesiredCentroids([1,2,3,4], [0.1, 0.2, 0.3, 0.4], 0.5)
        self.assertEqual(roughly([6.58333333, 6.75, 6.91666666, 7.08333333]), desired_centroids)

    def test_get_centroids(self):
        centroids = self.bo.getCentroids()
        self.slitMotor.assert_has_calls([call(7), call(8), call(9), call(10)])
        self.assertEqual([1,2,3,4], centroids)

    @unittest.expectedFailure # setVoltage is currently broken for first/last voltages
    def test_force_voltages(self):
        self.bo.forceVoltages(16.8)
        self.voltageController.setVoltage.assert_has_calls([
            call(0, 16.8),
            call(1, 16.8),
            call(2, 16.8),
            call(3, 16.8)])

    @unittest.expectedFailure # setVoltage is currently broken for first/last voltages
    def test_interaction_matrix_with_auto_offset(self):
        self.bo.auto_offset = True
        matrix = self.bo.buildInteractionMatrix(self._voltages, 0.3)
        matrix = [list(i) for i in matrix] # convert from jama Matrix

    @unittest.expectedFailure # setVoltage is currently broken for first/last voltages
    def test_interaction_matrix_without_auto_offset(self):
        self.bo.auto_offset = False
        self.bo.buildInteractionMatrix(self._voltages, 0.3)

    @unittest.expectedFailure# setVoltage is currently broken for first/last voltages
    def test_run(self):
        self.bo.run([0.1, 0.2, 0.3, 0.4], # weights
                    0.5, # desired focal size
                    self._voltages, # initial voltages
                    0.1, # increment
                    [1,2,3,4], # centroids
                    True, # auto dist
                    0.1 # scaling factor
                    )

class TestRunOptimisation(unittest.TestCase):
    def setUp(self):
        self.bimorph_scannable = Mock()
        self.bm_voltages = [10,13,16,19]
        self.ro = bimorph.RunOptimisation(bimorphScannable=self.bimorph_scannable,
                  mirror_type='vfm',
                  numberOfElectrodes=4,
                  voltageIncrement=0.1,
                  files=[1234,1235,1236,1237,1238],
                  error_file=1233,
                  desiredFocSize=0.5,
                  user_offset=1.4,
                  bm_voltages=self.bm_voltages, 
                  beamOffset=1.2,
                  autoDist=True,
                  scalingFactor=1.5,
                  scanDir='/tmp/datadir', 
                  minSlitPos=4.8, 
                  maxSlitPos=12.3,
                  slitPosScannableName='s1')

    def test_call(self):
        ro = Mock(spec=bimorph.RunOptimisation)
        bimorph.RunOptimisation.__call__(ro)
        ro.requestInputs.assert_called_once_with()
        ro.calculateVoltages.assert_called_once_with()

    @patch('bimorph.GDAMetadataProvider')
    @patch('bimorph.ScanFileLoader')
    @patch('bimorph.NumTracker')
    def test_get_error_data_without_error_file(self, num_tracker, sfl, gdametaprovider):
        self.ro.error_file = 0
        num_tracker.return_value.getCurrentFileNumber.return_value = 1234
        error_data = self.ro.getErrorData()
        num_tracker.assert_called_once_with(gdametaprovider.getInstance().getMetadataValue())
        sfl.assert_called_once_with(1234, '/tmp/datadir')
        sfl().getSFH.assert_called_once_with()
        self.assertEquals(sfl().getSFH(), error_data)

    @patch('bimorph.ScanFileLoader')
    def test_get_error_data(self, sfl):
        error_data = self.ro.getErrorData()
        sfl.assert_called_once_with(1233, '/tmp/datadir')
        sfl().getSFH.assert_called_once_with()
        self.assertEqual(error_data, sfl().getSFH())
        
    @patch('bimorph.InterfaceProvider')
    @patch('bimorph.ScanFileLoader')
    def test_get_error_data_without_data_directory(self, sfl, ip):
        self.ro.scanDir = None
        file_loader = Mock()
        def mock_file_loader(file_number, path):
            if path is None: raise ValueError('Null path')
            else: return file_loader
        sfl.side_effect = mock_file_loader
        error_data = self.ro.getErrorData()

        sfl.assert_has_calls([call(1233, None), call(1233, ip.getPathConstructor().getDefaultDataDir())])
        file_loader.getSFH.assert_called_once_with()
        self.assertEqual(file_loader.getSFH(), error_data)
        
    def test_get_column_names_peak2d(self):
        ro = Mock(spec=bimorph.RunOptimisation)
        ro.getColumnSuffix.return_value = 'testsuffix'
        
        names = bimorph.RunOptimisation.getColumnNames(ro, ['peak2d.testsuffix'])
        
        ro.nexusColumnNames.assert_called_once_with('peak2d.', 'testsuffix')
        ro.nonNexusColumnNames.assert_not_called()
        self.assertEquals(names, ro.nexusColumnNames.return_value)
    
    def test_get_column_names_pa(self):
        ro = Mock(spec=bimorph.RunOptimisation)
        ro.getColumnSuffix.return_value = 'testsuffix'
        
        names = bimorph.RunOptimisation.getColumnNames(ro, ['pa.testsuffix'])
        
        ro.nexusColumnNames.assert_called_once_with('pa.', 'testsuffix')
        ro.nonNexusColumnNames.assert_not_called()
        self.assertEquals(names, ro.nexusColumnNames.return_value)

    def test_get_column_names_non_nexus(self):
        ro = Mock(spec=bimorph.RunOptimisation)
        ro.getColumnSuffix.return_value = 'testsuffix'
        
        names = bimorph.RunOptimisation.getColumnNames(ro, ['testsuffix'])
        
        ro.nonNexusColumnNames.assert_called_once_with('testsuffix')
        ro.nexusColumnNames.assert_not_called()
        self.assertEquals(names, ro.nonNexusColumnNames.return_value)

    def test_nexus_column_names(self):
        names = self.ro.nexusColumnNames('prefix', 'suffix')
        self.assertEquals({'data_centroid': 'prefixsuffix',
                           'err_centroid': 'prefixsuffix',
                           'err_slit': 's1.s1'}, names)

    def test_non_nexus_column_names(self):
        names = self.ro.nonNexusColumnNames('suffix')
        self.assertEquals({'data_centroid': 'suffix',
                           'err_centroid': 'suffix',
                           'err_slit': '/entry1/instrument/pa/idx'}, names)

    def test_get_column_suffix(self):
        self.ro.mirror_type = 'vfm'
        self.assertEquals('peak2d_peaky', self.ro.getColumnSuffix())

        self.ro.mirror_type = 'hfm'
        self.assertEquals('peak2d_peakx', self.ro.getColumnSuffix())

        self.ro.mirror_type = 'xyz'
        with self.assertRaises(ValueError):
            self.ro.getColumnSuffix()
            
    def test_get_slit_position_with_err_slit(self):
        mock_data = Mock()
        names = {'data_centroid': 'prefixsuffix',
                'err_centroid': 'prefixsuffix',
                'err_slit': 's1.s1'}
        slit_position = self.ro.getSlitPos(names, ['s1.s1'], mock_data)
        mock_data.getLazyDataset.assert_called_once_with('s1.s1')
        mock_data.getLazyDataset().getSlice.assert_called_once_with(None)
        mock_data.getLazyDataset().getSlice().getBuffer.assert_called_once_with()
        self.assertEquals(mock_data.getLazyDataset().getSlice().getBuffer(), slit_position)

    def test_get_slit_position_with_idx(self):
        mock_data = Mock()
        names = {'data_centroid': 'prefixsuffix',
                'err_centroid': 'prefixsuffix',
                'err_slit': '/entry/instrument/pa'}
        slit_position = self.ro.getSlitPos(names, ['idx'], mock_data)
        mock_data.getLazyDataset.assert_called_once_with(1)
        mock_data.getLazyDataset().getSlice.assert_called_once_with(None)
        mock_data.getLazyDataset().getSlice().getBuffer.assert_called_once_with()
        self.assertEquals(mock_data.getLazyDataset().getSlice().getBuffer(), slit_position)

    def test_get_slit_position_without_idx(self):
        mock_data = Mock()
        names = {'data_centroid': 'prefixsuffix',
                'err_centroid': 'prefixsuffix',
                'err_slit': '/entry/instrument/pa'}
        slit_position = self.ro.getSlitPos(names, ['not_idx'], mock_data)
        mock_data.getLazyDataset.assert_called_once_with(0)
        mock_data.getLazyDataset().getSlice.assert_called_once_with(None)
        mock_data.getLazyDataset().getSlice().getBuffer.assert_called_once_with()
        self.assertEquals(mock_data.getLazyDataset().getSlice().getBuffer(), slit_position)

    def test_limit_indices_with_two_limits(self):
        self.ro.minSlitPos = 1.8
        self.ro.maxSlitPos = 2.7
        start, end = self.ro.getLimitIndices([0.5, 1, 1.5, 2, 2.5, 3, 3.5, 4])
        self.assertEquals(3, start)
        self.assertEquals(5, end)
    
    @unittest.expectedFailure # lower limits only are broken
    def test_limit_indices_with_lower_limit(self):
        self.ro.minSlitPos = 1.8
        self.ro.maxSlitPos = None
        start, end = self.ro.getLimitIndices([0.5, 1, 1.5, 2, 2.5, 3, 3.5, 4])
        self.assertEquals(3, start)
        self.assertEquals(7, end)
    
    def test_limit_indices_with_upper_limit(self):
        self.ro.minSlitPos = None
        self.ro.maxSlitPos = 2.7
        start, end = self.ro.getLimitIndices([0.5, 1, 1.5, 2, 2.5, 3, 3.5, 4])
        self.assertEquals(0, start)
        self.assertEquals(5, end)
    
    @unittest.expectedFailure # 'works' but does the wrong thing
    def test_limit_indices_with_no_limits(self):
        self.ro.minSlitPos = None
        self.ro.maxSlitPos = None
        indices = self.ro.getLimitIndices([0.5, 1, 1.5, 2, 2.5, 3, 3.5, 4])
        self.assertIs(None, indices)
        self.fail('This should just return (0, len(slitPos))')
        
    def test_has_slit_pos_limit(self):
        self.ro.minSlitPos = 1
        self.ro.minSlitPos = 3
        self.assertTrue(self.ro.hasSlitPosLimit())
        
        self.ro.minSlitPos = 1
        self.ro.maxSlitPos = None
        self.assertTrue(self.ro.hasSlitPosLimit())
        
        self.ro.minSlitPos = None
        self.ro.maxSlitPos = 3
        self.assertTrue(self.ro.hasSlitPosLimit())
        
        self.ro.minSlitPos = None
        self.ro.maxSlitPos = None
        self.assertFalse(self.ro.hasSlitPosLimit())
    
    @patch('bimorph.ScanFileLoader')
    def test_centroid_matrix(self, sfl):
        column_names = {'data_centroid': 'prefixsuffix',
                'err_centroid': 'prefixsuffix',
                'err_slit': 's1.s1'}
        limit = (1, 4)
        _mock_data_generator = count() # number generator
        def create_mock_data_file():
            df = MagicMock()
            sfh = df.getSFH.return_value
            sfh.getNames.return_value = ['peak2d.peak2d_peaky']
            data_buffer = sfh.getLazyDataset.return_value.getSlice.return_value.getBuffer
            data_buffer.return_value = [next(_mock_data_generator) for _ in range(5)]
            return df
        
        mock_file_data = {1234: create_mock_data_file(),
                          1235: create_mock_data_file(),
                          1236: create_mock_data_file(),
                          1237: create_mock_data_file(),
                          1238: create_mock_data_file()}
        def _load_file(f_number, scan_dir):
            return mock_file_data[f_number]
        sfl.side_effect = _load_file
        
        error_data = create_mock_data_file().getSFH()
        
        centroid = self.ro.getCentroidMatrix(error_data, column_names, limit)
        
        self.assertEquals(([[1, 2, 3], [6, 7, 8], [11, 12, 13], [16, 17, 18], [21, 22, 23]], [26, 27, 28]), centroid)
        for df in mock_file_data.values():
            df.getSFH.assert_called_once_with()
            sfh = df.getSFH()
            # called too many times
            # sfh.getNames.assert_called_once_with()
            sfh.getLazyDataset.assert_called_once_with('peak2d.peak2d_peaky')
            ds = sfh.getLazyDataset()
            ds.getSlice.assert_called_once_with(None)
            ds.getSlice().getBuffer.assert_called_once_with()


class TestBimorphMirror(unittest.TestCase):
    def setUp(self):
        self.bm = bimorph.BimorphMirror(4, # number of electrodes
                                        20, # max voltage
                                        10, # min voltage
                                        5) # max voltage diff

    def test_get_slit_position(self):
        positions = self.bm.genSlitPos(3.0, 10.0, 2) # initial pos, final pos, points per electrode
        self.assertEquals(roughly([3, 4, 5, 6, 7, 8, 9, 10]), positions)
        positions = self.bm.genSlitPos(1.0, 9.0, 2)
        self.assertEquals(roughly([1, 2.14285714, 3.28571429, 4.42857142, 5.57142857, 6.714285714, 7.85714286, 9.0]), positions)

class TestBimorphFunctions(unittest.TestCase):
    @patch('bimorph.RunOptimisation')
    def test_run_optimisation(self, run_opt):
        bimorph_mirror = Mock()
        mirror_type = 'vfm'
        number_of_electrodes = 4
        voltage_increment = 0.1
        files = ['a', 'b', 'c', 'd']
        error_file = 'error_file'
        desired_foc_size = 0.5
        user_offset = 0.8
        bm_voltages = [1,2,3,4]
        beam_offset = 1.2
        auto_dist = True
        scaling_factor = 1.5
        scan_dir = '/scan/directory'
        min_slit_pos = 10
        max_slit_pos = 20
        slit_pos_scannable_name = 's1'
        optimisation = bimorph.runOptimisation(bimorph_mirror,
                                               mirror_type,
                                               number_of_electrodes,
                                               voltage_increment,
                                               files,
                                               error_file,
                                               desired_foc_size,
                                               user_offset,
                                               bm_voltages,
                                               beam_offset,
                                               auto_dist,
                                               scaling_factor,
                                               scan_dir,
                                               min_slit_pos,
                                               max_slit_pos,
                                               slit_pos_scannable_name)
        run_opt.assert_called_once_with(bimorph_mirror,
                                               mirror_type,
                                               number_of_electrodes,
                                               voltage_increment,
                                               files,
                                               error_file,
                                               desired_foc_size,
                                               user_offset,
                                               bm_voltages,
                                               beam_offset,
                                               auto_dist,
                                               scaling_factor,
                                               scan_dir,
                                               min_slit_pos,
                                               max_slit_pos,
                                               slit_pos_scannable_name)
        optimisation.assert_called_once_with()
        self.assertEqual(run_opt(), optimisation)

    def test_footprint(self):
        """I think this is just dividing..."""
        self.assertEqual(8, bimorph.footprint(32, 4))
        self.assertEqual(roughly(3.2), bimorph.footprint(15.04, 4.7))


if __name__ == '__main__':
    unittest.main()
