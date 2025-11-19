'''
A Scannable class works with lattice matrix and lattice parameters. It provides
method to convert from lattice matrix to lattice parameters and vice verse.

Created on 21 Nov 2025

@author: fy65
'''
from gda.device.scannable import ScannableBase
import scisoftpy as dnp

class MatrixScannable(ScannableBase):
    '''
    a scannable provides support for lattice matrix and lattice parameters of a sample.
    '''

    def __init__(self, name, initial_value = dnp.full((3,3), dnp.nan).tolist()):
        '''
        Constructor - the initial value can be a lattice matrix or a lattice parameters.
        This scannable always returns lattice matrix.

        @param name: the name of this scannable
        @param initial_value: the initial value to use when this scannable instance is created
        initial value must be either lattice matrix in the format of [[x1,y1,z1],[x2,y2,z2],[x3,y3,z3]]
         or lattice parameters in the format of ["a","b","c","alpha_deg","beta_deg","gamma_deg"]
        '''
        self.setName(name)
        if self.is_lattice_matrix(initial_value):
            self.value = initial_value
            self._setMatrixInput(True)
        elif self.is_lattice_parameters(initial_value):
            self._setMatrixInput(False)
            a,b,c,alpha,beta,gamma = initial_value
            self.value = self.lattice_matrix_from_parameters(a, b, c, alpha, beta, gamma)
        else:
            raise ValueError("Initial value must be either a matrix [[x1,y1,z1],[x2,y2,z2],[x3,y3,z3]] or 6 lattice parameters [a,b,c,alpha_deg,beta_deg,gamma_deg]")

    def _setMatrixInput(self, ismatrix):
        if ismatrix:
            self.setInputNames(["row1","row2","row3"])
            self.setOutputFormat(["%f","%f","%f"])
            print("Info: Scannable '%s' is configured to take input value in a form of lattice matrix [[x1,y1,z1],[x2,y2,z2],[x3,y3,z3]]" % self.getName())
        else:
            self.setInputNames(["a","is_matrix","c","alpha_deg","beta_deg","gamma_deg"])
            self.setOutputFormat(["%f","%f","%f","%f","%f","%f"])
            print("Info: Scannable '%s' is configured to take input value in a form of lattice parameters [a,b,c,alpha_deg,beta_deg,gamma_deg] " % self.getName())

    def getPosition(self):
        ''' returns the lattice matrix of the sample
        '''
        self.setInputNames(["row1","row2","row3"])
        self.setOutputFormat(["%f","%f","%f"])
        if self.is_lattice_parameters(self.value):
            a,b,c,alpha,beta,gamma = self.value
            return self.lattice_matrix_from_parameters(a,b,c,alpha,beta,gamma)
        return self.value

    def is_lattice_matrix(self, initial_value):
        return all(type(i) is list for i in initial_value) and len(initial_value) == 3

    def is_lattice_parameters(self, new_pos):
        return all(isinstance(x, (int, float)) for x in new_pos) and len(new_pos) == 6

    def asynchronousMoveTo(self, new_pos):
        ''' sets either lattice matrix or lattice parameters only
        '''
        if self.is_lattice_matrix(new_pos) or self.is_lattice_parameters(new_pos):
            self.value = new_pos
        else:
            raise ValueError("Input is not a matrix [[x1,y1,z1],[x2,y2,z2],[x3,y3,z3]] or 6 lattice parameters [a,b,c,alpha_deg,beta_deg,gamma_deg]")

    def reset(self):
        self.value = dnp.full((3,3), dnp.nan).tolist()

    def isBusy(self):
        return False

    def namevaluepair(self):
        string=""
        position = self.getPosition()
        for name, value in zip([str(x) for x in self.getInputNames()], position):
            string += " " + name + " : " + str(value)
        return string


    def toFormattedString(self):
        ''' terminal print formatted string
        '''
        return "%s : %s" % (self.getName(), self.namevaluepair())

    def lattice_matrix_from_parameters(self, a, b, c, alpha_deg, beta_deg, gamma_deg):
        """
        Compute the lattice matrix from lattice parameters.

        Parameters:
        a, b, c : float
            Lattice lengths in angstrom.
        alpha_deg, beta_deg, gamma_deg : float
            Lattice angles in degrees.

        Returns:
        scisoftpy.ndarray
            3x3 lattice matrix.
        """
        # Convert angles to radians
        alpha = dnp.radians(alpha_deg)
        beta = dnp.radians(beta_deg)
        gamma = dnp.radians(gamma_deg)

        # Compute cosines and sines
        cos_alpha = dnp.cos(alpha)
        cos_beta = dnp.cos(beta)
        cos_gamma = dnp.cos(gamma)
        sin_gamma = dnp.sin(gamma)

        # Compute lattice vectors
        a1 = [a, 0.0, 0.0]
        a2 = [round(b * cos_gamma, 1), round(b * sin_gamma, 1), 0.0]

        # For a3 components
        a3_x = round(c * cos_beta, 1)
        a3_y = round(c * (cos_alpha - cos_beta * cos_gamma) / sin_gamma, 1)
        term = 1 - cos_beta**2 - ((cos_alpha - cos_beta * cos_gamma) / sin_gamma)**2
        a3_z = round(c * dnp.sqrt(term), 1)

        a3 = [a3_x, a3_y, a3_z]

        # Form the lattice matrix
        lattice_mat = dnp.array([a1, a2, a3]).T  # columns are lattice vectors

        return lattice_mat.tolist()

    def lattice_matrix_to_parameters(self, matrix):
        """
        Convert a 3x3 lattice matrix into lattice parameters (a, b, c, alpha, beta, gamma).

        Parameters:
            matrix (scisoftpy.ndarray): A 3x3 numpy array representing the lattice vectors as columns.

        Returns:
            dict: A dictionary containing lengths (a, b, c) and angles (alpha, beta, gamma) in degrees.
        """
        # Ensure the matrix is a numpy array
        matrix = dnp.array(matrix)

        # Extract lattice vectors (columns)
        a1 = matrix[:, 0]
        a2 = matrix[:, 1]
        a3 = matrix[:, 2]

        # Compute lengths
        a = dnp.linalg.norm(a1)
        b = dnp.linalg.norm(a2)
        c = dnp.linalg.norm(a3)

        # Compute angles in radians
        alpha_rad = dnp.arccos(dnp.dot(a2, a3) / (b * c))
        beta_rad = dnp.arccos(dnp.dot(a1, a3) / (a * c))
        gamma_rad = dnp.arccos(dnp.dot(a1, a2) / (a * b))

        # Convert angles to degrees
        alpha = dnp.degrees(alpha_rad)
        beta = dnp.degrees(beta_rad)
        gamma = dnp.degrees(gamma_rad)

        return {
            'a': a,
            'b': b,
            'c': c,
            'alpha': alpha.item(),
            'beta': beta.item(),
            'gamma': gamma.item()
        }
