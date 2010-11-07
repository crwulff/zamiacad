module dEdgeFF (q, clock, data);

  output    q;
  reg       q;
  input     clock, data;

  initial
    q = 0;

  always
    @(negedge clock) #10 q = data;

endmodule

