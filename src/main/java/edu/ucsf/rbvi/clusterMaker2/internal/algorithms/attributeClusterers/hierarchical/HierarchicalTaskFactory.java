package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hierarchical;

import java.util.Collections;
import java.util.List;

//Cytoscape imports
import org.cytoscape.work.TaskIterator;


import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class HierarchicalTaskFactory implements ClusterTaskFactory   {
	ClusterManager clusterManager;
	HierarchicalContext context = null;
	
	public HierarchicalTaskFactory(ClusterManager clusterManager) {
		context = new HierarchicalContext();
		this.clusterManager = clusterManager;
	}
	
	public String getShortName() {return HierarchicalCluster.SHORTNAME;};
	public String getName() {return HierarchicalCluster.NAME;};

	public ClusterViz getVisualizer() {
		// return new NewNetworkView(true);
		return null;
	}

	public boolean isReady() {
		return true;
	}

	public boolean isAvailable() {
		return false;
	}

	public List<ClusterType> getTypeList() {
		return Collections.singletonList(ClusterType.ATTRIBUTE); 
	}

	public TaskIterator createTaskIterator() {
		// Not sure why we need to do this, but it looks like
		// the tunable stuff "remembers" objects that it's already
		// processed this tunable.  So, we use a copy constructor
		return new TaskIterator(new HierarchicalCluster(context, clusterManager));
	}
	
}
	
	


