
class I20SamplePreparer:
    def __init__(self):
        pass
    
    def prepare(self, sampleBean):
        
        if sampleBean.getUseSampleWheel():
            self.moveSampleWheel()
        
        return []