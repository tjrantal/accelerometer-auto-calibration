%si'(t) = di * si(t) * ai -> calibrated signal

fclose all;
close all;
clear all;
clc;

pkg load optim signal

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
observedData = [features(1).mean(calibrationEpochIndices)', features(2).mean(calibrationEpochIndices)',features(3).mean(calibrationEpochIndices)'];

%Check sanity
if max(min(observedData)) > -0.3 | min(max(observedData)) < 0.3
	disp('Not sufficient data to calibrate');
	return;
end

figure
plot(observedData,'linewidth',3);


figure
plot(acc)
hold on;
plot(resultant,'k','linewidth',3);
plot(ind,features(1).mean,'r','linewidth',3);
plot(ind,features(1).sd,'g','linewidth',3);