#
# ZamiaCAD - cmtvc Bindings
#
#

set locationPattern {[0-9]+ COACHDL.*\]: File: ([^,]*), line\.column: ([0-9]*)\.([0-9]*)}
set msgPattern {([0-9]+) COACHDL.*\]: (.*)}

proc zamiaBuild {filename} {
   global locationPattern msgPattern

   puts "CMTVC: compiling $filename"

   catch {exec env CM_COMPILE="YES" .cm_init "cmtvc -noll -src $filename"} out

   set lines [split $out "\n"]

   foreach ol $lines {
      puts "CMTVC: $ol"
      
      if [regexp $locationPattern $ol dummy sfname sfline sfcolumn ] {
         #puts "zamiaBuildCompile(): Got source location:"
         #puts "zamiaBuildCompile(): filename: $sfname"
         #puts "zamiaBuildCompile(): line    : $sfline"
         #puts "zamiaBuildCompile(): column  : $sfcolumn"
      }

      if [regexp $msgPattern $ol dummy num msg] {
         #puts "zamiaBuildCompile(): Got error message: $msg"

         if { $num > 19 } {
            if [info exists sfname] {
               #puts "Generating marker: $sfname:$sfline,$sfcolumn $msg"
               zamiaAddMarker $sfname $sfline $sfcolumn "cmtvc: $msg" "1"
               unset sfname
            }
         }
      }
   }
}

