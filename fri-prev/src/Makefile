#
# START GOOGLE CHROME WITH --allow-file-access-from-files
#


%.frames.xml	: %.prev
	java -classpath ../prg/bin compiler.Main --xsldir=../prg/xsl --phase=frames --loggedphases=frames $<

%.synan.xml	: %.prev
	java -classpath ../prg/bin compiler.Main --xsldir=../prg/xsl --phase=synan --loggedphases=synan $<

%.lexan.xml	: %.prev
	java -classpath ../prg/bin compiler.Main --xsldir=../prg/xsl --phase=lexan --loggedphases=lexan $<

clean		:
	'rm' -fr *.xml
