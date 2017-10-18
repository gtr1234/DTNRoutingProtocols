import matplotlib.pyplot as plt
import numpy as np
import os
import re

xAxis = np.array(range(1,95))

terms = ["delivery_prob", "overhead_ratio", "End-to-End delay"]

fileList = [name for name in os.listdir(".") if name.endswith(".txt")]

listFiles = []

for fname in fileList:
	if re.search(r"\d+\.txt$",fname):
		listFiles.append(fname)

fileList = listFiles

legend = []
for fname in fileList:
	legend.append(re.search(r'\d+',fname).group())

legend = [int(x) for x in legend]

fileList.sort(key = lambda x : int(re.search(r'\d+',x).group()))
legend.sort()



fileList = fileList[0:4]
legend = legend[0:4]

legend.append("epidemic")
legend.append("prophet")
extraFiles = ["Epidemic_MessageStatsReport.txt","Prophet_MessageStatsReport.txt"]

for term in terms:
	for fname in fileList:
		with open(fname,"r") as f:
			yAxis = []

			for line in f:
				values = line.split(":")
				if(values[0] == term):
					yAxis.append(float(values[1].strip()))
		plt.plot(xAxis,yAxis)

	for fname in extraFiles:
		with open(fname,"r") as f:
			yAxis = []

			for line in f:
				values = line.split(':')
				if(values[0] == term):
					yAxis.append(float(values[1].strip()))

		plt.plot(xAxis,yAxis)


	plt.ylabel(term)
	plt.xlabel('time in hours')
	plt.legend(legend, loc='lower right')
	outputFile = term + ".png"
	plt.savefig(outputFile)
	plt.gcf().clear()
