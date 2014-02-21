package ch.unibe.droidtorch;

import java.util.List;

public class TorchDevice extends ObservableAsyncTask<List<Device>,Void,Device>{

	public TorchDevice(OnTaskCompletedListener<List<Device>, Void, Device> l) {
		super(l);
	}

	@Override
	protected Device doInBackground(List<Device>... params) {
		assert params != null;
		assert params.length == 1;
		
		Device device = null;
		for (Device dev : params[0]){
			if (Utils.isFileExists(dev.getTorch())) {
				device = dev;
				break;
			}
		}
		return device;
	}

}
