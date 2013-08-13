package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCL;

import java.util.ArrayList;
import java.util.List;

//Cytoscape imports
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TaskMonitor;


import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.DistanceMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;

public class MCLCluster extends AbstractNetworkClusterer   {
	ClusterManager clusterManager;
	RunMCL runMCL;
	public static String SHORTNAME = "mcl";
	public static String NAME = "MCL Cluster";
	
	@ContainsTunables
	public MCLContext context = null;
	
	public MCLCluster(MCLContext context, ClusterManager manager) {
		super();
		this.context = context;
		this.clusterManager = clusterManager;
	}

	public String getShortName() { return SHORTNAME; }
	public String getName() { return NAME; }
	
	public void run(TaskMonitor monitor) {
		this.monitor = monitor;
		if (network == null)
			network = clusterManager.getNetwork();
		
		DistanceMatrix matrix = context.edgeAttributeHandler.getMatrix();
		if (matrix == null) {
			monitor.showMessage(TaskMonitor.Level.ERROR,"Can't get distance matrix: no attribute value?");
			return;
		}

		if (canceled) return;

		//Cluster the nodes
		runMCL = new RunMCL(matrix, context.inflation_parameter, context.iterations, 
		                    context.clusteringThresh, context.maxResidual, context.maxThreads, monitor);

		runMCL.setDebug(debug);

		if (canceled) return;

		monitor.showMessage(TaskMonitor.Level.INFO,"Clustering...");

		// results = runMCL.run(monitor);
		List<NodeCluster> clusters = runMCL.run(network, monitor);
		if (clusters == null) return; // Canceled?

		monitor.showMessage(TaskMonitor.Level.INFO,"Removing groups");

		// Remove any leftover groups from previous runs
		removeGroups(network);

		monitor.showMessage(TaskMonitor.Level.INFO,"Creating groups");

		params = new ArrayList<String>();
		context.edgeAttributeHandler.setParams(params);

		List<List<CyNode>> nodeClusters = createGroups(network, clusters);

		results = new AbstractClusterResults(network, nodeClusters);
		monitor.showMessage(TaskMonitor.Level.INFO, "Done.  MCL results:\n"+results);

	}

	public void cancel() {
		canceled = true;
		runMCL.cancel();
	}
}
	
	



