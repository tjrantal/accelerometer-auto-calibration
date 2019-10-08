%x = [xOffs,xCoeff,yOffs,yCoeff,zOffs,zCoeff]
function y = optimiseCalib(x)
	global observedData;
	y = sqrt(sum(applyCalib(observedData,x).^2,2))-1;	%Optimise this sum -> have to evaluate ode at sampling instants
end