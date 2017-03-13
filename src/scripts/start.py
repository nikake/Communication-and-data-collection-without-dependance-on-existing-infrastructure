"""
	This script will run when starting the Raspberry Pi. It will change the operating mode and
	SSID and raise an ad-hoc interface. It will then scan for nearby cells that are in range and
	assign itself an unique IP-address.

	When the launch process is completed the Java application will be launched and run until the
	system is shutdown.

	Source: https://wiki.debian.org/WiFi/AdHoc#Manual_Method
"""

import os
import subprocess

ipAddress = "192.168.1.1"
applicationPath = ""
outputPath = os.getcwd()+"/./"
outputChannel = open(os.devnull, 'w')

""" apply super user access"""
subprocess.call("sudo su", shell=True, stdout=outputChannel, stderr=subprocess.STDOUT)

""" On each node, bring the wireless interface down, change the device's operating mode and SSID, then raise the interface."""
subprocess.call("ifconfig wlan0 down", shell=True, stdout=outputChannel, stderr=subprocess.STDOUT)
subprocess.call("iwconfig wlan0 channel 1 essid MYNETWORK mode ad-hoc", shell=True, stdout=outputChannel, stderr=subprocess.STDOUT)
subprocess.call("ifconfig wlan0 up", shell=True, stdout=outputChannel, stderr=subprocess.STDOUT)

""" Scan for ad-hoc cells in range (necessary for some drivers to trigger IBSS scanning) """
subprocess.call("iwlist wlan0 scan", shell=True, stdout=outputChannel, stderr=subprocess.STDOUT)

""" Assign Unique IP to the node """
subprocess.call("ifconfig wlan0 " + ipAddress + " netmask 255.255.255.0", shell=True, stdout=outputChannel, stderr=subprocess.STDOUT)

""" Start Java application """
""" TODO """