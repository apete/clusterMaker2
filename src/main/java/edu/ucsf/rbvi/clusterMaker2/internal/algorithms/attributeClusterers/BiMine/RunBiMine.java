package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.BiMine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.ChengChurch.ChengChurchContext;

public class RunBiMine {

	protected CyNetwork network;
	protected String[] weightAttributes;
	//protected DistanceMetric metric;
	protected BET<Integer> bet;
	protected Matrix matrix;
	protected Double arr[][];
	protected int[] clusters;
	protected TaskMonitor monitor;
	protected boolean ignoreMissing = true;
	protected boolean selectedOnly = false;
	BiMineContext context;	
	double delta;	
	
	public RunBiMine(CyNetwork network, String weightAttributes[],
            TaskMonitor monitor, BiMineContext context) {
		//super(network, weightAttributes, metric, monitor);
		this.network = network;
		this.weightAttributes = weightAttributes;
		//this.metric = metric;
		this.monitor = monitor;
		this.context = context;
		this.delta = context.delta.getValue();
	}
	
	public Integer[] cluster(boolean transpose) {
		// Create the matrix
		matrix = new Matrix(network, weightAttributes, transpose, ignoreMissing, selectedOnly);
		monitor.showMessage(TaskMonitor.Level.INFO,"cluster matrix has "+matrix.nRows()+" rows");
		
		// Create a weight vector of all ones (we don't use individual weighting, yet)
		matrix.setUniformWeights();
				
		if (monitor != null) 
			monitor.setStatusMessage("Clustering...");
		
		int nelements = matrix.nRows();
		int nattrs = matrix.nColumns();
		
		arr = preProcess();
		
		bet = Init_BET();
		
		Integer[] rowOrder = null;
		return rowOrder;
	}
	
	private BET<Integer> Init_BET() {
		BETNode<Integer> root = new BETNode<Integer>();
		
		int nelements = matrix.nRows();
		int nattrs = matrix.nColumns();
		
		for(int i = 0; i < nelements; i++){			
			ArrayList<Integer> condList = new ArrayList<Integer>();
			ArrayList<Integer> geneList = new ArrayList<Integer>();
			geneList.add(i);
			double avg_i = getRowAvg(i);
			
			for(int j = 0 ; j < nattrs; j++){
				Double value = matrix.getValue(i, j);
				if(value!=null && avg_i != 0.0){				
					if( (Math.abs(value-avg_i)/avg_i) > delta )condList.add(j);										
				}
			}
			BETNode<Integer> child = new BETNode<Integer>(geneList,condList);
			root.addChild(child);
		}
		
		BET<Integer> newBet = new BET<Integer>(root);
		return newBet;
	}
	
	private void BET_tree(){
		BETNode<Integer> node = bet.getRoot();
		List<BETNode<Integer>> level = node.getChildren();
		List<BETNode<Integer>> leaves = new ArrayList<BETNode<Integer>>();
		while(level.size() > 0){
			int levelSize = level.size();	
			List<BETNode<Integer>> nextLevel = new ArrayList<BETNode<Integer>>();
			for(int i = 0; i < levelSize; i++){				
				BETNode<Integer> node_i = level.get(i);
				
				for(int j = i+1; j < levelSize; j++){
					BETNode<Integer> uncle_j = level.get(j);
					List<Integer> childGenes = union(node_i.getGenes(),uncle_j.getGenes());
					List<Integer> childConditions = intersection(node_i.getConditions(),uncle_j.getConditions());
					
					BETNode<Integer> child_j = new BETNode<Integer>(childGenes,childConditions);
					
					if(getASR(child_j) >= delta){
						node_i.addChild(child_j);					
					}
					else{
					}				
				}
				if(node_i.getChildren().size() > 0){
					nextLevel.addAll(node_i.getChildren());
				}
				else{
					leaves.add(node_i);
				}
			}
			level = nextLevel;			
		}
		getBiClusters(leaves);
	}
	
	private void getBiClusters(List<BETNode<Integer>> leaves) {
		// TODO Auto-generated method stub
		
	}

	private double getASR(BETNode<Integer> node) {
		return 0;
	}

	public List<Integer> union(List<Integer> a, List<Integer> b){
		List<Integer> unionList = new ArrayList<Integer>(a);
		unionList.removeAll(b);
		unionList.addAll(b);
		return unionList;
	}
	
	public List<Integer> intersection(List<Integer> a, List<Integer> b){
		List<Integer> intersectionList = new ArrayList<Integer>(a);
		intersectionList.retainAll(b);		
		return intersectionList;
	}

	public Double[][] preProcess(){
		int nelements = matrix.nRows();
		int nattrs = matrix.nColumns();
		Double matrix_proc[][] = new Double[nelements][nattrs];
		
		for(int i = 0; i < nelements; i++){
			
			double avg_i = getRowAvg(i);
			for(int j = 0 ; j < nattrs; j++){
				Double value = matrix.getValue(i, j);
				if(value==null || avg_i == 0.0)matrix_proc[i][j] = null;
				else{
					if( (Math.abs(value-avg_i)/avg_i) > delta )matrix_proc[i][j] = value;
					else matrix_proc[i][j] = null;					
				}
			}
		}
		
		return matrix_proc;
	}
	
	public double getRowAvg(int row){
		int nattrs = matrix.nColumns();
		double avg = 0.0;
		int count = 0;
		for(int j = 0; j < nattrs; j++){
			Double value = matrix.getValue(row, j);
			if(value!=null){
				avg += value;
				count++;
			}
		}
		
		if(count==0)return avg;
		else return avg/count;
	}
}











