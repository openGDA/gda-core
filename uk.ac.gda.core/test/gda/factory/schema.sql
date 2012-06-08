drop database gda_test;

create database gda_test;

use gda_test;

CREATE TABLE objectfactory (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  PRIMARY KEY (id)
) TYPE=InnoDB;

CREATE TABLE amp_keithley (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  gpibInterfaceName varchar(100) NOT NULL,
  deviceName varchar(100) NOT NULL,
  timeout int(11) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;


CREATE TABLE archive_configuration (
  configuration_id int(11) NOT NULL auto_increment,
  archive_time datetime NOT NULL,
  PRIMARY KEY  (configuration_id)
) TYPE=InnoDB;


CREATE TABLE archive_element (
  element_id int(11) NOT NULL auto_increment,
  configuration_id int(11) NOT NULL,
  element_type varchar(20) NOT NULL,
  element_name varchar(20) NOT NULL,
  PRIMARY KEY  (element_id),
  INDEX (configuration_id),
  FOREIGN KEY (configuration_id) REFERENCES archive_configuration (configuration_id)
) TYPE=InnoDB;


CREATE TABLE archive_parameter (
  parameter_id int(11) NOT NULL auto_increment,
  element_id int(11) NOT NULL,
  element_value varchar(100) NOT NULL,
  PRIMARY KEY  (parameter_id),
  INDEX (element_id),
  FOREIGN KEY (element_id) REFERENCES archive_element (element_id)
) TYPE=InnoDB;


CREATE TABLE comm_serial (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  portName varchar(100) NOT NULL,
  baudRate int(11) NOT NULL,
  byteSize int(11) NOT NULL,
  stopBits int(11) NOT NULL,
  parity varchar(100) NOT NULL,
  flowcontrol varchar(100) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;


CREATE TABLE controller_aerotech3200 (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  local tinyint(1) NOT NULL,
  paramFile varchar(100) NOT NULL,
  numMotors int(11) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;


CREATE TABLE controller_mclennan (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  local tinyint(1) NOT NULL,
  serialDeviceName varchar(100) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;


CREATE TABLE controller_parker6k_enet (
  id int(11) NOT NULL auto_increment,
  name varchar(20) NOT NULL,
  port int(11) NOT NULL,
  host varchar(20) NOT NULL,
  controllerNo int(11) NOT NULL,
  maxNoOfMotors int(11) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;


CREATE TABLE controller_triax_gpib (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  deviceName varchar(100) NOT NULL,
  GpibInterfaceName varchar(100) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE current_config (
  id int(11) NOT NULL auto_increment,
  table_name varchar(100) NOT NULL,
  class_name varchar(100) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE current_config_gui (
  id int(11) NOT NULL auto_increment,
  table_name varchar(100) NOT NULL,
  class_name varchar(100) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE detector_dummy_exafs (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  active tinyint(1) NOT NULL,
  totalChans int(11) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE dimension (
  id int(11) NOT NULL auto_increment,
  width varchar(100) NOT NULL,
  height varchar(100) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE memory_gdhist (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  daServerName varchar(100) NOT NULL,
  dimension_id int(11) NOT NULL,
  openCommand varchar(100) NOT NULL,
  startupScript varchar(100) NOT NULL,
  sizeCommand varchar(100) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE modulator_pem90 (
  id int(11) NOT NULL auto_increment,
  parent_name varchar(100) ,
  name varchar(100) NOT NULL,
  serialDeviceName varchar(100) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE motor_aerotech (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  AerotechControllerName varchar(100) NOT NULL,
  axis int(11) NOT NULL,
  homeable tinyint(1) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE motor_dummy (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  backlashSteps double NOT NULL,
  sleepTime float NOT NULL,
  nonContinuousIncrements int(11) NOT NULL,
  fastSpeed int(11) NOT NULL,
  mediumSpeed int(11) NOT NULL,
  slowSpeed int(11) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE motor_epics (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  fastSpeed int(11) NOT NULL,
  mediumSpeed int(11) NOT NULL,
  slowSpeed int(11) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE motor_newport (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  NewportControllerName varchar(100) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE motor_mclennan_600 (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  mcLennanControllerName varchar(100) NOT NULL,
  axis int(11) NOT NULL,
  slewSpeed double NOT NULL,
  backlashSteps double NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE motor_mclennan_servo (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  mcLennanControllerName varchar(100) NOT NULL,
  axis int(11) NOT NULL,
  slewSpeed double NOT NULL,
  backlashSteps double NOT NULL,
  isMaster tinyint(1) NOT NULL,
  slaveAxis int(11) NOT NULL,
  offset double NOT NULL ,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE motor_mclennan_stepper (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  mcLennanControllerName varchar(100) NOT NULL,
  axis int(11) NOT NULL,
  channel int(11) NOT NULL,
  slewSpeed double NOT NULL,
  backLashSteps double NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE motor_parker6k (
  id int(11) NOT NULL auto_increment,
  name varchar(20) NOT NULL,
  Parker6kControllerName varchar(20) NOT NULL,
  axisNo int(11) NOT NULL,
  isStepper tinyint(1) NOT NULL,
  minPosition double NOT NULL,
  maxPosition double NOT NULL,
  minSpeed double NOT NULL,
  maxSpeed double NOT NULL,
  fastSpeed int(11) NOT NULL,
  mediumSpeed int(11) NOT NULL,
  slowSpeed int(11) NOT NULL,
  limitsSettable tinyint(1) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE motor_triax (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  triaxControllerName varchar(100) NOT NULL,
  identifier varchar(100) NOT NULL,
  slitNumber int(11) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE temp_eurotherm2000 (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  serialDeviceName varchar(100) NOT NULL,
  accuracy double NOT NULL,
  polltime float NOT NULL,
  gid int(11) NOT NULL,
  uid int(11) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE dummygpib (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE generic_oe (
  oe_id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  PRIMARY KEY  (oe_id)
) TYPE=InnoDB;

CREATE TABLE dof_singleaxislinear (
id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  protectionLevel int(11) NOT NULL,
  reportingUnit varchar(100) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE dof_singleaxisangular (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  protectionLevel int(11) NOT NULL,
  reportingUnit varchar(100) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE dof_doubleaxislinear (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  protectionLevel int(11) NOT NULL,
  reportingUnit varchar(100) NOT NULL,
  axisOffset double NOT NULL,
  centralOffset tinyint(1) NOT NULL,
  separation double NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE dof_doubleaxisangular (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  protectionLevel int(11) NOT NULL,
  reportingUnit varchar(100) NOT NULL,
  axisOffset double NOT NULL,
  centralOffset tinyint(1) NOT NULL,
  separation double NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;


CREATE TABLE dof_doubleaxisgapposition (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  protectionLevel int(11) NOT NULL,
  reportingUnit varchar(100) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE dof_doubleaxisgapwidth (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  protectionLevel int(11) NOT NULL,
  reportingUnit varchar(100) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE dof_doubleaxisparallellinear (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  protectionLevel int(11) NOT NULL,
  reportingUnit varchar(100) NOT NULL,
  opposing bit NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE dof_mono (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  protectionLevel int(11) NOT NULL,
  reportingUnit varchar(100) NOT NULL,
  twoD double NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE dof_coupled (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  protectionLevel int(11) NOT NULL,
  reportingUnit varchar(100) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE dof_sinedrivewavelength (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  protectionLevel int(11) NOT NULL,
  reportingUnit varchar(100) NOT NULL,
  gratingDensity double NOT NULL,
  correctionFactor double NOT NULL,
  sineArmLength double NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE dof_sinedriveenergy (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  protectionLevel int(11) NOT NULL,
  reportingUnit varchar(100) NOT NULL,
  gratingDensity double NOT NULL,
  correctionFactor double NOT NULL,
  sineArmLength double NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE dof_sinedriveangular (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  protectionLevel int(11) NOT NULL,
  reportingUnit varchar(100) NOT NULL,
  angleOffset double NOT NULL,
  angleUpperLimit double NOT NULL,
  angleLowerLimit double NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE dof_epics (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  protectionLevel int(11) NOT NULL,
  reportingUnit varchar(100) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE function_linear (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  slopeDividend varchar(100) NOT NULL,
  slopeDivisor varchar(100) NOT NULL,
  interception varchar(100) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE function_identity (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;


CREATE TABLE positioner_linear (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  motorName varchar(100) NOT NULL,
  stepsPerUnit double NOT NULL,
  pollTime float NOT NULL,
  softLimitLow double NOT NULL,
  softLimitHigh double NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE positioner_angular (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  motorName varchar(100) NOT NULL,
  stepsPerUnit double NOT NULL,
  pollTime float NOT NULL,
  softLimitLow double NOT NULL,
  softLimitHigh double NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE positioner_servo (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  adcName varchar(100) NOT NULL,
  piezoName varchar(100) NOT NULL,
  PRIMARY KEY  (id)
) TYPE=InnoDB;

CREATE TABLE stringholder (
  owner varchar(100)  NOT NULl,
  string varchar(100)  NOT NULL
) TYPE=InnoDB;

CREATE TABLE dofname (
  owner varchar(100)  NOT NULl,
  string varchar(100)  NOT NULL
) TYPE=InnoDB;

CREATE TABLE moveablename (
  owner varchar(100)  NOT NULl,
  string varchar(100)  NOT NULL
) TYPE=InnoDB;

CREATE TABLE panel_configure (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  PRIMARY KEY (id)
) TYPE=InnoDB;

CREATE TABLE panel_status (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  PRIMARY KEY (id)
) TYPE=InnoDB;

CREATE TABLE panel_environment (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  PRIMARY KEY (id)
) TYPE=InnoDB;

CREATE TABLE panel_graph (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  PRIMARY KEY (id)
) TYPE=InnoDB;

CREATE TABLE panel_oemove (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  PRIMARY KEY (id)
) TYPE=InnoDB;

CREATE TABLE panel_monosetup (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  monochromatorName varchar(100) NOT NULL,
  counter1Name varchar(100) NOT NULL,
  counter2Name varchar(100) NOT NULL,
  gratingDofName varchar(100) NOT NULL,
  mirrorDofName varchar(100) NOT NULL,
  gratingLinesPerMM double NOT NULL,
  gratingArmLength double NOT NULL,
  mirrorArmLength double NOT NULL,
  gratingAngleOffset double NOT NULL,
  mirrorAngleOffset double NOT NULL,
  PRIMARY KEY (id)
) TYPE=InnoDB;

CREATE TABLE panel_generalscan (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  PRIMARY KEY (id)
) TYPE=InnoDB;

CREATE TABLE panel_exafs (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  PRIMARY KEY (id)
) TYPE=InnoDB;

CREATE TABLE panel_configure_mi (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  PRIMARY KEY (id)
) TYPE=InnoDB;

CREATE TABLE panel_editor (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  jythonTerminalName varchar(100) NOT NULL,
  PRIMARY KEY (id)
) TYPE=InnoDB;

CREATE TABLE panel_terminal (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  PRIMARY KEY (id)
) TYPE=InnoDB;

CREATE TABLE panel_ni6602counter (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  counter1Name varchar(100) NOT NULL,
  counter2Name varchar(100) NOT NULL,
  PRIMARY KEY (id)
) TYPE=InnoDB;

CREATE TABLE panel_vuv_configure (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  PRIMARY KEY (id)
) TYPE=InnoDB;

CREATE TABLE panel_vuv_scan (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  PRIMARY KEY (id)
) TYPE=InnoDB;

CREATE TABLE panel_vuv_timescan (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  PRIMARY KEY (id)
) TYPE=InnoDB;

CREATE TABLE panel_vuv_temperaturescan (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  PRIMARY KEY (id)
) TYPE=InnoDB;

CREATE TABLE panel_vuv_datareduction (
  id int(11) NOT NULL auto_increment,
  name varchar(100) NOT NULL,
  PRIMARY KEY (id)
) TYPE=InnoDB;



/*
add entries to the list of tables.  In other rdbms's, this table could be replaced by a view.

This must be server-side objects only.  NO GUI PANELS IN THIS SECTION.
*/

insert into current_config(class_name,table_name)
values ('dl.device.motor.DummyMotor','motor_dummy');

insert into current_config(class_name,table_name)
values ('dl.device.motor.EpicsMotor','motor_epics');

insert into current_config(class_name,table_name)
values ('dl.device.motor.NewportMotor','motor_newport');

insert into current_config(class_name,table_name)
values ('dl.device.motor.Parker6kControllerEnet','controller_parker6k_enet');

insert into current_config(class_name,table_name)
values ('dl.device.motor.Parker6kMotor','motor_parker6k');

insert into current_config(class_name,table_name)
values ('dl.device.motor.McLennanController','controller_mclennan');

insert into current_config(class_name,table_name)
values ('dl.device.motor.McLennanStepperMotor','motor_mclennan_stepper');

insert into current_config(class_name,table_name)
values ('dl.device.motor.McLennanServoMotor','motor_mclennan_servo');

insert into current_config(class_name,table_name)
values ('dl.device.motor.McLennan600Motor','motor_mclennan_600');

insert into current_config(class_name,table_name)
values ('dl.device.motor.TriaxMotor','motor_triax');

insert into current_config(class_name,table_name)
values ('dl.device.motor.AerotechMotor','motor_aerotech');

insert into current_config(class_name,table_name)
values ('dl.device.motor.Aerotech3200Controller','controller_aerotech3200');

insert into current_config(class_name,table_name)
values ('dl.device.motor.NewportMotor','motor_newport');

insert into current_config(class_name,table_name)
values ('dl.device.countertimer.DummyCounterTimer','detector_dummy_exafs');

insert into current_config(class_name,table_name)
values ('dl.device.amplifier.Keithley','amp_keithley');

insert into current_config(class_name,table_name)
values ('dl.device.memory.Gdhist','memory_gdhist');

insert into current_config(class_name,table_name)
values ('dl.device.temperature.Eurotherm2000','temp_eurotherm2000');

insert into current_config(class_name,table_name)
values ('dl.device.serial.SerialComm','comm_serial');

insert into current_config(class_name,table_name)
values ('dl.device.modulator.PEM90','modulator_pem90');

insert into current_config(class_name,table_name)
values ('dl.device.gpib.DummyGpib','dummygpib');

insert into current_config(class_name,table_name)
values ('dl.vuv.spectrometer.TriaxControllerGPIB','controller_triax_gpib');

insert into current_config(class_name,table_name)
values ('dl.oe.GenericOE','generic_oe');

insert into current_config(class_name,table_name)
values ('dl.oe.positioners.LinearPositioner','positioner_linear');

insert into current_config(class_name,table_name)
values ('dl.oe.positioners.AngularPositioner','positioner_angular');

insert into current_config(class_name,table_name)
values ('dl.oe.positioners.ServoPositioner','positioner_servo');

insert into current_config(class_name,table_name)
values ('dl.oe.dofs.SingleAxisLinearDOF','dof_singleaxislinear');

insert into current_config(class_name,table_name)
values ('dl.oe.dofs.SingleAxisAngularDOF','dof_singleaxisangular');

insert into current_config(class_name,table_name)
values ('dl.oe.dofs.DoubleAxisLinearDOF','dof_doubleaxislinear');

insert into current_config(class_name,table_name)
values ('dl.oe.dofs.DoubleAxisAngularDOF','dof_doubleaxisangular');

insert into current_config(class_name,table_name)
values ('dl.oe.dofs.DoubleAxisGapPositionDOF','dof_doubleaxisgapposition');

insert into current_config(class_name,table_name)
values ('dl.oe.dofs.DoubleAxisGapWidthDOF','dof_doubleaxisgapwidth');

insert into current_config(class_name,table_name)
values ('dl.oe.dofs.MonoDOF','dof_mono');

insert into current_config(class_name,table_name)
values ('dl.oe.dofs.CoupledDOF','dof_coupled');

insert into current_config(class_name,table_name)
values ('dl.oe.dofs.EpicsDOF','dof_epics');

insert into current_config(class_name,table_name)
values ('dl.oe.dofs.DoubleAxisParallelLinearDOF','dof_doubleaxisparallellinear');

insert into current_config(class_name,table_name)
values ('dl.oe.dofs.SineDriveWavelengthDOF','dof_sinedrivewavelength');

insert into current_config(class_name,table_name)
values ('dl.oe.dofs.SineDriveEnergyDOF','dof_sinedriveenergy');

insert into current_config(class_name,table_name)
values ('dl.oe.dofs.SineDriveAngularDOF','dof_sinedriveangular');

insert into current_config(class_name,table_name)
values ('dl.function.LinearFunction','function_linear');

insert into current_config(class_name,table_name)
values ('dl.function.IdentityFunction','function_identity');









