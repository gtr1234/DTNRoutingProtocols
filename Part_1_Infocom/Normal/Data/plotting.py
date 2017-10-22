import matplotlib.pyplot as plt
import numpy as np
import os
import re

xAxis = np.array(range(1,95))

terms = ["delivery_prob", "overhead_ratio", "End-to-End delay"]

fileList = []
legend = ["ContactsRouter","epidemic","Prophet"]
extraFiles = ["MyRouter_MessageStatsReport.txt","EpidemicRouter_MessageStatsReport.txt","ProphetRouter_MessageStatsReport.txt"]

for term in terms:
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
