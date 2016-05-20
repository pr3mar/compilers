#
# START GOOGLE CHROME WITH google-chrome --allow-file-access-from-files
#


%.codegen.xml	: %.prev
	java -classpath ../prg/bin compiler.Main --xsldir=../prg/xsl --phase=codegen --loggedphases=codegen $<

%.lincode.xml	: %.prev
	java -classpath ../prg/bin compiler.Main --xsldir=../prg/xsl --phase=lincode --loggedphases=lincode $<

%.imcode.xml	: %.prev
	java -classpath ../prg/bin compiler.Main --xsldir=../prg/xsl --phase=imcode --loggedphases=imcode $<

%.frames.xml	: %.prev
	java -classpath ../prg/bin compiler.Main --xsldir=../prg/xsl --phase=frames --loggedphases=frames $<

%.seman.xml	: %.prev
	java -classpath ../prg/bin compiler.Main --xsldir=../prg/xsl --phase=seman --loggedphases=seman $<

%.abstr.xml	: %.prev
	java -classpath ../prg/bin compiler.Main --xsldir=../prg/xsl --phase=abstr --loggedphases=abstr $<

%.synan.xml	: %.prev
	java -classpath ../prg/bin compiler.Main --xsldir=../prg/xsl --phase=synan --loggedphases=synan $<

%.lexan.xml	: %.prev
	java -classpath ../prg/bin compiler.Main --xsldir=../prg/xsl --phase=lexan --loggedphases=lexan $<

clean		:
	'rm' -fr *.xml
