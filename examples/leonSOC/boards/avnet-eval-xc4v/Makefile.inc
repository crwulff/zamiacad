TECHNOLOGY=virtex4
ifeq ("$(CONFIG_FPGA_LX60)","y")
PART=xc4vlx60
else
PART=xc4vlx25
endif
PACKAGE=ff668
SPEED=-10
SYNFREQ=130
ifeq ("$(CONFIG_FPGA_LX60)","y")
PROMGENPAR=-x xcf32p -u 0 $(TOP).bit -p mcs -w -o avnet-eval-xc4v
else
PROMGENPAR=-x xcf08p -u 0 $(TOP).bit -p mcs -w -o avnet-eval-xc4v
endif
