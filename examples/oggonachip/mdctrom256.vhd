LIBRARY ieee;
use IEEE.std_logic_1164.all;

package mdctrom256 is

--n = 256
constant rom_lenght_br: integer:=64;
constant rom_lenght: integer:=320;
type rom_table is array (0 to rom_lenght-1) of std_logic_vector (31 downto 0);
type rom_bitrev is array (0 to rom_lenght_br-1) of std_logic_vector (31 downto 0);

constant bitrev: rom_bitrev:= rom_bitrev'(
--bitrev:
X"0000007e",
X"00000000",
X"0000003e",
X"00000040",
X"0000005e",
X"00000020",
X"0000001e",
X"00000060",
X"0000006e",
X"00000010",
X"0000002e",
X"00000050",
X"0000004e",
X"00000030",
X"0000000e",
X"00000070",
X"00000076",
X"00000008",
X"00000036",
X"00000048",
X"00000056",
X"00000028",
X"00000016",
X"00000068",
X"00000066",
X"00000018",
X"00000026",
X"00000058",
X"00000046",
X"00000038",
X"00000006",
X"00000078",
X"0000007a",
X"00000004",
X"0000003a",
X"00000044",
X"0000005a",
X"00000024",
X"0000001a",
X"00000064",
X"0000006a",
X"00000014",
X"0000002a",
X"00000054",
X"0000004a",
X"00000034",
X"0000000a",
X"00000074",
X"00000072",
X"0000000c",
X"00000032",
X"0000004c",
X"00000052",
X"0000002c",
X"00000012",
X"0000006c",
X"00000062",
X"0000001c",
X"00000022",
X"0000005c",
X"00000042",
X"0000003c",
X"00000002",
X"0000007c"
);
--T:
constant T: rom_table:= rom_table'(
X"00004000",
X"00000000",
X"00003fec",
X"fffffcdc",
X"00003fb1",
X"fffff9ba",
X"00003f4f",
X"fffff69c",
X"00003ec5",
X"fffff384",
X"00003e15",
X"fffff073",
X"00003d3f",
X"ffffed6c",
X"00003c42",
X"ffffea70",
X"00003b21",
X"ffffe782",
X"000039db",
X"ffffe4a3",
X"00003871",
X"ffffe1d5",
X"000036e5",
X"ffffdf19",
X"00003537",
X"ffffdc72",
X"00003368",
X"ffffd9e0",
X"00003179",
X"ffffd766",
X"00002f6c",
X"ffffd505",
X"00002d41",
X"ffffd2bf",
X"00002afb",
X"ffffd094",
X"0000289a",
X"ffffce87",
X"00002620",
X"ffffcc98",
X"0000238e",
X"ffffcac9",
X"000020e7",
X"ffffc91b",
X"00001e2b",
X"ffffc78f",
X"00001b5d",
X"ffffc625",
X"0000187e",
X"ffffc4df",
X"00001590",
X"ffffc3be",
X"00001294",
X"ffffc2c1",
X"00000f8d",
X"ffffc1eb",
X"00000c7c",
X"ffffc13b",
X"00000964",
X"ffffc0b1",
X"00000646",
X"ffffc04f",
X"00000324",
X"ffffc014",
X"00000000",
X"ffffc000",
X"fffffcdd",
X"ffffc014",
X"fffff9bb",
X"ffffc04f",
X"fffff69d",
X"ffffc0b1",
X"fffff385",
X"ffffc13b",
X"fffff074",
X"ffffc1eb",
X"ffffed6d",
X"ffffc2c1",
X"ffffea71",
X"ffffc3be",
X"ffffe783",
X"ffffc4df",
X"ffffe4a4",
X"ffffc625",
X"ffffe1d6",
X"ffffc78f",
X"ffffdf1a",
X"ffffc91b",
X"ffffdc73",
X"ffffcac9",
X"ffffd9e1",
X"ffffcc98",
X"ffffd767",
X"ffffce87",
X"ffffd506",
X"ffffd094",
X"ffffd2c0",
X"ffffd2bf",
X"ffffd095",
X"ffffd505",
X"ffffce88",
X"ffffd766",
X"ffffcc99",
X"ffffd9e0",
X"ffffcaca",
X"ffffdc72",
X"ffffc91c",
X"ffffdf19",
X"ffffc790",
X"ffffe1d5",
X"ffffc626",
X"ffffe4a3",
X"ffffc4e0",
X"ffffe782",
X"ffffc3bf",
X"ffffea70",
X"ffffc2c2",
X"ffffed6c",
X"ffffc1ec",
X"fffff073",
X"ffffc13c",
X"fffff384",
X"ffffc0b2",
X"fffff69c",
X"ffffc050",
X"fffff9ba",
X"ffffc015",
X"fffffcdc",
X"00003ffe",
X"00000064",
X"00003ffc",
X"0000012d",
X"00003ff7",
X"000001f6",
X"00003fef",
X"000002bf",
X"00003fe6",
X"00000388",
X"00003fda",
X"00000450",
X"00003fcb",
X"00000519",
X"00003fb9",
X"000005e1",
X"00003fa6",
X"000006aa",
X"00003f90",
X"00000772",
X"00003f77",
X"00000839",
X"00003f5c",
X"00000900",
X"00003f3f",
X"000009c7",
X"00003f1f",
X"00000a8d",
X"00003efc",
X"00000b53",
X"00003ed7",
X"00000c19",
X"00003eb0",
X"00000cde",
X"00003e86",
X"00000da3",
X"00003e5a",
X"00000e67",
X"00003e2c",
X"00000f2b",
X"00003dfb",
X"00000fee",
X"00003dc8",
X"000010b0",
X"00003d93",
X"00001172",
X"00003d5b",
X"00001233",
X"00003d20",
X"000012f3",
X"00003ce3",
X"000013b3",
X"00003ca4",
X"00001472",
X"00003c62",
X"00001530",
X"00003c1f",
X"000015ee",
X"00003bd9",
X"000016aa",
X"00003b90",
X"00001766",
X"00003b46",
X"00001820",
X"00003af9",
X"000018da",
X"00003aa9",
X"00001992",
X"00003a58",
X"00001a4a",
X"00003a04",
X"00001b01",
X"000039af",
X"00001bb7",
X"00003957",
X"00001c6b",
X"000038fc",
X"00001d1f",
X"0000389f",
X"00001dd1",
X"00003840",
X"00001e83",
X"000037e0",
X"00001f33",
X"0000377d",
X"00001fe2",
X"00003717",
X"00002090",
X"000036b0",
X"0000213c",
X"00003646",
X"000021e7",
X"000035db",
X"00002291",
X"0000356d",
X"00002339",
X"000034fe",
X"000023e1",
X"0000348c",
X"00002487",
X"00003418",
X"0000252b",
X"000033a2",
X"000025ce",
X"0000332b",
X"00002670",
X"000032b1",
X"00002710",
X"00003235",
X"000027ae",
X"000031b7",
X"0000284b",
X"00003138",
X"000028e7",
X"000030b6",
X"00002981",
X"00003033",
X"00002a19",
X"00002fae",
X"00002aaf",
X"00002f27",
X"00002b44",
X"00002e9e",
X"00002bd7",
X"00002e13",
X"00002c69",
X"00002d87",
X"00002cf9",
X"00001ffd",
X"ffffff37",
X"00001fea",
X"fffffda6",
X"00001fc2",
X"fffffc15",
X"00001f87",
X"fffffa88",
X"00001f39",
X"fffff8fd",
X"00001ed7",
X"fffff777",
X"00001e62",
X"fffff5f7",
X"00001ddb",
X"fffff47c",
X"00001d41",
X"fffff309",
X"00001c95",
X"fffff19d",
X"00001bd8",
X"fffff03a",
X"00001b09",
X"ffffeee2",
X"00001a29",
X"ffffed93",
X"0000193a",
X"ffffec50",
X"0000183b",
X"ffffeb19",
X"0000172d",
X"ffffe9f0",
X"00001610",
X"ffffe8d3",
X"000014e7",
X"ffffe7c5",
X"000013b0",
X"ffffe6c6",
X"0000126d",
X"ffffe5d7",
X"0000111e",
X"ffffe4f7",
X"00000fc6",
X"ffffe428",
X"00000e63",
X"ffffe36b",
X"00000cf7",
X"ffffe2bf",
X"00000b84",
X"ffffe225",
X"00000a09",
X"ffffe19e",
X"00000889",
X"ffffe129",
X"00000703",
X"ffffe0c7",
X"00000578",
X"ffffe079",
X"000003eb",
X"ffffe03e",
X"0000025a",
X"ffffe016",
X"000000c9",
X"ffffe003"
);
end;

