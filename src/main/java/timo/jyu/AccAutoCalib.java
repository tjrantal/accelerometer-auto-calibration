package timo.jyu;

/*Class to calculate van Hees and colleagues (J Appl Physiol 117: 738–744, 2014) autocalibration. 
Temperature calibration is not attempted (my accelerometers do not have a thermometer + temperature drift was shown to have little effect)
Written by tjrantal at gmail dot com. Released into the public domain.
*/

public class AccAutoCalib{
	private double sdThresh;	//g threshold to consider an epoch to not have movement
	private double epochLength;	//[s] for feature extraction (20 used by van Hees et al). Data analysed in non-overlapping  epochs
	private double sRate;
	private Features features; //Store features here

	
	/**Constructors
	Store only features in this class -> can discard the raw data after instantiation*/
	public AccAutoCalib(double[] x, double[] y, double[] z,long[] tStamps,double sRate){
		this(x,y,z,tStamps,sRate,20,0.013);
	}
	
		
	public AccAutoCalib(double[] x, double[] y, double[] z,long[] tStamps,double sRate, double epochLength){
		this(x,y,z,tStamps,sRate,epochLength,0.013);
	}
	
	/**Store only features in this class -> can discard the raw data after instantiation*/
	public AccAutoCalib(double[] x, double[] y, double[] z,long[] tStamps,double sRate, double epochLength, double sdThresh){
		this.epochLength = epochLength;
		this.sdThresh = sdThresh;
		features = new Features();
		features.getFeatures(x,y,z,tStamps,sRate,epochLength);
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