
all:	counter_tb

ha.o:	ha.vhdl
	ghdl -a ha.vhdl

va.o:	va.vhdl ha.o
	ghdl -a va.vhdl

addg.o:	addg.vhdl va.o
	ghdl -a addg.vhdl

counter.o:	counter.vhdl addg.o
	ghdl -a counter.vhdl

counter_tb.o:	counter_tb.vhdl counter.o
	ghdl -a counter_tb.vhdl

counter_tb:	counter_tb.o 
	ghdl -e counter_tb	

run:	counter_tb
	ghdl -r counter_tb --vcd=counter_tb.vcd --stop-time=250ns

clean:	
	rm -f *.vcd *.o *.cf

