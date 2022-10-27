import pytest

from gda.device.scannable import DummyScannable

class DefaultGdaFixtures(object):
    """
    Default fixtures for all GDA tests

    Additional fixtures can be added per-beamline by adding them to a conftest.py
    file in the root of the tests directory for that beamline.
    """
    @pytest.fixture
    def meta(self):
        from gdaserver import GDAMetadata as meta
        initial = {}
        for entry in meta.getMetadataEntries():
            initial[entry.name] = (entry, entry.metadataValue)
        try:
            yield meta
        finally:
            meta.setMetadataEntries([entry for (entry, _) in initial.values()])
            for name, (_, value) in initial.items():
                meta[name] = value

    @pytest.fixture
    def main(self):
        import __main__
        return __main__

    @pytest.fixture
    def config(self):
        from gda.configuration.properties import LocalProperties
        return LocalProperties.getConfigDir()

    @pytest.fixture
    def scan_command(self, main):
        yield main.scan if hasattr(main, 'scan') else scan

    @pytest.fixture
    def gaussian_pair(self):
        from gdascripts.pd.pd_gaussian import GaussianX, GaussianY
        gx = GaussianX('gx')
        gy = GaussianY('gy', gx, 0, 1, 1, 0, 0)
        return (gx, gy)

    @pytest.fixture
    def gaussian_2d(self):
        from gdascripts.pd.pd_gaussian import Gaussian2d
        x = DummyScannable('x')
        y = DummyScannable('y')
        g = Gaussian2d('g', x, y)
        return x, y, g

    @pytest.fixture()
    def find(self):
        from gda.factory import Finder
        return Finder.find
