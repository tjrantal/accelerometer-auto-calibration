package timo.jyu;

public class Utils{
	public static double[] applyCalib(double[] a, double intercept, double scale){
		double[] b = new double[a.length];
		for (int i = 0; i<a.length; ++i){
			b[i] = a[i]*scale+intercept;
		}
		return b;
	}
	
	public static double[] minmax(double[] a){
		double[] b = new double[]{Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
		for (int i = 0; i<a.length;++i){
			if (a[i] < b[0]){b[0]  = a[i];}
			if (a[i] > b[1]){b[1]  = a[i];}
		}
		return b;
	}
	
	public static double min(double[] a){
		double b = Double.POSITIVE_INFINITY;
		for (int i = 0; i<a.length;++i){
			if (a[i] < b){b  = a[i];}
		}
		return b;
	}
	
	public static double max(double[] a){
		double b = Double.NEGATIVE_INFINITY;
		for (int i = 0; i<a.length;++i){
			if (a[i] > b){b  = a[i];}
		}
		return b;
	}
}