#
# Clocks
#
define_clock            -name {clk_100}  -freq 130.000 -route 1.0 -clockgroup default_clkgroup

#define_clock            -name {leon3mp|ddrsp0.ddrc0.ddr_phy0.clk} -freq 100.000 -route 2.0 -clockgroup ddr_clkgroup
define_clock            -name {ddr2spa|ddr_phy0.ddr_phy0.xc4v.ddr_phy0.clkscale.mclkfx_derived_clock} -freq 200.000 -route 2.0 -clockgroup ddr_clkgroup
define_clock            -name {leon3mp|clkgen0.clkin}  -freq 100.000 -route 2.0 -clockgroup ahb_clkgroup
#define_clock            -name {leon3mp|eth1.e1.m100.u0.rxclk}  -freq 25.000 -route 10.0 -clockgroup rx100_clkgroup
#define_clock            -name {leon3mp|eth1.e1.m1000.u0.rxclk}  -freq 100.000 -route 2.0 -clockgroup rx1000_clkgroup

# Clock to Clock
#
#define_clock_delay           -rise {clk_100mhz} -fall {clk_100mhz} -false
#define_clock_delay -rise leon3mp|clkgen0.xc5l.v.clk0B_derived_clock -rise ddr2spa|ddr_phy0.ddr_phy0.xc4v.ddr_phy0.clk_270ro_derived_clock -false
#define_clock_delay -rise ddr2spa|ddr_phy0.ddr_phy0.xc4v.ddr_phy0.clk_0ro_derived_clock -rise leon3mp|clkgen0.xc5l.v.clk0B_derived_clock -false
#define_clock_delay -rise ddr2spa|ddr_phy0.ddr_phy0.xc4v.ddr_phy0.clk_270ro_derived_clock -rise leon3mp|clkgen0.xc5l.v.clk0B_derived_clock -false

#
# Inputs/Outputs
#
define_output_delay -disable     -default  10.00 -improve 0.00 -route 0.00 -ref {clk:r}
define_input_delay -disable      -default  10.00 -improve 0.00 -route 0.00 -ref {clk:r}

#
# Registers
#

#
# Multicycle Path
#

#
# False Path
#

#
# Path Delay
#

#
# Attributes
#
define_global_attribute          syn_useioff {1}

#
# I/O standards
#

#
# Compile Points
#

#
# Other Constraints
#
