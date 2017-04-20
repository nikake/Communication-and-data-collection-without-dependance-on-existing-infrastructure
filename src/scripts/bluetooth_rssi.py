import fcntl
import struct
import array
import bluetooth
import bluetooth._bluetooth as bt
import RPi.GPIO as GPIO
import os
import sys

""" Read RSSI value from hwaddr given as input to the file """

def read_rssi():
	hwaddr = sys.argv[1]
	# Open hci socket
	hci_sock = bt.hci_open_dev()
	hci_fd = hci_sock.fileno()
	
	# Connect to device (to whatever you like)
	bt_sock = bluetooth.BluetoothSocket(bluetooth.L2CAP)
	bt_sock.settimeout(10)
	result = bt_sock.connect_ex((hwaddr, 1))	# PSM 1 - Service Discovery
	
	try:
		# Get ConnInfo
		reqstr = struct.pack("6sB17s", bt.str2ba(hwaddr), bt.ACL_LINK, "\0" * 17)
		request = array.array("c", reqstr )
		handle = fcntl.ioctl(hci_fd, bt.HCIGETCONNINFO, request, 1)
		handle = struct.unpack("8xH14x", request.tostring())[0]
		
		# Get RSSI
		cmd_pkt=struct.pack('H', handle)
		rssi = bt.hci_send_req(hci_sock, bt.OGF_STATUS_PARAM, bt.OCF_READ_RSSI, bt.EVT_CMD_COMPLETE, 4, cmd_pkt)
		rssi = struct.unpack('b', rssi[3])[0]
		
		# Close sockets
		bt_sock.close()
		hci_sock.close()
		
		print rssi
		return rssi
		
	except:
		print "device not found"
		return "device not found"
		
read_rssi()