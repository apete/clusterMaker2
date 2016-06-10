package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa;

import java.util.List;
import java.util.TreeMap;

import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.ComputationMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

public class CalculationMatrix{

	double data[][];
	int rows;
	int columns;
	int diag;//make diagnostic plots 
	int scale;//scale eigenvectors (= scores) by their eigenvalue 
	int neg;//discard (= 0), keep (= 1), or correct (= 2)  negative eigenvalues  
	double eigen_values[];
	double eigenvectors[][];
	double combine_array[][];
	double scores[][];
	
	public CalculationMatrix(int rows,int columns,double inputdata[][],int diag,int scale,int neg){
		this.rows=rows;
		this.columns=columns;
		this.diag=diag;
		this.scale=scale;
		this.neg=neg;
		data=new double[rows][columns];
		for (int row = 0; row < rows; row++) {
			for (int column = 0; column < columns; column++) {
				this.data[row][column] = inputdata[row][column];
			}
		}
	}
	

	public double[][] getScores() {
		return scores;
	}


	public void setScores(double[][] scores) {
		this.scores = scores;
	}


	public int getNeg() {
		return neg;
	}


	public void setNeg(int neg) {
		this.neg = neg;
	}


	public double[][] getCombine_array() {
		return combine_array;
	}


	public void setCombine_array(double[][] combine_array) {
		this.combine_array = combine_array;
	}


	public double[] getEigen_values() {
		return eigen_values;
	}


	public void setEigen_values(double[] eigen_values) {
		this.eigen_values = eigen_values;
	}


	public double[][] getEigenvectors() {
		return eigenvectors;
	}


	public void setEigenvectors(double[][] eigenvectors) {
		this.eigenvectors = eigenvectors;
	}


	public Double getValue(int row, int column) {
		// TODO Auto-generated method stub
		return data[row][column];
	}


	//to check matrix isSymmertical
	public boolean isSymmetrical() {
		for( int row=0; row < data.length; row++ ){
            for( int col=0; col < row; col++ ){
            	
                if( data[row][col] != data[col][row] ){
                    return false;
                }
            }
        }
        return true;
	}

	//reverse matrix
	public static double[] matrixReverse(double[] x) {

	    double[] d = new double[x.length];


	    for (int i = 0; i < x.length; i++) {
	        d[i] = x[x.length - 1 -i];
	    }
	    return d;
	}
	
	//matrix multiplication
	public static double[][] multiplyByMatrix(double[][] m1, double[][] m2) {
        int m1ColLength = m1[0].length; 
        int m2RowLength = m2.length;    
        if(m1ColLength != m2RowLength) return null; 
        int mRRowLength = m1.length;    
        int mRColLength = m2[0].length; 
        double[][] mResult = new double[mRRowLength][mRColLength];
        for(int i = 0; i < mRRowLength; i++) {         
            for(int j = 0; j < mRColLength; j++) {     
                for(int k = 0; k < m1ColLength; k++) { 
                    mResult[i][j] += m1[i][k] * m2[k][j];
                }
            }
        }
        return mResult;
    }
	
