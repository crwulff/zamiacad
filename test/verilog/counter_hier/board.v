module board();

  wire [3:0] count;
  wire       clock,
             f,
             af;

  m16   counter (count, clock, f, af);
  m555  clockGen (clock);

  always @(posedge clock)
    $display ($time, "count=%d, f=%d, af=%d", count, f, af);

endmodule
