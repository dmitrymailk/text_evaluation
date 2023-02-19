#!/usr/bin/env python

__version__ = "1.0"
__author__ = "Michael Denkowski"
__contact__ = "mdenkows@cs.cmu.edu"

# Walk directories to find hypothesis, reference, and hter files, and
# reorganize them into a modular test set

import os, re, sys

def main(argv):

	if len(argv) < 4:
		print "Create a Correlyzer test set from HTER data"
		print "usage:", argv[0], "<sys name> <hter dir> <ref dir>"
		sys.exit(0)

	sysName = argv[1]
	hterDir = os.path.normpath(argv[2])
	refDir = os.path.normpath(argv[3])

	if os.path.exists(sysName):
		print "File", sysName, "exists, aborting to avoid overwriting files"
		sys.exit(1)
	
	os.mkdir(sysName)

	# Walk HTER directories to find segments and TER scores
	
	outSeg = open(os.path.join(sysName, "tst.sgm"), "w")
	outSeg.write("<tstset trglang=\"en\" setid=\"dev\" srclang=\"any\">\n")
	outTerSeg = open(os.path.join(sysName, "ter.seg"), "w")
	outTerDoc = open(os.path.join(sysName, "ter.doc"), "w")

	for w in os.walk(hterDir):
		if len(w[2]) > 0:
			for f in w[2]:
				# Find DOCNAME.hyp.orig_ter files
				if f.endswith(".orig_ter"):
					hyp = open(os.path.join(w[0], f), "r")
					doc = ""
					lines = []
					for line in hyp:
						r = re.match("^\\s*(.+)\\s+\\(id=(.+)\\)\\s*", line)
						text = r.group(1)
						part = r.group(2).rpartition("_")
						doc = part[0]
						seg = part[2]
						lines.append("<seg id=\"" + seg + "\"> " + text + \
								" </seg>\n")
					hyp.close()
					outSeg.write("<doc docid=\"" + doc + "\" sysid=\"" + \
							sysName + "\">\n")
					outSeg.writelines(lines)
					outSeg.write("</doc>\n")
					
					# Also read corresponding DOCNAME.hyp.hter.sum files
					terfile = f[0:-9] + ".hter.sum"
					ter = open(os.path.join(w[0], terfile), "r")
					curDoc = ""
					for line in ter:
						r = re.match("^\\s*id=(.+)_([0-9]+):[0-9]+\\s+.+\\|" + \
								"\\s+([0-9\\.]+)\\s+\\|\\s+([0-9\\.]+)\\s*$", \
								line)
						if r:
							doc = r.group(1)
							seg = r.group(2)
							length = r.group(3) # not used here
							score = r.group(4)
							outTerSeg.write(doc + " " + seg + " " + score + \
									"\n")
							curDoc = doc
							continue
						r = re.match("^\\s*TOTAL\\s+.+\\|\\s+([0-9\\.]+)" + \
								"\\s+\\|\\s+([0-9\\.]+)\\s*$", line)
						if r:
							length = r.group(1) # also not used
							score = r.group(2)
							outTerDoc.write(curDoc + " " + score + "\n")
					ter.close()
	outSeg.write("</tstset>\n")
	outSeg.close()
	outTerSeg.close()
	outTerDoc.close()
	
	# Combine reference files
	
	outRef = open(os.path.join(sysName, "ref.sgm"), "w")
	outRef.write("<refset trglang=\"en\" setid=\"dev\" srclang=\"any\">\n")
	for w in os.walk(refDir):
		if len(w[2]) > 0:
			for f in w[2]:
				if f.endswith(".sgm"):
					ref = open(os.path.join(w[0], f), "r")
					for line in ref:
						outRef.write(line)
					ref.close()
	outRef.write("</refset>\n")
	outRef.close()

if __name__ == "__main__" : main(sys.argv)