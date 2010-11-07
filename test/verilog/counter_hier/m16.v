module m16 (value, clock, fifteen, altFifteen);

  output [3:0]   value;
  output         fifteen,
                 altFifteen;
  input          clock;

  dEdgeFF a (value[0], clock, ~value[0]),
          b (value[1], clock, value[1] ^ value[0]),
          c (value[2], clock, value[2] ^ &value[1:0]),
          d (value[3], clock, value[3] ^ &value[2:0]);

  assign fifteen = value[0] & value[1] & value[2] & value[3];
  assign altFifteen = &value;

endmodule

