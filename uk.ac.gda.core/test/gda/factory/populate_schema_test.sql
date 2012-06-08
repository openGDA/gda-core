use gda_test;

insert into objectfactory(name)
values('TestFactory');

insert into motor_dummy(name,backlashSteps,sleepTime,nonContinuousIncrements,fastSpeed,mediumSpeed,slowSpeed)
values('Test01Dummy',2,5000,10,50,25,5);

insert into controller_parker6k_enet(id,name,host,port)
values(1,'Test01Controller','testHostname',5002);

insert into motor_parker6k(name,Parker6kControllerName,axisNo,isStepper,minPosition,
   maxPosition,minSpeed,maxSpeed,fastSpeed,mediumSpeed,slowSpeed)
values('Test01Parker','Test01Controller',1,1,-1000000.0,1000000.0,0.0,2000.0,50,25,5);

insert into amp_keithley(name,gpibInterfaceName,deviceName,timeout)
values('Test01Keithley','Test01Gpib','dev1',1000);

insert into dimension(width,height)values(512,1);

insert into memory_gdhist(name,daServerName,dimension_id,openCommand,startupScript,sizeCommand)
values('TestMemory','TestdaServer',1,'test open','test startup','test size');

insert into temp_eurotherm2000(name,serialDeviceName,accuracy,polltime,gid,uid)
values('TestEurotherm','testPort',2.0,5000.0,0,1);
insert into stringholder(owner,string)values('TestEurotherm','Internal');
insert into stringholder(owner,string)values('TestEurotherm','External');

insert into comm_serial(name,portName,baudRate,byteSize,stopBits,parity,flowcontrol)
values('COM4','/dev/term/a',9600,7,1,'even','None');

insert into controller_mclennan(name,serialDeviceName)
values('w0_Controller','w0');

insert into motor_mclennan_stepper(name,MclennanControllerName,axis,slewSpeed,backlashSteps)
values('EOB_YMotor','w0',4,1000,800);

insert into motor_mclennan_servo(name,MclennanControllerName,axis,slewSpeed,backlashSteps,offset)
values('AlphaMotor','w0',6,10000,260,-4.150);

insert into motor_mclennan_600(name,MclennanControllerName,axis,slewSpeed,backlashSteps)
values('PhiMotor','w0',4,10000,800);

insert into modulator_pem90(name,serialDeviceName)
values('PRS232','w0');

insert into controller_triax_gpib(name,deviceName,GpibInterfaceName)
values('TCGPIB','dev1','gpib');

insert into motor_triax(name,triaxControllerName,identifier,slitNumber)
values('gmotor','TCGPIB','mono',1);

insert into dummygpib(name)values('Test01Gpib');

insert into positioner_linear(id,name,motorName,stepsPerUnit,pollTime,softLimitLow,softLimitHigh)
values(1,'testFront','testLinearMotor',1000,750,-10000,10000);

insert into positioner_angular(id,name,motorName,stepsPerUnit)
values(1,'testRotate','testAngularMotor',500);

insert into positioner_servo(id,name,adcName,piezoName)
values(1,'testServo','testAdc','testPiezo');

insert into positioner_linear(id,name,motorName,stepsPerUnit,pollTime,softLimitLow,softLimitHigh)
values(2,'testBackLinear','testMotor',1000,750,-10000,10000);

insert into dof_singleaxislinear(id,name,protectionLevel,reportingUnit)
values(1,'testUprightLinear',0,'mm');
insert into moveablename(owner,string)values('testUprightLinear','testBackLinear');

insert into positioner_angular(id,name,motorName,stepsPerUnit,pollTime,softLimitLow,softLimitHigh)
values(2,'testBackAngular','testBack',1000,750,-10000,10000);

insert into dof_singleaxisangular(id,name,protectionLevel,reportingUnit)
values(1,'testUprightAngular',0,'mdeg');
insert into moveablename(owner,string)values('testUprightAngular','testBackAngular');

insert into generic_oe(name) values('OneDegreeTable');
insert into dofname(owner,string)values('OneDegreeTable','YTrans');
insert into moveablename(owner,string)values('OneDegreeTable','ypositioner');

insert into dof_singleaxislinear(id,name,protectionLevel,reportingUnit)
values(2,'YTrans',0,'mm');
insert into moveablename(owner,string)values('YTrans','ypositioner');

insert into positioner_linear(id,name,motorName,stepsPerUnit,softLimitLow,softLimitHigh,pollTime)
values(3,'ypositioner','testMotor',1000,-10000,10000,1000);

insert into motor_epics(name,fastSpeed,mediumSpeed,slowSpeed)
values('TSXXX-MO-HSLIT-01:NEGB:MOT',50,25,5);

insert into motor_newport(name,NewportControllerName)
values('newportmoptor01','newportcontroller01');

#<!-- Slit -->
insert into generic_oe(name) values('slit01');
insert into dofname(owner,string)values('slit01','HorizGapWidth01');
insert into dofname(owner,string)values('slit01','HorizGapPosition01');
insert into moveablename(owner,string)values('slit01','positioner6');
insert into moveablename(owner,string)values('slit01','positioner7');

insert into dof_doubleaxisgapwidth(name,protectionLevel,reportingUnit)
values('HorizGapWidth01',0,'mm');
insert into moveablename(owner,string)values('HorizGapWidth01','positioner6');
insert into moveablename(owner,string)values('HorizGapWidth01','positioner7');
insert into dof_doubleaxisgapposition(name,protectionLevel,reportingUnit)
values('HorizGapPosition01',0,'mm');
insert into moveablename(owner,string)values('HorizGapPosition01','positioner6');
insert into moveablename(owner,string)values('HorizGapPosition01','positioner7');

