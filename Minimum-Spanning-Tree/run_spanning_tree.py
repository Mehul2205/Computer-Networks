import sys
from Topology import *

if len(sys.argv) != 3:
	print("Syntax:")
	print("    python run_spanning_tree.py <topology_file> <log_file>")
	exit()

# Populate the topology
topo = Topology(sys.argv[1])

# Run the topology.
topo.run_spanning_tree()

# Close the logfile
topo.log_spanning_tree(sys.argv[2])
