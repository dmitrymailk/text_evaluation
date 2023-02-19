#!/usr/bin/env python

import os, re, sys

def main(argv):

	if len(argv) < 3:
		print "Create resources/paraphrase file using data from a paraphrase " \
				"table"
		print "usage:", argv[0], "<meteorBaseDir> <phrasetable>"
		print "ex:   ", argv[0], ". phrasetable.txt"
		sys.exit()

	meteorDir = argv[1]
	pdbFile = argv[2]
	pdb = open(pdbFile, "r")
	pOut = open(os.path.join(meteorDir, "resources/paraphrase"), "w")

	print "Creating paraphrase file - this may take a few minutes..."
	
	for line in pdb:
		r = re.match("\\s*([0-9\\.e-]+)\\s*<p>(.+)</p>\\s*<p>(.+)</p>\\s*", \
				line, re.I)
		prob = r.group(1)
		ref = r.group(2)
		para = r.group(3)
		if len(ref.split()) == 1 and len(para.split()) == 1:
			pOut.write(ref + "\n")
			pOut.write(para + "\n")
			pOut.write(prob + "\n")
	
	pdb.close()
	pOut.close()

	print "Paraphrase file created."

if __name__ == "__main__" : main(sys.argv)
