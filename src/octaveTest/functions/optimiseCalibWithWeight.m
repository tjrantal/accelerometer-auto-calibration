%x = [xOffs,xCoeff,yOffs,yCoeff,zOffs,zCoeff]
function y = optimiseCalibWithWeight(x)
	global observedData weights;
	differences = sqrt(sum(applyCalib(observedData,x).^2,2))-1;
	tempWeights = abs(1./differences); %Update the weights prior to applying weights
	weighteddifferences = (differences.*weights)./sum(weights);	%Apply the weights
	weights = tempWeights;	
	weights(weights >100) = 100;	%Cap the maximum weight at 100
	y = weighteddifferences;	%Optimise this sum -> have to evaluate ode at sampling instants
end