#
# Clocks
#
define_clock            -name {clk}  -freq 130.000 -route 1.0 -clockgroup default_clkgroup

define_clock            -name {leon3mp|clkgen0.clkin}  -freq 100.000 -route 2.0 -clockgroup ahb_clkgroup

# Clock to Clock

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
