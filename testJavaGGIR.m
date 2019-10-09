fclose all;
close all;
clear all;
clc;

javaclasspath({'build/libs/accelerometer-auto-calibration-1.0.jar'});

addpath('src/octaveTest/functions');

sdThresh = 0.013;	%13 mg threshold to be included in calibration
epochLength = 10; %Seconds. (van Hees et al used 10 s but I didn't allow 10 s per orientation)
if ~exist('ggirSimulated.mat','file')
    fh = fopen('123A_testaccfile.csv','r');
    for i = 1:11
        lineOfData = fgetl(fh);
    end
    data = fscanf(fh,'"%f","%f","%f"\n',[3,inf]);
    fclose(fh);
    data = data';
    save('ggirSimulated.mat','data','-v7');
else
    load('ggirSimulated.mat');
end

sFreq = 50; %This was manually set into the simulated file. File simulated with GGIR create_test_acc_csv(50,24*60*2)
tStamps = int64(([1:size(data,1)]-1)./sFreq*1000); %ms timestamps

ggirOptimised = [-0.000669426836644031 0.984798233302441 5.65010201476697e-07 0.984193867141311 -2.31543715662464e-05 0.984380532090462];

javaClass = javaObject('timo.jyu.AccAutoCalib',data(:,1),data(:,2),data(:,3),tStamps,epochLength,sdThresh);
calibCoeffs = javaMethod('getFit',javaClass);
calibCoeffsw = javaMethod('getWeightedFit',javaClass);  %With weights

[calibCoeffs'; calibCoeffsw']
% 
%Test calibration with matlab
joptimData = [javaMethod('getOptimX',javaClass), javaMethod('getOptimY',javaClass), javaMethod('getOptimZ',javaClass)];
global observedData weights
observedData = joptimData;
weights = ones(size(observedData,1),1);
optimised = lsqnonlin(@optimiseCalib,[0,1,0,1,0,1]);	%Optimisation without weights
optimisedw = lsqnonlin(@optimiseCalibWithWeight,[0,1,0,1,0,1]);	%Optimisation with weights


% [optimisedw; calibCoeffsw']

[optimised; optimisedw]

origRes = sqrt(sum(observedData.^2,2));
calibratedData = applyCalib(observedData,optimised);
calibratedDataW = applyCalib(observedData,optimisedw);
calibratedDataJ = applyCalib(observedData,calibCoeffs);
calibratedDataJW = applyCalib(observedData,calibCoeffsw);
calibratedGGIR = applyCalib(observedData,ggirOptimised);
calibRes = sqrt(sum(calibratedData.^2,2));
calibResW = sqrt(sum(calibratedDataW.^2,2));
calibResJ = sqrt(sum(calibratedDataJ.^2,2));
calibResJW = sqrt(sum(calibratedDataJW.^2,2));
ggirRes = sqrt(sum(calibratedGGIR.^2,2));
disp(sprintf('Coeffs x %.3f %.3f y %.3f %.3f z %.3f %.3f orig e %.3f calib e %.3f',optimised(1),optimised(2),optimised(3),optimised(4),optimised(5),optimised(6),sqrt(sum((origRes-1).^2)),sqrt(sum((calibRes-1).^2))));
disp(sprintf('CoeffsW x %.3f %.3f y %.3f %.3f z %.3f %.3f orig e %.3f calib e %.3f',optimisedw(1),optimisedw(2),optimisedw(3),optimisedw(4),optimisedw(5),optimisedw(6),sqrt(sum((origRes-1).^2)),sqrt(sum((calibResW-1).^2))));
disp(sprintf('Coeffs with Java x %.3f %.3f y %.3f %.3f z %.3f %.3f orig e %.3f calib e %.3f',calibCoeffs(1),calibCoeffs(2),calibCoeffs(3),calibCoeffs(4),calibCoeffs(5),calibCoeffs(6),sqrt(sum((origRes-1).^2)),sqrt(sum((calibResJ-1).^2))));
disp(sprintf('Coeffs with JavaW x %.3f %.3f y %.3f %.3f z %.3f %.3f orig e %.3f calib e %.3f',calibCoeffsw(1),calibCoeffsw(2),calibCoeffsw(3),calibCoeffsw(4),calibCoeffsw(5),calibCoeffsw(6),sqrt(sum((origRes-1).^2)),sqrt(sum((calibResJW-1).^2))));
disp(sprintf('Coeffs with GGIR x %.3f %.3f y %.3f %.3f z %.3f %.3f orig e %.3f calib e %.3f',ggirOptimised(1),ggirOptimised(2),ggirOptimised(3),ggirOptimised(4),ggirOptimised(5),ggirOptimised(6),sqrt(sum((origRes-1).^2)),sqrt(sum((ggirRes-1).^2))));

figure
plot(observedData)


figure
plot(origRes,'k');
hold on
plot(calibRes,'r--');
plot(calibResW,'g--');
plot(calibResJ,'r-');
plot(calibResJW,'g-');
plot(ggirRes,'linestyle','-','color',[.5 .5 .5]);

% 
% 
% figure
% plot(origRes,'k');
% hold on;
% plot(calibRes,'r');
% plot(calibResJ,'g');
% plot(calibResJW,'g--');
% title('Resultant from the included epochs');
% 
% figure
% plot((calibResJ./calibRes).*100,'r')
% hold on;
% plot((calibResJW./calibRes).*100,'g')
% title('Matlab resultant divided by BOBYQA Java optimisation');