	//calculate transpose of a matrix
	public  double[][] transposeMatrix(double matrix[][]){
        double[][] temp = new double[matrix[0].length][matrix.length];
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[0].length; j++)
                temp[j][i] = matrix[i][j];
        return temp;
    }
	
	//calculate Gowern's matrix
	public double[][] getGowernsMatrix(){
		//set ones matrix with row vector
		double ones[][]=new double[rows][rows];
		for(int i=0;i<ones.length;i++){
			for(int j=0;j<ones.length;j++){
				if(j==0){
					ones[i][j]=1;	}
			}}
		
		//create unit matrix with row*row dimension
		double unitmatrix[][]=new double[rows][rows];
		for(int i=0;i<rows;i++){
			for(int j=0;j<rows;j++){
				if(i==j){
					unitmatrix[i][j]=1;
				}else{
					unitmatrix[i][j]=0;
					}}}
	
		//calculate matrixA
		double matrixA[][]=new double[rows][columns]; 
		for(int i=0;i<rows;i++){
			for(int j=0;j<columns;j++){
				matrixA[i][j]=-0.5*Math.pow(data[i][j], 2);
			}}
		
		//calculate matrixG
		double transposeOnes[][]=transposeMatrix(ones);
		
		
		double multimatrix[][]=multiplyByMatrix(ones, transposeOnes);
		double tempmatrix[][]=new double[rows][rows];
		for(int i=0;i<rows;i++){
			for(int j=0;j<rows;j++){
				tempmatrix[i][j]=unitmatrix[i][j]-(multimatrix[i][j])/rows;
			}}
		multimatrix=multiplyByMatrix(tempmatrix, matrixA);
		double matrixG[][]=multiplyByMatrix(multimatrix, tempmatrix);
	return matrixG;
	}
	
	
	public double[][] eigenAnalysis(){
		ComputationMatrix computationMatrix=new ComputationMatrix(getGowernsMatrix());
		double eigenvector[][]=computationMatrix.eigenVectors();
		double eigenvalues[]=computationMatrix.eigenValuesAll();		
		double tolerance=Math.sqrt(Math.pow(2, -52));//get tolerance to reduce eigens
		
		int idx_size=0;//for set idx length 
		double tempeigen[]=new double[eigenvalues.length];
		for(int i=0;i<eigenvalues.length;i++){
			if(Math.abs(eigenvalues[i])>tolerance){
				tempeigen[i]=1;
				idx_size++;
			}
		}
		
		//calculate idx value from eigen values
		double idx[]=matrixReverse(tempeigen);
		int count=1;
		//double idx[]=new double[idx_size];
		for(int i=0;i<idx.length;i++){
			if(idx[i]!=0){
				idx[i]=count;
			}
			count++;
		}	
		//discard eigen values
		double reverseeigen[]=new double[eigenvalues.length];
		int j=0;
		for(int i=eigenvalues.length-1;i>=0;i--){
			reverseeigen[j]=eigenvalues[i];
			j++;
		}
		 eigen_values=new double[idx.length];
		for(int i=0;i<reverseeigen.length;i++){
			for(j=0;j<idx.length;j++){
		if(i+1==idx[j]){
			eigen_values[j]=reverseeigen[i];
		}
			}}
		
		
		//discard eigen vectors
		eigenvectors=new double[eigenvector.length][eigenvector.length];
		for(int i=0;i<eigenvectors.length;i++){
			for( j=0;j<eigenvectors.length;j++){
				for(int k=0;k<idx.length;k++){
					if(j+1==idx[k]){
						eigenvectors[i][k]=eigenvector[i][j];
						}}}
		}
		for(int i=0;i<eigenvectors.length;i++){
			for( j=0;j<eigenvectors.length;j++){
				if(eigenvectors[i][j]==0 && j!=eigenvectors.length-1){
					double temp=eigenvectors[i][j];
					eigenvectors[i][j]=eigenvectors[i][j+1];
					eigenvectors[i][j+1]=temp;
				}
			}}
		//calculate final length of eigen values
		int length_count=0;
		for(int i=0;i<eigen_values.length;i++){
			if(eigen_values[i]==0 && i!=eigen_values.length-1){//shift all zeros to back
				double temp=eigen_values[i];
				eigen_values[i]=eigen_values[i+1];
				eigen_values[i+1]=temp;
			}
				if(eigen_values[i]!=0){
					length_count+=1;//count to reduced eigen val length
				}
			}
		
		
		//need n-1 axes for n objects
	if(length_count>eigen_values.length-1){
		for(int i=0;i<eigen_values.length;i++){
			if(eigen_values[i]!=0 && length_count<i+length_count-1){
				eigen_values[i]=0;
			}
		}
		for(int i=0;i<eigenvectors.length;i++){
			for( j=0;j<eigenvectors.length;j++){
			if(eigenvectors[i][j]!=0 && length_count<j+length_count-1){
				eigenvectors[i][j]=0;
			}
		}}
	}
		return eigenvectors;
	}
	
	//get Variance explained
	public double[][] getVarianceExplained(){
		double eigen_values_sum=0;
		int length=0;//length for cum_sum and var_explain
		for(int i=0;i<eigen_values.length;i++){
			if(eigen_values[i]!=0){
				eigen_values_sum+=eigen_values[i];
				length+=1;
			}
		}
		//calculate varianceExplained
		double var_explain[]=new double[length];
		for(int i=0;i<length;i++){
			var_explain[i]=(eigen_values[i]*100)/eigen_values_sum;
		}
		
		//calculate cumulativeSum
		double cumsum[]=new double[length];
		double sum_temp=0;
		for(int i=0;i<length;i++){
			cumsum[i]=sum_temp+var_explain[i];
			sum_temp=cumsum[i];
		}
		
		//combine two arrays
		combine_array=new double[length][2];//for all this will set a n by 2 matrix
		int j=0;
		for(int i=0;i<length;i++){
			combine_array[i][j]=var_explain[i];
			combine_array[i][j+1]=cumsum[i];
		}
		return combine_array;
	}
	
	//calculate upper triangular matrix from vector
	public double[] getUpperMatrixInVector(double symmetricmat[][]){
		
		int length=0;//calculate size of upper trianguar matrix length
		for (int j = 1; j < symmetricmat.length; j++) {
			length+=j;
		}
		double uppertrimatrix[]=new double[length];
		int p=0;
		for (int i =0; i<symmetricmat.length; i++) {
            for (int j=i ; j<symmetricmat.length ; j++) {
             if(symmetricmat[i][j]!=0){
            	 uppertrimatrix[p]=symmetricmat[i][j];
            	 p++;
             }}
            }
		return uppertrimatrix;
	}
	
	//convert column To Matrix 
	public double[][] convertColumntoMatrix(double columnmatrix[]){
		double matrix[][]=new double[eigen_values.length][eigen_values.length];
		int p=0;
		for(int i=0;i<matrix.length;i++){
			for(int j=i;j<matrix.length;j++){
				if(i==j){
					matrix[i][j]=0;
				}else{
					matrix[i][j]=columnmatrix[p];
					matrix[j][i]=columnmatrix[p];
					p++;
				}
			}	
		}
		return matrix;
	}
	
	
	// handle negative eigen values
	public double[][] negativeEigenAnalysis(){
		
		double negSum=0;
		for(int i=0;i<eigen_values.length;i++){
			if(eigen_values[i]<0){
				negSum+=i+1;
			}
		}
		
		double temp_min=0;
		double uppermatrixp[]=new double[2];//the size can be changed
		double columnmatrix[]=new double[2];//the size can be changed
		double converedmatrix[][]=new double[2][2];//the size can be changed
		if(negSum>0 && neg==2){//should include && correct value check matlab
			
			
			for(int i=0;i<eigen_values.length;i++){
			if(eigen_values[i]<temp_min){
				temp_min=eigen_values[i];
			}}
			temp_min=Math.abs(temp_min);
			 uppermatrixp=getUpperMatrixInVector(data);
			 columnmatrix=new double[uppermatrixp.length];
			 for(int i=0;i<uppermatrixp.length;i++){
				 columnmatrix[i]=Math.sqrt((Math.pow(uppermatrixp[i],2)+2*temp_min));
			 }
			 converedmatrix=convertColumntoMatrix(columnmatrix);
			 CalculationMatrix calc=new CalculationMatrix(converedmatrix.length, converedmatrix.length, converedmatrix,0,0,1);
			 eigen_values=calc.getEigen_values();
			 combine_array=calc.getCombine_array();
			 scores=calc.getScores();

			 
		}else if(negSum>0 && neg==1){
			int count=0;
			for(int i=0;i<eigen_values.length;i++){
				if(eigen_values[i]<0){
					eigen_values[i]=0;
				}else{
					count+=1;
				}
			}
			for(int i=0;i<eigenvectors.length;i++){
				for(int j=0;j<eigenvectors.length;j++){
					if(j+1==count){
						eigenvectors[i][j]=0;
					}}}
			
			for(int i=0;i<combine_array.length;i++){
				for(int j=0;j<combine_array.length;j++){
					if(i+1==count){
						combine_array[i][j]=0;
					}}}
		}
		return combine_array;
	}
	
	//scale eigen vectors
	public void scaleEigenVectors(){
		double temp_eigen[][]=new double[eigen_values.length][eigen_values.length];
		double multi_matrix[][];
		
		if(scale>0){
			
			for(int i=0;i<eigen_values.length;i++){
				for(int j=0;j<eigen_values.length;j++){
					if(j==0){
						temp_eigen[i][j]=Math.pow(Math.abs(eigen_values[i]), 0.5);	
					}else{
						temp_eigen[i][j]=0;
					}
					}}
			
			temp_eigen=transposeMatrix(temp_eigen);
			multi_matrix=new double[eigenvectors.length][temp_eigen.length];
			for(int i=0;i<eigen_values.length;i++){
				for(int j=0;j<temp_eigen.length;j++){
					multi_matrix[i][j]=temp_eigen[0][j];
				}
			}
			scores=multiplyByMatrix(eigenvectors, multi_matrix);
			}else{
				scores=eigenvectors;
			}
	}
}
