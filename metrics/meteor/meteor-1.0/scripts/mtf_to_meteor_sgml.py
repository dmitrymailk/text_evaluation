#!/usr/bin/env python

import re, sys

def main(argv) :
	
	if len(argv) < 2 :
		print "Turns a mtf reference into a sgml reference for METEOR by"
		print "enumerating all possible translations (might not work with"
		print "other metrics)"
		print ""
		print "usage:", argv[0], "mtfFile [ > outFile ]"
		sys.exit()
	
	mtfFile = argv[1]
	mtf = open(mtfFile, "r")
	
	seg = {} # seg[docID][segID] = list of translations
	translator = "" # reference base name
	pad = 0 # total "references"
	docID = ""
	segID = ""
	
	while True :
		line = mtf.readline()
		if not line : break
		r = re.search("<docs\\s+translator=\"(.+?)\".*>", line, re.I)
		if r:
			translator = r.group(1)
			continue
		r = re.search("<doc\\s+id=\"(.+)\">", line, re.I)
		if r:
			docID = r.group(1)
			seg[docID] = {}
			continue
		r = re.search("<seg\\s+id=\"(.+?)\".*>", line, re.I)
		if r:
			segID = int(r.group(1))
			seg[docID][segID] = []
			alt = {} # alt[altID] = list of alternates
			altTotal = 1
			# Collect altIDs
			while True:
				line = mtf.readline()
				r = re.search("<alts\\s+for=\"(.+)\">", line, re.I)
				if not r : break # If not r, line = "<text>"
				altID = r.group(1)
				alt[altID] = []
				# Collect alternates for this altID
				while True:
					line = mtf.readline()
					r = re.search("<alt\\s+translator=\".+\">(.+)</alt>", \
							line, re.I)
					if not r : break # If not r, line = "</alts>"
					alt[altID].append(r.group(1))
				altTotal *= len(alt[altID]) + 1 # plus one for default text
			pad = max(pad, altTotal)
			# Extract text
			text = mtf.readline().rstrip("\n")
			mtf.readline() # Throw away </text>
			mtf.readline() # Throw away </seg>
			# Enumerate all possible translations
			__enumerate(text, sorted(alt.keys()), alt, seg[docID][segID], 0)
				
	# Ignore </doc>, </docs>
	
	# Output SGML
	for docID in sorted(seg.keys()):
		for i in range(0, pad):
			print "<doc docid=\"" + docID + "\" sysid=\"" + translator + \
					str(i) + "\">"
			for segID in sorted(seg[docID].keys()):
				text = ""
				if i < len(seg[docID][segID]):
					text = seg[docID][segID][i]
				print "<seg id=\"" + str(segID) + "\"> " + text + " </seg>"
			print "</doc>"

# Enumerate alternate translations and add them to a list
def __enumerate(text, altKeys, alt, segList, index):
	# If all substitutions have been made, add segment
	if index == len(alt):
		segList.append(text)
		return
	# Default text
	altText = re.sub("<alt_target\\s+id=\"" + altKeys[index] + \
				"\">(.+?)</alt_target>", "\\1", text)
	__enumerate(altText, altKeys, alt, segList, index + 1)
	# Each possible alternate
	for i in range(0, len(alt[altKeys[index]])):
		altText = re.sub("<alt_target\\s+id=\"" + altKeys[index] + \
				"\">(.+?)</alt_target>", alt[altKeys[index]][i], text)
		__enumerate(altText, altKeys, alt, segList, index + 1)

if __name__ == "__main__" : main(sys.argv)