package ch.unibe.droidtorch;

import java.util.List;

public class TorchDeviceSelectTask extends ObservableAsyncTask<List<Device>,Void,Device>{

	public TorchDeviceSelectTask(OnTaskCompletedListener<List<Device>, Void, Device> l) {
		super(l);
	}

	@Override
	protected Device doInBackground(List<Device> devices) {
		for (Device device : devices){
			if (Utils.isFileExists(device.getTorch())) {
				return device;
			}
		}
		return Device.NULL;
	}

}
