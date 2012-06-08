# Role Based Access Control Test Scripts
# Run from GDA jython terminal using "run 'rbac_testing'"
import sys

RBAC_TEST_ITEMS = (
	# ( item, protection level, pos_param, (scan_params))
	(testLinearSM1, 2, 8, (1,10,2)),
	(testLinearSM2, 4, 8, (1,10,2)),
	(testAngularSM1, 3, 4, (1,10,2)),
	(testAngularSM2, 5, 4, (1,10,2)),
	(testLinearDOF1, 1, 8, (1,10,2)),
	(testLinearDOF2, 2, 8, (1,10,2)),
	(testAngularDOF1, 3, 8, (1,10,2)),
	(testAngularDOF2, 4, 8, (1,10,2)),
	(testCombinedDOF1, 10, 8, (1,10,2)),
	)

# part of the expected message text when an exception is thrown 
RBAC_PERMISSION_LOW_MSG_PART = "You need a permission level"
RBAC_BATON_NOT_HELD_MSG_PART = "You do not hold the baton"
RBAC_SCAN_HALTED_MSG_PART = "Scan halted"

def write_error(msg):
	print msg

def rbac_jython_test(command, user_holds_baton=None, user_permission_level=2, rbac_in_use=True):
	""" Runs the pos or scan command and checks that the appropriate exceptions are thrown. """

	print "\n\n==== Running RBAC jython terminal test ===="
	print "Testing command = %s" % (command,)
	if rbac_in_use:
		if user_holds_baton is None:
			print "----> ERROR: You need to specify user_holds_baton= as True or False"
			return
		if user_holds_baton:
			print "This test assumes that you are HOLDING the baton, and have logged in with user_permission_level = %d" % (user_permission_level)
		else:
			print "This test assumes that you are NOT HOLDING the baton"
	else:
		print "This test assumes that you are running with RBAC NOT ACTIVE"

	valid_commands = ("pos", "scan")
	error_count = 0
	if command not in valid_commands:
		print "----> ERROR: Invalid command=%s, must be one of %s" % (command, valid_commands)
		return

	for (item, item_protection_level, pos_param, scan_params) in RBAC_TEST_ITEMS:
		item_name = item.getName()

		# define settings if the command is expected to succeed
		message_to_look_for_prompt = None
		exception_message_expected = None

		# define settings if command is expected to fail
		# unfortunately pos and scan raise different exception types 
		if rbac_in_use:
			if user_holds_baton:
				if item_protection_level > user_permission_level:
					if command == "pos":
						exception_message_expected = RBAC_PERMISSION_LOW_MSG_PART
					else:
						message_to_look_for_prompt = RBAC_PERMISSION_LOW_MSG_PART
						exception_message_expected = RBAC_SCAN_HALTED_MSG_PART
			else:
				if command == "pos":
					exception_message_expected = RBAC_BATON_NOT_HELD_MSG_PART
				else:
					message_to_look_for_prompt = RBAC_BATON_NOT_HELD_MSG_PART
					exception_message_expected = RBAC_SCAN_HALTED_MSG_PART

		# inform the tester what we are about to do
		if command == "pos":
			print "\n>>>pos %s" % (item_name,)
			pos item
			print ">>>pos %s %d" % (item_name, pos_param)
		else:
			print "\n>>>scan %s %d %d %d" % (item_name, scan_params[0], scan_params[1], scan_params[2], )

		# run the command, catch any exceptions, and compare with expected result
		try:
			if message_to_look_for_prompt:
				print "----> Make sure a message with this text appears: %s..." % message_to_look_for_prompt
			if command == "pos":
				pos item pos_param
			else:
				scan item scan_params[0] scan_params[1] scan_params[2]
		except:
			etype, msg = sys.exc_info()[:2]
			if exception_message_expected:
				if not exception_message_expected in str(msg):
					write_error("----> Exception returned wrong message" +
						"\n----> Expected '%s ...', got %s" % (exception_message_expected, msg,))
					error_count += 1
			else:
				write_error("----> Unexpected exception caught in %s - %s, %s" % (command, type, exception))
				error_count += 1
		else:
			if exception_message_expected:
				write_error("----> Should have raised an exception with '%s ...', but did not" % (exception_message_expected,))
				error_count += 1			

	print "\n==== Done, %d errors reported. ====" % (error_count,)

print "\n==== RBAC test harness loaded ===="

