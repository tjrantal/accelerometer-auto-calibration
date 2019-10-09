package timo.jyu;

//Import apache math classes for Levenber - Marquardt optimisation
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.util.Pair;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;

import java.util.Arrays;	//Fill in an array with a value


public class LMOptimisation{
	double[][] acc;
	private double[] fit = null;
	
	/**The error function*/
	MultivariateJacobianFunction errorFunction = new MultivariateJacobianFunction() {
		//This interface has to be implemented. Returns error function values, and the Jacobian of the error
		public Pair<RealVector, RealMatrix> value(final RealVector point) {
			//Apply current calibration
			double[][] calibrated = new double[3][];
			for (int i = 0;i<calibrated.length; ++i){
				calibrated[i] = Utils.applyCalib(acc[i],point.getEntry(2*i),point.getEntry(2*i+1));
			}
			
			
			
			 RealVector value = new ArrayRealVector(calibrated[0].length);
			 RealMatrix jacobian = new Array2DRowRealMatrix(calibrated[0].length, point.getDimension());
			 for (int i = 0; i < calibrated[0].length; ++i) {
			     value.setEntry(i, getDist(new double[]{calibrated[0][i],calibrated[1][i],calibrated[2][i]}));	//Error function result
				 //Jacobian, partial derivatives w.r.t. the parameters in columns
				 //Error function = sqrt((Xo-Xp)^2+(Yo-Yp)^2)-r = 0 -> (Xo-Xp)^2+(Yo-Yp)^2 = r^2
			     jacobian.setEntry(i, 0, 2d*(point.getEntry(0)+point.getEntry(1)*calibrated[0][i]));
				 jacobian.setEntry(i, 1, 2d*(point.getEntry(0)+point.getEntry(1)*calibrated[0][i])*calibrated[0][i]);
				 jacobian.setEntry(i, 2, 2d*(point.getEntry(2)+point.getEntry(3)*calibrated[1][i]));
				 jacobian.setEntry(i, 3, 2d*(point.getEntry(2)+point.getEntry(3)*calibrated[1][i])*calibrated[1][i]);
				 jacobian.setEntry(i, 4, 2d*(point.getEntry(4)+point.getEntry(5)*calibrated[2][i]));
				 jacobian.setEntry(i, 5, 2d*(point.getEntry(4)+point.getEntry(5)*calibrated[2][i])*calibrated[2][i]);
			     
			 }
			 return new Pair<RealVector, RealMatrix>(value, jacobian);
		}
		
		//Calculate distance between origin (params[0 and 1]) and coordinate (test[0 and 1])
		private double getDist(double[] test){
			return Math.sqrt(Math.pow(test[0],2d)+Math.pow(test[1],2d)+Math.pow(test[2],2d));
		}

	};
	
	
	public LMOptimisation(double[][] acc){
		this.acc = acc;
		double[] initGuess = new double[] { 0d, 1d,0d, 1d,0d, 1d};
		double[] prescribedDistances = new double[acc[0].length];
		Arrays.fill(prescribedDistances, 1d);	//Optimisation is based on the error function -> target = 0
		
		LeastSquaresProblem problem = new LeastSquaresBuilder().
				                       start(initGuess).	//Initial guess
				                       model(errorFunction). //MJF
				                       target(prescribedDistances).	//Target values
				                       lazyEvaluation(false).
				                       maxEvaluations(100000).
				                       maxIterations(100000).
				                       build();
     	LevenbergMarquardtOptimizer optimiser  = new LevenbergMarquardtOptimizer().
                                    withCostRelativeTolerance(10*Math.ulp(1d)).
                                    withParameterRelativeTolerance(10*Math.ulp(1d));
		LeastSquaresOptimizer.Optimum optimum = optimiser.optimize(problem);
	
		
		fit = new double[initGuess.length];
		for (int i =0;i<initGuess.length; ++i){
			fit[i] = optimum.getPoint().getEntry(i);
		}
	}
	
	public double[] getFit(){
		return fit;
	}
}