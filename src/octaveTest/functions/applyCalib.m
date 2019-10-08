function calibrated = applyCalib(acc,calib)
	calibrated = [calib(1)+calib(2)*acc(:,1),calib(3)+calib(4)*acc(:,2),calib(5)+calib(6)*acc(:,3)];
end