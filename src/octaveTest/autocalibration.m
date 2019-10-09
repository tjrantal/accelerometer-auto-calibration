%si'(t) = di * si(t) * ai -> calibrated signal

fclose all;
close all;
clear all;
clc;

if exist('OCTAVE_VERSION', 'builtin')
    pkg load optim signal
end

addpath('functions');
dataFolder = '../res/';
accFile = getFilesAndFolders([dataFolder]);

sdThresh = 0.013;	%13 mg threshold to be included in calibration
epochLength = 5; %Seconds. (van Hees et al used 10 s but I didn't allow 10 s per orientation)
data = readLog([dataFolder accFile(1).name]);
sFreq = 1/median(diff(data.data(:,1)./1000)); %Time stamps are milliseconds
acc = data.data(:,2:4)./9.81;	%Acceleration in g
resultant = sqrt(sum(acc.^2,2));


%Calculate features in 5 s epochs (van Hees et al used 10 s but I didn't allow 10 s per orientation). We need SDs and mean values for each axis
[features ind]= getFeatures(acc,epochLength,sFreq);

calibrationEpochIndices = find(features(1).sd < sdThresh & features(2).sd < sdThresh & features(3).sd < sdThresh);
global observedData weights %optimiseCalib requires this data
observedData = [features(1).mean(calibrationEpochIndices)', features(2).mean(calibrationEpochIndices)',features(3).mean(calibrationEpochIndices)'];
weights = ones(size(observedData,1),1);
origRes = sqrt(sum(observedData.^2,2));
%Check sanity
if max(min(observedData)) > -0.3 || min(max(observedData)) < 0.3
	disp('Not sufficient data to calibrate');
	return;
end



optimised = lsqnonlin(@optimiseCalib,[0,1,0,1,0,1]);	%Optimisation without weights
optimisedw = lsqnonlin(@optimiseCalibWithWeight,[0,1,0,1,0,1]);	%Optimisation with weights



calibratedData = applyCalib(observedData,optimised);
calibratedDataw = applyCalib(observedData,optimisedw);
calibRes = sqrt(sum(calibratedData.^2,2));
calibResw = sqrt(sum(calibratedDataw.^2,2));
disp(sprintf('Coeffs x %.3f %.3f y %.3f %.3f z %.3f %.3f orig e %.3f calib e %.3f',optimised(1),optimised(2),optimised(3),optimised(4),optimised(5),optimised(6),sqrt(sum((origRes-1).^2)),sqrt(sum((calibRes-1).^2))));
disp(sprintf('Coeffs with weight x %.3f %.3f y %.3f %.3f z %.3f %.3f orig e %.3f calib e %.3f',optimisedw(1),optimisedw(2),optimisedw(3),optimisedw(4),optimisedw(5),optimisedw(6),sqrt(sum((origRes-1).^2)),sqrt(sum((calibResw-1).^2))));
% keyboard;
	
figure

plot(observedData(:,1),'r','linewidth',3,'linestyle','--');
hold on;
plot(calibratedData(:,1),'r','linewidth',3,'linestyle','-');
plot(calibratedDataw(:,1),'r','linewidth',3,'linestyle','-.');
plot(observedData(:,2),'g','linewidth',3,'linestyle','--');
plot(calibratedData(:,2),'g','linewidth',3,'linestyle','-');
plot(calibratedDataw(:,2),'g','linewidth',3,'linestyle','-.');
plot(observedData(:,3),'b','linewidth',3,'linestyle','--');
plot(calibratedData(:,3),'b','linewidth',3,'linestyle','-');
plot(calibratedDataw(:,3),'b','linewidth',3,'linestyle','-.');
plot(origRes,'k','linewidth',3,'linestyle','--');
plot(calibRes,'k','linewidth',3,'linestyle','-');
plot(calibResw,'k','color',[.5 .5 .5],'linewidth',3,'linestyle','-');
	
	
if 1

	figure
	plot(acc)
	hold on;
	plot(resultant,'k','linewidth',3);
	plot(ind(calibrationEpochIndices),calibRes,'r','linewidth',3);
	plot(ind(calibrationEpochIndices),calibResw,'g','linewidth',3);
    
    figure
    plot(acc,'k')
    hold on
    plot(applyCalib(acc,optimised),'r');
    plot(applyCalib(acc,optimisedw),'b');
    title('No weights calibration');
    
    figure
    plot(acc)
    hold on
    
    title('With weights calibration');
end