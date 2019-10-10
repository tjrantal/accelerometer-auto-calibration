fclose all;
close all;
clear all;
clc;

javaclasspath({'build/libs/accelerometer-auto-calibration-1.0.jar'});

addpath('src/octaveTest/functions');
dataFolder = 'src/res/';
accFile = getFilesAndFolders([dataFolder]);

sdThresh = 0.013;	%13 mg threshold to be included in calibration
epochLength = 5; %Seconds. (van Hees et al used 10 s but I didn't allow 10 s per orientation)
data = readLog([dataFolder accFile(1).name]);
sFreq = 1/median(diff(data.data(:,1)./1000)); %Time stamps are milliseconds
acc = data.data(:,2:4)./9.81;	%Acceleration in g

javaClass = javaObject('timo.jyu.AccAutoCalib',acc(:,1),acc(:,2),acc(:,3),int64(data.data(:,1)),epochLength,sdThresh);
calibCoeffs = javaMethod('getFit',javaClass);
calibCoeffsw = javaMethod('getWeightedFit',javaClass);  %With weights
calibCoeffsLM = javaMethod('getLMFit',javaClass);  %
calibCoeffsLMw = javaMethod('getWeightedLMFit',javaClass);  %With weights
lmWeights = javaMethod('getLMWeights',javaClass);  %With weights
boWeights = javaMethod('getBoWeights',javaClass);  %With weights




% [calibCoeffs'; calibCoeffsw';calibCoeffsLM'; calibCoeffsLMw']

%Test calibration with matlab
joptimData = [javaMethod('getOptimX',javaClass), javaMethod('getOptimY',javaClass), javaMethod('getOptimZ',javaClass)];
global observedData weights
observedData = joptimData;
weights = ones(size(observedData,1),1);
optimised = lsqnonlin(@optimiseCalib,[0,1,0,1,0,1]);	%Optimisation without weights
optimisedw = lsqnonlin(@optimiseCalibWithWeight,[0,1,0,1,0,1]);	%Optimisation with weights

allWeights = [weights, boWeights, lmWeights];

origRes = sqrt(sum(observedData.^2,2));
calibratedData = applyCalib(observedData,optimised);
calibratedDataw = applyCalib(observedData,optimisedw);
calibratedDataJ = applyCalib(observedData,calibCoeffs);
calibratedDataJW = applyCalib(observedData,calibCoeffsw);
calibratedDataLM = applyCalib(observedData,calibCoeffsLM);
calibratedDataLMw = applyCalib(observedData,calibCoeffsLMw);

calibRes = sqrt(sum(calibratedData.^2,2));
calibResw = sqrt(sum(calibratedDataw.^2,2));
calibResJ = sqrt(sum(calibratedDataJ.^2,2));
calibResJW = sqrt(sum(calibratedDataJW.^2,2));
calibResLM = sqrt(sum(calibratedDataLM.^2,2));
calibResLMw = sqrt(sum(calibratedDataLMw.^2,2));
disp(sprintf('Coeffs x %.3f %.3f y %.3f %.3f z %.3f %.3f orig e %.3f calib e %.3f',optimised(1),optimised(2),optimised(3),optimised(4),optimised(5),optimised(6),sum((origRes-1).^2),sum((calibRes-1).^2)));
disp(sprintf('Coeffs w x %.3f %.3f y %.3f %.3f z %.3f %.3f orig e %.3f calib e %.3f',optimisedw(1),optimisedw(2),optimisedw(3),optimisedw(4),optimisedw(5),optimisedw(6),sum((origRes-1).^2),sum((calibResw-1).^2)));
disp(sprintf('Coeffs B x %.3f %.3f y %.3f %.3f z %.3f %.3f orig e %.3f calib e %.3f',calibCoeffs(1),calibCoeffs(2),calibCoeffs(3),calibCoeffs(4),calibCoeffs(5),calibCoeffs(6),sum((origRes-1).^2),sum((calibResJ-1).^2)));
disp(sprintf('Coeffs BW x %.3f %.3f y %.3f %.3f z %.3f %.3f orig e %.3f calib e %.3f',calibCoeffsw(1),calibCoeffsw(2),calibCoeffsw(3),calibCoeffsw(4),calibCoeffsw(5),calibCoeffsw(6),sum((origRes-1).^2),sum((calibResJW-1).^2)));
disp(sprintf('Coeffs LM x %.3f %.3f y %.3f %.3f z %.3f %.3f orig e %.3f calib e %.3f',calibCoeffsLM(1),calibCoeffsLM(2),calibCoeffsLM(3),calibCoeffsLM(4),calibCoeffsLM(5),calibCoeffsLM(6),sum((origRes-1).^2),sum((calibResLM-1).^2)));
disp(sprintf('Coeffs LMw x %.3f %.3f y %.3f %.3f z %.3f %.3f orig e %.3f calib e %.3f',calibCoeffsLMw(1),calibCoeffsLMw(2),calibCoeffsLMw(3),calibCoeffsLMw(4),calibCoeffsLMw(5),calibCoeffsLMw(6),sum((origRes-1).^2),sum((calibResLMw-1).^2)));

figure
plot(origRes,'k');
hold on;
plot(calibRes,'r');
plot(calibResw,'r--');
plot(calibResJ,'g');
plot(calibResJW,'g--');
plot(calibResLM,'b');
plot(calibResLMw,'b--');

plot([1 length(origRes)],1+[0.01 0.01],'r-.');
plot([1 length(origRes)],1-[0.01 0.01],'r-.');

title('Resultant from the included epochs');

if 0
    figure
    plot((calibResJ./calibRes).*100,'r')
    hold on;
    plot((calibResJW./calibRes).*100,'g')
    title('Matlab resultant divided by BOBYQA Java optimisation');
end