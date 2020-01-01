from Message import *
from StpSwitch import *

class Switch(StpSwitch):

	def __init__(self, idNum, topolink, neighbors):    
		# Invoke the super class constructor, which makes available to this object the following members:
		# -self.switchID                   (the ID number of this switch object)
		# -self.links                      (the list of swtich IDs connected to this switch object)
		super(Switch, self).__init__(idNum, topolink, neighbors)
		
		#TODO: Define a data structure to keep track of which links are part of / not part of the spanning tree.
		self.root = self.switchID
		self.distance = 0
		self.activeLinks = []
		self.throughSwitch = self.switchID

	def send_initial_messages(self):
		#TODO: This function needs to create and send the initial messages from this switch.
		#Messages are sent via the superclass method send_message(Message msg) - see Message.py.
		#Use self.send_message(msg) to send this.  DO NOT use self.topology.send_message(msg)
		for s in self.links:
			# create a message for every switch in the links list. i.e. neighbors
			# signature: claimedRoot, distanceToRoot, originID, destinationID, pathThrough
			m = Message(self.root, self.distance, self.switchID, s, False)
			self.send_message(m)
		return
	
	def add_active_link(self, switchID):
		if switchID not in self.activeLinks:
			self.activeLinks.append(switchID)
		return

	def remove_active_link(self, switchID):
		if switchID in self.activeLinks:
			del self.activeLinks[self.activeLinks.index(switchID)]
		return

	def notify_neighbors(self):
		for s in self.links:
			if (s != self.throughSwitch):
				m = Message(self.root, self.distance, self.switchID, s, False)
			else:
				m = Message(self.root, self.distance, self.switchID, s, True)
			self.send_message(m)
		return

	def process_message(self, message):
		#TODO: This function needs to accept an incoming message and process it accordingly.
		#      This function is called every time the switch receives a new message.
		if (message.root > self.root):
			return
		if (message.root == self.root):
			if (message.distance + 1 < self.distance):
				self.distance = message.distance + 1
				self.throughSwitch = message.origin
				self.add_active_link(message.origin)
				self.notify_neighbors()

			if (message.distance + 1 == self.distance):
				if (message.origin < self.throughSwitch):
					self.remove_active_link(self.throughSwitch)
					self.throughSwitch = message.origin
					self.add_active_link(message.origin)
				elif (message.origin > self.throughSwitch):
					self.remove_active_link(message.origin)
				self.notify_neighbors()

			if (message.distance + 1 > self.distance):
				if (message.pathThrough == True):
					self.add_active_link(message.origin)
				else:
					self.remove_active_link(message.origin)
			return

		if (message.root < self.root):
			self.root = message.root
			self.distance = message.distance + 1
			self.throughSwitch = message.origin
			self.add_active_link(message.origin)
			self.notify_neighbors()
		return
		
	def generate_logstring(self):
		#TODO: This function needs to return a logstring for this particular switch.  The
		#      string represents the active forwarding links for this switch and is invoked 
		#      only after the simulaton is complete.  Output the links included in the 
		#      spanning tree by increasing destination switch ID on a single line. 
		#      Print links as '(source switch id) - (destination switch id)', separating links 
		#      with a comma - ','.  
		#
		#      For example, given a spanning tree (1 ----- 2 ----- 3), a correct output string 
		#      for switch 2 would have the following text:
		#      2 - 1, 2 - 3
		#      A full example of a valid output file is included (sample_output.txt) with the project skeleton.
		logstring = ''
		self.activeLinks.sort()
		for l in self.activeLinks:
			logstring += str(self.switchID) + ' - ' + str(l) + ', '
		logstring = logstring[:-2]
		return logstring
