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


%Calculate features in 5 s epochs (van Hees et al used 10 s but I didn't allow 10 s per orientation). We need SDs and mean values for each axis
[features ind]= getFeatures(acc,epochLength,sFreq);
tStamps = int64(data.data(ind,1));
jStamps = javaMethod('gettStamps',javaClass);
javaFeatures = [javaMethod('getX',javaClass), javaMethod('getY',javaClass), javaMethod('getZ',javaClass) ...
                javaMethod('getSDX',javaClass), javaMethod('getSDY',javaClass), javaMethod('getSDZ',javaClass)];
            
            
        
 figure
plot(features(1).mean,'r*-')
hold on;
plot(javaFeatures(:,1),'ro--')
plot(features(2).mean,'g*-')
plot(javaFeatures(:,2),'go--')
plot(features(3).mean,'b*-')
plot(javaFeatures(:,3),'bo--')

 figure
plot(features(1).sd,'r*-')
hold on;
plot(javaFeatures(:,4),'ro--')
plot(features(2).sd,'g*-')
plot(javaFeatures(:,5),'go--')
plot(features(3).sd,'b*-')
plot(javaFeatures(:,6),'bo--')



