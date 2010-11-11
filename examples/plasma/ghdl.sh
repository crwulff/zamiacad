#!/bin/sh

rm -rf work
mkdir work

ghdl -i --work=work --workdir=work alu_tb.vhd
ghdl -i --work=work --workdir=work alu.vhd
ghdl -i --work=work --workdir=work bus_mux.vhd
ghdl -i --work=work --workdir=work control.vhd
ghdl -i --work=work --workdir=work cpu_testbench.vhd
ghdl -i --work=work --workdir=work mem_ctrl.vhd
ghdl -i --work=work --workdir=work mlite2sram.vhd
ghdl -i --work=work --workdir=work mlite2uart.vhd
ghdl -i --work=work --workdir=work mlite_cpu.vhd
ghdl -i --work=work --workdir=work mlite_pack.vhd
ghdl -i --work=work --workdir=work mult.vhd
ghdl -i --work=work --workdir=work pc_next.vhd
ghdl -i --work=work --workdir=work pipeline.vhd
ghdl -i --work=work --workdir=work plasma.vhd
ghdl -i --work=work --workdir=work reg_bank.vhd
ghdl -i --work=work --workdir=work shifter.vhd
ghdl -i --work=work --workdir=work uart.vhd
ghdl -i --work=work --workdir=work tbench.vhd
ghdl -i --work=work --workdir=work sram2mlite.vhd
ghdl -i --work=work --workdir=work ram.vhd

ghdl -m --ieee=synopsys -fexplicit --workdir=work -Pwork tbench
ghdl -e --ieee=synopsys -fexplicit --workdir=work -Pwork tbench
ghdl -r tbench --vcd=tbench.vcd --stop-time=100000ns
