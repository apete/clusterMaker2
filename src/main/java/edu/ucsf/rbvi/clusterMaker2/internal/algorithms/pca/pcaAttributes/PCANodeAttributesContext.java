/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.pcaAttributes;

import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;
import java.util.List;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;

/**
 *
 * @author root
 */
public class PCANodeAttributesContext {
        CyNetwork network;
        
        @Tunable(description = "Only use selected nodes for PCA", groups={"Data Input"}, gravity=7.0)
	public boolean selectedOnly = false;
        
        @Tunable(description="Ignore nodes with no data", groups={"Data Input"}, gravity=8.0)
	public boolean ignoreMissing = true;
        
        
        @Tunable(description="Node attributes for PCA", groups="Source for Distance Matric",
	         tooltip="You must choose at least 2 node columns for an attribute PCA", gravity=9.0 )
	public ListMultipleSelection<String> nodeAttributeList = null;
        
        @Tunable(description = "Create Result Panel with Principal Component selection option", groups={"Result Options"}, gravity=83.0)
	public boolean pcaResultPanel = true;
        
        @Tunable(description = "Create PCA scatter plot with node selection option", groups={"Result Options"}, gravity=84.0)
	public boolean pcaPlot = false;
                
        public PCANodeAttributesContext(){
            
        }
        
        public void setNetwork(CyNetwork network){
            if (this.network != null && this.network.equals(network))
			return;
            
            this.network = network;
            
            if (network != null)
                    nodeAttributeList = ModelUtils.updateNodeAttributeList(network, nodeAttributeList);
        }
        
        public List<String> getNodeAttributeList() {
		if (nodeAttributeList == null) return null;
		List<String> attrs = nodeAttributeList.getSelectedValues();
		if (attrs == null || attrs.isEmpty()) return null;
		if ((attrs.size() == 1) &&
		    (attrs.get(0).equals("--None--"))) return null;
		return attrs;
	}
        
        public CyNetwork getNetwork() { return network; }
        
}
