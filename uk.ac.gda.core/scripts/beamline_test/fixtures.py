import pytest

@pytest.fixture
def meta():
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
def main():
    import __main__
    return __main__

@pytest.fixture
def config():
    from gda.configuration.properties import LocalProperties
    return LocalProperties.getConfigDir()

@pytest.fixture
def scan_command():
    import __main__
    yield __main__.scan

@pytest.fixture
def gaussian_pair():
    from gdascripts.pd.pd_gaussian import GaussianX, GaussianY
    gx = GaussianX('gx')
    gy = GaussianY('gy', gx, 0, 1, 1, 0, 0)
    return (gx, gy)

@pytest.fixture()
def find():
    from gda.factory import Finder
    return Finder.find