insert into positioner_linear(name,motorName,stepsPerUnit,softLimitLow,softLimitHigh,pollTime)
values('positioner6','Test01Dummy',1000,-10000,10000,1000);
insert into positioner_linear(name,motorName,stepsPerUnit,softLimitLow,softLimitHigh,pollTime)
values('positioner7','Test01Dummy',1000,-10000,10000,1000);

#<!-- Mirror -->
insert into generic_oe(name) values('Mirror01');
insert into dofname(owner,string)values('Mirror01','VertMoveMirror01');
insert into dofname(owner,string)values('Mirror01','RotateMirror01');
insert into moveablename(owner,string)values('Mirror01','positioner12');
insert into moveablename(owner,string)values('Mirror01','positioner13');

insert into dof_doubleaxislinear(name,protectionLevel,reportingUnit)
values('VertMoveMirror01',0,'mm');
insert into moveablename(owner,string)values('VertMoveMirror01','positioner12');
insert into moveablename(owner,string)values('VertMoveMirror01','positioner13');
insert into dof_doubleaxisangular(name,protectionLevel,reportingUnit,separation,axisOffset,centralOffset)
values('RotateMirror01',0,'deg',1250.0,625.0,1);
insert into moveablename(owner,string)values('RotateMirror01','positioner12');
insert into moveablename(owner,string)values('RotateMirror01','positioner13');

insert into positioner_linear(name,motorName,stepsPerUnit,softLimitLow,softLimitHigh,pollTime)
values('positioner12','Test01Dummy',1000,-10000,10000,1000);
insert into positioner_linear(name,motorName,stepsPerUnit,softLimitLow,softLimitHigh,pollTime)
values('positioner13','Test01Dummy',1000,-10000,10000,1000);

#<!-- mono -->
insert into generic_oe(name) values('Mono');
insert into dofname(owner,string)values('Mono','Wavelength');
insert into dofname(owner,string)values('Mono','MonoPlusXtrans');
insert into moveablename(owner,string)values('Mono','positioner15');
insert into moveablename(owner,string)values('Mono','positioner21');

insert into dof_mono(name,protectionLevel,reportingUnit,twoD)
values('Wavelength',0,'mm',6.271);
insert into moveablename(owner,string)values('Wavelength','positioner21');
insert into dof_coupled(name,protectionLevel,reportingUnit)
values('MonoPlusXtrans',0,'mdeg');
insert into moveablename(owner,string)values('MonoPlusXtrans','Bragg');
insert into moveablename(owner,string)values('MonoPlusXtrans','MonoX');
insert into stringholder(owner,string)values('MonoPlusXtrans','identity1');
insert into stringholder(owner,string)values('MonoPlusXtrans','XTransForBragg');

insert into positioner_linear(name,motorName,stepsPerUnit,softLimitLow,softLimitHigh,pollTime)
values('positioner15','Test01Dummy',1000,-10000,30000,1000);
insert into positioner_angular(name,motorName,stepsPerUnit,softLimitLow,softLimitHigh)
values('positioner21','Test01Dummy',1000,-10000,30000);

insert into function_identity(name) values('identity1');
insert into function_linear(name,slopeDividend,slopeDivisor,interception)
values('XTransForBragg','0.05 mm','1.0 mdeg','10.0 mm');

#<!-- AppleTwo Undulator-->
insert into generic_oe(name) values('Undulator5u');
insert into dofname(owner,string)values('Undulator5u','OpposingPhase');
insert into moveablename(owner,string)values('Undulator5u','positioner37');
insert into moveablename(owner,string)values('Undulator5u','positioner38');

insert into dof_doubleaxisparallellinear(name,protectionLevel,reportingUnit,opposing)
values('OpposingPhase',0,'mm',1);
insert into moveablename(owner,string)values('OpposingPhase','positioner37');
insert into moveablename(owner,string)values('OpposingPhase','positioner38');

insert into positioner_linear(name,motorName,stepsPerUnit,softLimitLow,softLimitHigh,pollTime)
values('positioner37','Test01Dummy',1000,-10000,10000,1000);
insert into positioner_linear(name,motorName,stepsPerUnit,softLimitLow,softLimitHigh,pollTime)
values('positioner38','Test01Dummy',1000,-10000,10000,1000);


#<!-- SineDrive Monochromator-->
insert into generic_oe(name) values('Analyser');
insert into dofname(owner,string)values('Analyser','SDWavelength');
insert into dofname(owner,string)values('Analyser','SDEnergy');
insert into moveablename(owner,string)values('Analyser','positioner39');

insert into dof_sinedrivewavelength(name,protectionLevel,reportingUnit,sineArmLength,correctionFactor,gratingDensity)
values('SDWavelength',0,'nm',471.4,1,300);
insert into moveablename(owner,string)values('SDWavelength','positioner39');
insert into dof_sinedriveenergy(name,protectionLevel,reportingUnit,sineArmLength,correctionFactor,gratingDensity)
values('SDEnergy',0,'eV',471.4,1,300);
insert into moveablename(owner,string)values('SDEnergy','positioner39');

insert into positioner_linear(name,motorName,stepsPerUnit,softLimitLow,softLimitHigh,pollTime)
values('positioner39','Test01Dummy',1000,-10000,10000,1000);


