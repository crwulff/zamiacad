
from java.io import PrintStream
from org.zamia.instgraph.interpreter.logger import Report

def checkSignalValue(signalName, expectedValue, sim):
  value = sim.getValue(PathName(signalName))
  return value.toString() == expectedValue

rebuild()

passedAssignments = []
# passedConditions = []
failedAssignments = []
# failedConditions = []

# Run Tests
for i in range(1, 45):

  if (i == 26):
    continue

  copy("tests/" + str(i) + "/code.txt", "CODE.TXT")

  sim = openSim()
  run(sim, "10000")

  assignmentsLog = sim.collectExecutedAssignments(str(i))
  # conditionsLog = sim.collectExecutedConditions(str(i))
    
  ok = checkSignalValue("RESULT", "49", sim)
  if (not ok):
    failedAssignments.append(assignmentsLog)
    # failedConditions.append(conditionsLog)
  else:
    passedAssignments.append(assignmentsLog)
    # passedConditions.append(conditionsLog)

reportAssignments = Report.createReport(failedAssignments, passedAssignments, "Assignments")
# reportConditions = Report.createReport(failedConditions, passedConditions, "Conditions")

# reportAssignments.printStat(PrintStream(file("assignments.txt")))
# reportAssignments.print(PrintStream(file("assignments_full.txt")))
# reportAssignments.getSuspects().printStat(PrintStream(file("assignments_red.txt")))
# reportConditions.printStat(PrintStream(file("conditions.txt")))
# reportConditions.print(PrintStream(file("conditions_full.txt")))
# reportConditions.getSuspects().printStat(PrintStream(file("conditions_red.txt")))

if (actionRootNode and actionXmlFile):
  reportAssignments.getSuspects().write2XMLFile(actionXmlFile, actionRootNode)