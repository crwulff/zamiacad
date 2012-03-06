
from subprocess import call, Popen, PIPE
import re
from java.lang import String

def cl(cmd):
  call(cmd, shell=True)

pattern = re.compile(r"([^:]*):([0-9]+):([0-9]+):(.*)$")


name = String(sourceFile.getName())
shName = name.substring(0, name.lastIndexOf(".vhd"))
parent = sourceFile.getParent()

os.chdir(parent)

cl("rm -rf work")
cl("mkdir work")

process = Popen(["ghdl", "-i", "--work=work", "--workdir=work", str(name)], stdout=PIPE)

marker_clean(sourceFileFullPath)

process = Popen(["ghdl", "-m", "--ieee=synopsys", "-fexplicit", "--workdir=work", "-Pwork", str(shName)], stderr=PIPE, stdout=PIPE)
out = process.stdout.read()
err = process.stderr.read()
for line in re.split("\n+", out):
  if line:
    printf(line)
for line in re.split("\n+", err):
  m = pattern.match(line)

  if not m:
    break;
  path = m.group(1)
  ln = m.group(2)
  col = m.group(3)
  msg = "[GHDL] " + m.group(4)
  if m.group(4).startswith("warning:"):
    error = False
  else:
    error = True

  if path and sourceFileFullPath.endswith(path) and ln and col:
    printf("Creating marker:%s, %s, %s, %s", sourceFileFullPath, ln, col, msg)
    marker_add(sourceFileFullPath, int(ln), int(col), msg, error)

cl("rm -rf work")
cl("rm -rf " + str(shName))

