package timo.jyu;

//Import apache math classes for optimisation
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;
import org.apache.commons.math3.util.Pair;

import java.util.Arrays;	//Fill in an array with a value


public class BOBYQAOptimisation{
	double[][] acc;
	private double[] fit = null;

	/**The error function class*/
	public class ErrorFunction implements MultivariateFunction {
			/**Implement the MultivariateFunction interface. This is the summed squares error
				@input x are the parameters that are being optimised
			*/
        @Override
        public double value(double[] x) {
			//Apply current calibration
			double[][] calibrated = new double[3][];
			
			for (int i = 0;i<calibrated.length; ++i){
				calibrated[i] = Utils.applyCalib(acc[i],x[2*i],x[2*i+1]);
				
			}
			
			double f = 0;	//Sum of squared residuals
			//Calculate sum of squared residuals
			for (int i = 0;i<calibrated[0].length; ++i){
				f+=Math.sqrt(Math.pow(
						Math.sqrt(Math.pow(calibrated[0][i],2d)+Math.pow(calibrated[1][i],2d)+Math.pow(calibrated[2][i],2d))-1d				
				,2d));
			}

            return f;
        }
    }	
	
	public BOBYQAOptimisation(double[][] acc){
		this.acc = acc;
		double[] initGuess = new double[] { 0d, 1d,0d, 1d,0d, 1d};
		int numIterpolationPoints = 2 * initGuess.length + 1;
		int maxEvaluations = 200000;
        BOBYQAOptimizer optim = new BOBYQAOptimizer(numIterpolationPoints);
        PointValuePair result = optim.optimize(new MaxEval(maxEvaluations),
                           new ObjectiveFunction(new ErrorFunction()),
                           GoalType.MINIMIZE,
                           SimpleBounds.unbounded(initGuess.length),
                           new InitialGuess(initGuess));
		
		fit = new double[initGuess.length];
		for (int i =0;i<initGuess.length; ++i){
			fit[i] = result.getPoint()[i];
		}
	}
	
	public double[] getFit(){
		return fit;
	}
}