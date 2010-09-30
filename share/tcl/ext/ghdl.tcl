#
# ZamiaCAD - GHDL Bindings
#

set msgPattern {([^:]*):([0-9]+):([0-9]+):(.*)$}

proc zamiaBuild {filename} {
   global msgPattern projectBasePath

   puts "GHDL: filename is $filename, project base path is $projectBasePath"

   cd $projectBasePath
   catch {exec make} out

   set lines [split $out "\n"]

   foreach ol $lines {
      puts "GHDL: $ol"
     
      if [regexp $msgPattern $ol dummy sfname sfline sfcolumn msg] {
         #puts "zamiaBuild(): Got error message: $msg"

         #puts "Generating marker: $sfname:$sfline,$sfcolumn $msg"
         
         if [regexp {.*warning:.*$} $ol] {
            zamiaAddMarker $sfname $sfline $sfcolumn "ghdl: $msg" "0"
         } else {
            zamiaAddMarker $sfname $sfline $sfcolumn "ghdl: $msg" "1"
         }
      }
   }
}

