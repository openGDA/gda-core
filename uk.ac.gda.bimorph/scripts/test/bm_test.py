import unittest
from gda.configuration.properties import LocalProperties
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
        
    def test_ellipse_method0(self):
        LocalProperties.set("gda.data.scan.datawriter.dataFormat", None);
        pixel_size = 6.5
        p_1 = 46.5
        q_1 = 0.4
        theta_1 = 3
        p_2 = 46.5
        q_2 = 0.27
        theta_2 = 2
        i_sign = -1
        detector_distance = 0.4
        slit_start = -0.15
        slit_end = 0.12
        slit_step = 0.01
        column = "peak2d_peaky"
        inv = 1
        method = 0
        el = ellipse.EllipseCalculator(pixel_size, p_1, q_1, theta_1, p_2, q_2, theta_2, i_sign, detector_distance, slit_start, slit_end, slit_step, column, inv, method)    
        el.calcSlopes()
        beamPositions = []
        for pos in el.getBeamPositions():
            beamPositions.append(round(pos, 2))
        self.assertEquals(beamPositions, [-0.15,-0.14,-0.13,-0.12,-0.11,-0.1,-0.09,-0.08,-0.07,-0.06,-0.05,-0.04,-0.03,-0.02,-0.01,0.0,0.01,0.02,0.03,0.04,0.05,0.06,0.07,0.08,0.09,0.1,0.11,0.12])
        errors = []
        for e in el.getErrors():
            errors.append(round(e, 6))
        self.assertEquals(errors, [0.966437,0.858933,0.756076,0.658116,0.565314,0.477945,0.396298,0.320679,0.251409,0.188826,0.133288,0.085170,0.044869,0.012806,-0.010578,-0.024815,-0.029410,-0.023840,-0.007553,0.020037,0.059551,0.111646,0.177018,0.256407,0.350598,0.460425,0.586773,0.730586])
    
    def test_ellipse_method1(self):
        LocalProperties.set("gda.data.scan.datawriter.dataFormat", None);
        pixel_size = 4.65
        p_1 = 45.1
        q_1 = 0.4
        theta_1 = 3
        p_2 = 45.1
        q_2 = 0.25
        theta_2 = 2
        i_sign = 1.0
        detector_distance = 0.4
        slit_start = 3.0
        slit_end = 3.9
        slit_step = 0.01
        column = "peak2d_peaky"
        inv = 1
        method = 1
        el = ellipse.EllipseCalculator(pixel_size, p_1, q_1, theta_1, p_2, q_2, theta_2, i_sign, detector_distance, slit_start, slit_end, slit_step, column, inv, method)    
        el.calcCamPos()
        beamPositions = []
        for pos in el.getBeamPositions():
            beamPositions.append(round(pos, 2))
        vals=[round(x * 0.01, 2) for x in range(300, 391)]
        self.assertEquals(vals, beamPositions)
        errors = []
        for e in el.getErrors():
            errors.append(round(e, 8))
            vals=[-97.13370606    ,-100.44586078    ,-103.60163457    ,-106.61296492    ,-109.49055120    ,-112.24401507    ,-114.88203607    ,-117.41246708    ,-119.84243284    ,-122.17841453    ,-124.42632260    ,-126.59155986    ,-128.67907616    ,-130.69341610    ,-132.63876068    ,-134.51896390    ,-136.33758492    ,-138.09791645    ,-139.80300981    ,-141.45569729    ,-143.05861192    ,-144.61420513    ,-146.12476256    ,-147.59241822    ,-149.01916715    ,-150.40687685    ,-151.75729760    ,-153.07207174    ,-154.35274206    ,-155.60075945    ,-156.81748984    ,-158.00422043    ,-159.16216551    ,-160.29247160    ,-161.39622231    ,-162.47444268    ,-163.52810317    ,-164.55812338    ,-165.56537539    ,-166.55068690    ,-167.51484407    ,-168.45859419    ,-169.38264808    ,-170.28768238    ,-171.17434162    ,-172.04324014    ,-172.89496392    ,-173.73007221    ,-174.54909909    ,-175.35255490    ,-176.14092758    ,-176.91468396    ,-177.67427084    ,-178.42011615    ,-179.15262993    ,-179.87220530    ,-180.57921932    ,-181.27403385    ,-181.95699633    ,-182.62844047    ,-183.28868699    ,-183.93804424    ,-184.57680881    ,-185.20526609    ,-185.82369083    ,-186.43234763    ,-187.03149141    ,-187.62136788    ,-188.20221395    ,-188.77425813    ,-189.33772093    ,-189.89281517    ,-190.43974639    ,-190.97871310    ,-191.50990712    ,-192.03351387    ,-192.54971264    ,-193.05867683    ,-193.56057420    ,-194.05556714    ,-194.54381283    ,-195.02546352    ,-195.50066666    ,-195.96956514    ,-196.43229746    ,-196.88899787    ,-197.33979658    ,-197.78481992    ,-198.22419041    ,-198.65802702    ,-199.08644520]    
        self.assertEquals(errors, vals)
        
    def test_ellipse_method2(self):
        LocalProperties.set("gda.data.scan.datawriter.dataFormat", None);
        i_sign = -1
        inv = +1
        p_1 = 215.0
        q_1 = 0.403
        theta_1 = 2.91
        p_2 = 215.0
        q_2 = 0.270
        theta_2 = 2.40
        detector_distance = 0.403
        pixel_size = 0.19
        slit_start=-0.18
        slit_end=0.15
        slit_step=0.01
        column = "peak2d_peaky"
        method = 1
        el = ellipse.EllipseCalculator(pixel_size, p_1, q_1, theta_1, p_2, q_2, theta_2, i_sign, detector_distance, slit_start, slit_end, slit_step, column, inv, method)    
        error = el.calcCamPos2()
        vals=[-152.95537584088277, -146.0067144566203, -138.79892000444625, -131.321053601543, -123.57008690659926, -115.53703219966715, -107.21323801069101, -98.58946668603264, -89.65963325954903, -80.41555979624799, -70.84411798829083, -60.93899238131607, -50.68674169402116, -40.07888499076346, -29.1094410205496, -17.764287806377116, -6.008645856356755, 6.131089050918185, 18.678721615770744, 31.665749537210882, 45.089467512247644, 58.97606647492202, 73.33186095629578, 88.1814554391088, 103.53571610603831, 119.41707143257197, 135.84112959299773, 152.82755061183082, 170.3979097901861, 188.57536950936245, 207.382618204007, 226.84377404531574, 246.9849629193695, 267.8331781177888]
        self.assertEquals(error, vals)
    
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