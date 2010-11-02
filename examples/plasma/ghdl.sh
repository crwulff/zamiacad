#!/bin/sh

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
ghdl -i --work=work --workdir=work ram.vhd
ghdl -i --work=work --workdir=work reg_bank.vhd
ghdl -i --work=work --workdir=work shifter.vhd
ghdl -i --work=work --workdir=work uart.vhd

ghdl -m --ieee=synopsys -fexplicit --workdir=work -Pwork cpu_tb
ghdl -e --ieee=synopsys -fexplicit --workdir=work -Pwork cpu_tb
ghdl -r cpu_tb --vcd=cpu_tb.vcd --stop-time=10000ns
