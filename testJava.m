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


%Test calibration with matlab
joptimData = [javaMethod('getOptimX',javaClass), javaMethod('getOptimY',javaClass), javaMethod('getOptimZ',javaClass)];
global observedData
observedData = joptimData;
optimised = lsqnonlin(@optimiseCalib,[0,1,0,1,0,1]);	%Optimisation without weights

origRes = sqrt(sum(observedData.^2,2));
calibratedData = applyCalib(observedData,optimised);
calibratedDataJ = applyCalib(observedData,calibCoeffs);
calibRes = sqrt(sum(calibratedData.^2,2));
calibResJ = sqrt(sum(calibratedDataJ.^2,2));
disp(sprintf('Coeffs x %.3f %.3f y %.3f %.3f z %.3f %.3f orig e %.3f calib e %.3f',optimised(1),optimised(2),optimised(3),optimised(4),optimised(5),optimised(6),sqrt(sum((origRes-1).^2)),sqrt(sum((calibRes-1).^2))));
disp(sprintf('Coeffs with Java x %.3f %.3f y %.3f %.3f z %.3f %.3f orig e %.3f calib e %.3f',calibCoeffs(1),calibCoeffs(2),calibCoeffs(3),calibCoeffs(4),calibCoeffs(5),calibCoeffs(6),sqrt(sum((origRes-1).^2)),sqrt(sum((calibResJ-1).^2))));


figure
plot(origRes,'k');
hold on;
plot(calibRes,'r');
plot(calibResJ,'g');
title('Resultant from the included epochs');

figure,plot((calibResJ./calibRes).*100)
title('Matlab resultant divided by BOBYQA Java optimisation');
