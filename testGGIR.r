#rootPath= "U:/tyo/programming/java/accelerometer-auto-calibration"
#rootPath = 'smb://fileservices.ad.jyu.fi/homes/tjrantal/tyo/programming/java/accelerometer-auto-calibration'
setwd(rootPath)
library(GGIR)
source('create_test_acc_csv.R')

create_test_acc_csv(50,24*60*2)
test = g.calibrate("123A_testaccfile.csv",use.temp=F)
 
 
 #load('data.calibrate.RData')

