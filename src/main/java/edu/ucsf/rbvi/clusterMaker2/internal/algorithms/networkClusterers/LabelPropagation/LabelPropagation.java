package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.LabelPropagation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.jobs.CyJobData;
import org.cytoscape.jobs.CyJobDataService;
import org.cytoscape.jobs.CyJobExecutionService;
import org.cytoscape.jobs.CyJobManager;
import org.cytoscape.jobs.CyJobStatus;
import org.cytoscape.jobs.SUIDUtil;
import org.cytoscape.jobs.CyJobStatus.Status;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.LabelPropagation.LabelPropagationContext;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.NewNetworkView;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils.ClusterJob;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils.ClusterJobExecutionService;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils.ClusterJobHandler;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils.NetworkClusterJobHandler;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils.RemoteServer;

public class LabelPropagation extends AbstractNetworkClusterer {

	public static String NAME = "Label Propagation (remote)";
	public static String SHORTNAME = "labelpropagation";
	final CyServiceRegistrar registrar;
	public final static String GROUP_ATTRIBUTE = "__LabelPropagationGroups.SUID";
	
	@ContainsTunables
	public LabelPropagationContext context = null;
	
	public LabelPropagation(LabelPropagationContext context, ClusterManager manager, CyServiceRegistrar registrar) {
		super(manager);
		this.context = context;
		if (network == null)
			network = clusterManager.getNetwork();
		context.setNetwork(network);
		this.registrar = registrar;
	}

	@Override
	public String getShortName() {return SHORTNAME;}

	@Override
  @ProvidesTitle
	public String getName() {return NAME;}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		monitor = taskMonitor;
		// Get the execution service
		CyJobExecutionService executionService = 
						registrar.getService(CyJobExecutionService.class, "(title=ClusterJobExecutor)");
		CyApplicationManager appManager = registrar.getService(CyApplicationManager.class);
		CyNetwork currentNetwork = appManager.getCurrentNetwork(); //gets the network presented in Cytoscape
		
		clusterAttributeName = context.getClusterAttribute();
		createGroups = context.advancedAttributes.createGroups;
     	String attribute = context.getattribute().getSelectedValue();
     	
		HashMap<String, Object> configuration = new HashMap<>();
		if (context.isSynchronous == true) {
			configuration.put("waitTime", -1);
		} else {
			configuration.put("waitTime", 20);
		}
				
		HashMap<Long, String> nodeMap = getNetworkNodes(currentNetwork);
		List<String> nodeArray = new ArrayList<>();
		for (Long nodeSUID : nodeMap.keySet()) {
			nodeArray.add(nodeMap.get(nodeSUID));
		}
				
		List<String[]> edgeArray = getNetworkEdges(currentNetwork, nodeMap, attribute);
				
		String basePath = RemoteServer.getBasePath();
				
				// Get our initial job
		ClusterJob job = (ClusterJob) executionService.createCyJob("ClusterJob"); //creates a new ClusterJob object
				// Get the data service
		CyJobDataService dataService = job.getJobDataService(); //gets the dataService of the execution service
				// Add our data
		CyJobData jobData = dataService.addData(null, "nodes", nodeArray);
		jobData = dataService.addData(jobData, "edges", edgeArray);
		job.storeClusterData(clusterAttributeName, currentNetwork, clusterManager, createGroups, GROUP_ATTRIBUTE, null, getShortName());
				// Create our handler
		NetworkClusterJobHandler jobHandler = new NetworkClusterJobHandler(job, network, context.vizProperties.showUI, context.vizProperties.restoreEdges);
		job.setJobMonitor(jobHandler);	
				// Submit the job
		CyJobStatus exStatus = executionService.executeJob(job, basePath, configuration, jobData);
		
		CyJobStatus.Status status = exStatus.getStatus();
		System.out.println("Status: " + status);
		if (status == Status.FINISHED) {
			CyJobData data = dataService.getDataInstance();
			executionService.fetchResults(job, data); 
			
			Map<String, Object> clusterData = job.getClusterData().getAllValues();
			
			String clusterAttributeName = (String) clusterData.get("clusterAttributeName");
			Boolean createGroups = (Boolean) clusterData.get("createGroups");
			String group_attr = (String) clusterData.get("group_attr");
			List<String> params  = (List<String>) clusterData.get("params");

			List<NodeCluster> nodeClusters = ClusterJobExecutionService.createClusters(data, clusterAttributeName, network); //move this to remote utils
			System.out.println("NodeClusters: " + nodeClusters);
	
			AbstractNetworkClusterer.createGroups(network, nodeClusters, group_attr, clusterAttributeName, 
					clusterManager, createGroups, params, SHORTNAME); 
			
			if (context.vizProperties.showUI) {
				taskMonitor.showMessage(TaskMonitor.Level.INFO, "Creating network");
				insertTasksAfterCurrentTask(new NewNetworkView(network, clusterManager, true, context.vizProperties.restoreEdges, false));
			}
			
		} else if (status == Status.RUNNING 
				|| status == Status.SUBMITTED 
				|| status == Status.QUEUED) {
			CyJobManager manager = registrar.getService(CyJobManager.class);
			manager.addJob(job, jobHandler, 5); //this one shows the load button
			
		} else if (status == Status.ERROR) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Job error: " + exStatus.getMessage());
    } else if (status == Status.UNKNOWN  
				|| status == Status.CANCELED 
				|| status == Status.FAILED
				|| status == Status.TERMINATED 
				|| status == Status.PURGED) {
			monitor.setStatusMessage("Job status: " + status);
		}
		
				// Save our SUIDs in case we get saved and restored
		SUIDUtil.saveSUIDs(job, currentNetwork, currentNetwork.getNodeList());

	}	
}
