#!/bin/sh

rm -f *.vcd
rm -f *.o
rm -f *.cf
rm -f aggTest

ghdl -a aggTest.vhdl

ghdl -e aggTest

#ghdl -r aggTest --vcd=aggTest.vcd

