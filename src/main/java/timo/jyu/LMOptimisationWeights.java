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


public class LMOptimisationWeights{
	double[][] acc;
	private double[] fit = null;
	double[] weights;
	int cnt = 0;
	
	/**The error function*/
	MultivariateJacobianFunction errorFunction = new MultivariateJacobianFunction() {
		//This interface has to be implemented. Returns error function values, and the Jacobian of the error
		public Pair<RealVector, RealMatrix> value(final RealVector point) {
			cnt++;
			
				
			//Apply current calibration
			double[][] calibrated = new double[3][];
			for (int i = 0;i<calibrated.length; ++i){
				calibrated[i] = Utils.applyCalib(acc[i],point.getEntry(2*i),point.getEntry(2*i+1));
			}
			
			RealVector value = new ArrayRealVector(calibrated[0].length);
			RealMatrix jacobian = new Array2DRowRealMatrix(calibrated[0].length, point.getDimension());
			double weightMean = Utils.sum(weights)/((double) weights.length);
			for (int i = 0; i < calibrated[0].length; ++i) {
				double diffVal = getDist(new double[]{calibrated[0][i],calibrated[1][i],calibrated[2][i]})-1d;
				double weightMultiplier = weights[i]/weightMean;
				value.setEntry(i, diffVal*weightMultiplier);	//Weighted error function result include both positive and negative values
				//Jacobian, partial derivatives w.r.t. the parameters in columns
				jacobian.setEntry(i, 0, 2d*(point.getEntry(0)+point.getEntry(1)*calibrated[0][i]*weightMultiplier));
				jacobian.setEntry(i, 1, 2d*(point.getEntry(0)+point.getEntry(1)*calibrated[0][i]*weightMultiplier)*calibrated[0][i]*weightMultiplier);
				jacobian.setEntry(i, 2, 2d*(point.getEntry(2)+point.getEntry(3)*calibrated[1][i]*weightMultiplier));
				jacobian.setEntry(i, 3, 2d*(point.getEntry(2)+point.getEntry(3)*calibrated[1][i]*weightMultiplier)*calibrated[1][i]*weightMultiplier);
				jacobian.setEntry(i, 4, 2d*(point.getEntry(4)+point.getEntry(5)*calibrated[2][i]*weightMultiplier));
				jacobian.setEntry(i, 5, 2d*(point.getEntry(4)+point.getEntry(5)*calibrated[2][i]*weightMultiplier)*calibrated[2][i]*weightMultiplier);

				weights[i] = Math.abs(1d/diffVal) > 100d ? 100d : Math.abs(1d/diffVal);	//Update the weights

			}
			return new Pair<RealVector, RealMatrix>(value, jacobian);
		}
		
		//Calculate distance between origin (params[0 and 1]) and coordinate (test[0 and 1])
		private double getDist(double[] test){
			return Math.sqrt(Math.pow(test[0],2d)+Math.pow(test[1],2d)+Math.pow(test[2],2d));
		}

	};
	
	
	public LMOptimisationWeights(double[][] acc){
		double[] prescribedDistances = new double[acc[0].length];
		Arrays.fill(prescribedDistances, 0d);	//Optimisation is based on the error function -> target = 0
		
		this.acc = acc;
		//Initialise weights
		weights = new double[acc[0].length];
		Arrays.fill(weights, 1d);
		double[] initGuess = new double[] { 0d, 1d,0d, 1d,0d, 1d};
		
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
	
	public double[] getWeights(){
		return weights;
	}
}