package timo.jyu;

/*Class to calculate van Hees and colleagues (J Appl Physiol 117: 738–744, 2014) autocalibration. 
Temperature calibration is not attempted (my accelerometers do not have a thermometer + temperature drift was shown to have little effect)
Written by tjrantal at gmail dot com. Released into the public domain.
*/

public class AccAutoCalib{
	private double sdThresh;	//g threshold to consider an epoch to not have movement
	private double epochLength;	//[s] for feature extraction (10 used by van Hees et al). Data analysed in non-overlapping  epochs
	private Features features; //Store features here
	private double[][] toOptimisation = null;
	
	/**Constructors
	Store only features in this class -> can discard the raw data after instantiation*/
	public AccAutoCalib(double[] x, double[] y, double[] z,long[] tStamps){
		this(x,y,z,tStamps,10,0.013);
	}
	
		
	public AccAutoCalib(double[] x, double[] y, double[] z,long[] tStamps, double epochLength){
		this(x,y,z,tStamps,epochLength,0.013);
	}
	
	/**Store only features in this class -> can discard the raw data after instantiation*/
	public AccAutoCalib(double[] x, double[] y, double[] z,long[] tStamps, double epochLength, double sdThresh){
		this.epochLength = epochLength;
		this.sdThresh = sdThresh;
		features = new Features();
		features.getFeatures(x,y,z,tStamps,epochLength);
		Features tempFeat = new Features();
		//Take out epochs with more than sdThresh variation on any axis
		for (int i = 0;i<features.x.size(); ++i){
			if (features.sdx.get(i) < sdThresh && features.sdy.get(i) < sdThresh && features.sdz.get(i) < sdThresh){
				tempFeat.x.add(features.x.get(i));
				tempFeat.y.add(features.y.get(i));
				tempFeat.z.add(features.z.get(i));
			}
		}
		if (tempFeat.x.size() > 1){
			toOptimisation = new double[3][];
			toOptimisation[0] = tempFeat.getPrimitive(tempFeat.x);
			toOptimisation[1] = tempFeat.getPrimitive(tempFeat.y);
			toOptimisation[2] = tempFeat.getPrimitive(tempFeat.z);
		}else{
			System.out.println("Fewer than 10 data points remained");
		}
		
	}
	
	public double[] getFit(){
			BOBYQAOptimisation bo;
			double[][] mm = {Utils.minmax(toOptimisation[0]),Utils.minmax(toOptimisation[1]),Utils.minmax(toOptimisation[2])};
			if (Utils.max(new double[]{mm[0][0],mm[1][0],mm[2][0]}) < -0.3 && Utils.min(new double[]{mm[0][1],mm[1][1],mm[2][1]}) > 0.3){
				bo = new BOBYQAOptimisation(toOptimisation);
			}else{
				System.out.println("Sufficient data was not found to calibrate");
				return null;
			}
			return bo.getFit();
	}
	
	public double[] getWeightedFit(){
		BOBYQAOptimisationWeights bo;
		double[][] mm = {Utils.minmax(toOptimisation[0]),Utils.minmax(toOptimisation[1]),Utils.minmax(toOptimisation[2])};
		if (Utils.max(new double[]{mm[0][0],mm[1][0],mm[2][0]}) < -0.3 && Utils.min(new double[]{mm[0][1],mm[1][1],mm[2][1]}) > 0.3){
			bo = new BOBYQAOptimisationWeights(toOptimisation);
		}else{
			System.out.println("Sufficient data was not found to calibrate");
			return null;
		}
		return bo.getFit();
	}
	
	public double[] getLMFit(){
		LMOptimisation bo;
		double[][] mm = {Utils.minmax(toOptimisation[0]),Utils.minmax(toOptimisation[1]),Utils.minmax(toOptimisation[2])};
		if (Utils.max(new double[]{mm[0][0],mm[1][0],mm[2][0]}) < -0.3 && Utils.min(new double[]{mm[0][1],mm[1][1],mm[2][1]}) > 0.3){
			bo = new LMOptimisation(toOptimisation);
		}else{
			System.out.println("Sufficient data was not found to calibrate");
			return null;
		}
		return bo.getFit();
	}
	
	
	public double[] getOptimX(){
		return toOptimisation[0];
	}

	public double[] getOptimY(){
		return toOptimisation[1];
	}
	
	public double[] getOptimZ(){
		return toOptimisation[2];
	}
	
	
	
	/**Methods*/
	//Functions to modify analysis parameters
	public void setEpoch(double e){this.epochLength = e;}
	public void setSdTrhesh(double s){this.sdThresh = s;}
	
	public double[] getX(){return features.getPrimitive(features.x);}
	public double[] getY(){return features.getPrimitive(features.y);}
	public double[] getZ(){return features.getPrimitive(features.z);}
	public double[] getSDX(){return features.getPrimitive(features.sdx);}
	public double[] getSDY(){return features.getPrimitive(features.sdy);}
	public double[] getSDZ(){return features.getPrimitive(features.sdz);}
	public long[] gettStamps(){return features.getPrimitiveL(features.tStamps);}
	
	
	
}