package timo.jyu;

import java.util.ArrayList;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.moment.Mean;

public class Features{
	public ArrayList<Long> tStamps;
	public ArrayList<Double> x;
	public ArrayList<Double> y;
	public ArrayList<Double> z;
	public ArrayList<Double> sdx;
	public ArrayList<Double> sdy;
	public ArrayList<Double> sdz;
	private Mean mean;
	private StandardDeviation std;
	public Features(){
		x = new ArrayList<Double>();
		y = new ArrayList<Double>();
		z = new ArrayList<Double>();
		sdx = new ArrayList<Double>();
		sdy = new ArrayList<Double>();
		sdz = new ArrayList<Double>();
		tStamps = new ArrayList<Long>();
		mean = new Mean();
		std = new StandardDeviation();
		
	}
	
	//Calculate features here
	/**
		@param x x-axis acceleration in g
		@param y y-axis acceleration in g
		@param z z-axis acceleration in g
		@param t time stamps in milliseconds
		@param epoch non-overlapping epoch length in s
		
	*/
	public void getFeatures(double[] x, double[] y, double[] z, long[] t, double epoch){
		long increment = ((long) (epoch))*1000l;
		long nextTarget = t[0]+increment;
		long prevStartStamp = t[0];
		
		ArrayList<Double> tx = new ArrayList<Double>();
		ArrayList<Double> ty = new ArrayList<Double>();
		ArrayList<Double> tz = new ArrayList<Double>();
		int index = 0;
		while (index < x.length) {
			if (t[index] < nextTarget){
				tx.add(x[index]);
				ty.add(y[index]);
				tz.add(z[index]);
			}else{
				/*calculate features for the present data here. Consider only if at least some data was found*/
				if (tx.size() > 10 & ty.size() > 10  & tz.size() > 10){
					double[] temp = getMeanSD(tx);
					this.x.add(temp[0]);
					sdx.add(temp[1]);
					temp = getMeanSD(ty);
					this.y.add(temp[0]);
					sdy.add(temp[1]);
					temp = getMeanSD(tz);
					this.z.add(temp[0]);
					sdz.add(temp[1]);
					tStamps.add(prevStartStamp);
				}
				tx.clear();
				ty.clear();
				tz.clear();
				prevStartStamp = nextTarget;
				nextTarget+=increment;
			}
			++index;
		}
		
	}
	
	/**Evaluate mean and SD*/
	private double[] getMeanSD(ArrayList<Double> a){
		double[] b = getPrimitive(a);
		mean.clear();
		std.clear();
		double m = mean.evaluate(b,0,b.length);
		return new double[]{m,std.evaluate(b,m)};
	}
	
	public double[] getPrimitive(ArrayList<Double> a){
		double[] b = new double[a.size()];
		for (int i = 0; i<b.length;++i){
			b[i] = a.get(i);
		}
		return b;
	}
	
	public long[] getPrimitiveL(ArrayList<Long> a){
		long[] b = new long[a.size()];
		for (int i = 0; i<b.length;++i){
			b[i] = a.get(i);
		}
		return b;
	}
}