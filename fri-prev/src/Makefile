#
# START GOOGLE CHROME WITH --allow-file-access-from-files
#

%.lexan.xml	: %.prev
	java -classpath ../prg/bin compiler.Main --xsldir=../prg/xsl --phase=lexan --loggedphases=lexan $<

clean		:
	'rm' -fr *.xml
