function [feat indices] = getFeatures(dataIn,epochLength,sRate)
	feat = struct();
	buffered = struct();
	for d = 1:size(dataIn,2)
		[buffered(d).data discard] = buffer(dataIn(:,d),round(epochLength*sRate),0,'nodelay');
		tempCell = num2cell(buffered(d).data,1);
		feat(d).mean = cellfun(@(x) mean(x), tempCell,'uni',1);
		feat(d).sd = cellfun(@(x) std(x), tempCell,'uni',1);
		if ~exist('indices','var')
			indices = 1:length(dataIn(:,d));
			[temp discard] = buffer(indices,round(epochLength*sRate),0,'nodelay');
			indices = temp(1,:);
		end

	end
end