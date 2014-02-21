package ch.unibe.droidtorch;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.RemoteViews;
import android.widget.Toast;

import ch.unibe.droidtorch.ObservableAsyncTask.OnTaskCompletedListener;

/**
 * 
 * @author Aliaksei Syrel
 * @version 1.2
 */
public class TorchWidget extends AppWidgetProvider {

	public static final String ACTION_SWITCH_TORCH = "actionSwitchTorch";
	public static final String PREF_TORCH_DEVICE = "torch_device";
	
	private boolean lock = false;
	
	public static final LinkedList<Device> TORCH_DEVICES;
	static {
		TORCH_DEVICES = new LinkedList<Device>();
		TORCH_DEVICES.add(Device.SAMSUNG_GALAXY_S4);
		TORCH_DEVICES.add(Device.NEXUS_5);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,int[] appWidgetIds) {
		Intent action = new Intent(context, TorchWidget.class);
		action.setAction(ACTION_SWITCH_TORCH);

		PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context,0, action, 0);

		// creating widget view
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.torch);
		// setting onclick intent action
		remoteViews.setOnClickPendingIntent(R.id.torch, actionPendingIntent);

		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		if (ACTION_SWITCH_TORCH.equals(action)) {
			Device device = Device.loadDevice(context);
			if (!device.isNull())
				switchTorch(context,device);
			else
				selectTorchDevice(context);
		}
		super.onReceive(context, intent);
	}

	private void switchTorch(Context context, Device device) {
		boolean isOn = device.isOn(context);
		if (isOn) 
			device.turnOff(context);
		else
			device.turnOn(context);

		// updating widget status image
		ComponentName thisWidget = new ComponentName(context.getApplicationContext(),TorchWidget.class);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.torch);
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		initWidgetView(!isOn, remoteViews);
		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
	}
	
	
	@SuppressWarnings("unchecked")
	private void selectTorchDevice(final Context context){
		if (lock) return;
		lock = true;
		new TorchDeviceSelectTask(new TorchDeviceSelectOnCompleted(context)).execute(TORCH_DEVICES);
	}
	

	/**
	 * 
	 * @param isOn
	 * @param remoteViews
	 */
	private void initWidgetView(boolean isOn, RemoteViews remoteViews) {
		remoteViews.setImageViewResource(R.id.torch,
				(isOn) ? R.drawable.widget_torch_on : R.drawable.widget_torch_off);
	}
	
	/**
	 * 
	 * @author aliaksei
	 */
	private class TorchDeviceSelectOnCompleted implements OnTaskCompletedListener<List<Device>, Void, Device> {
		
		private Context context;
		
		public TorchDeviceSelectOnCompleted(Context context){
			this.context = context;
		}
		
		@Override
		public void onTaskCompleted(AsyncTask<List<Device>, Void, Device> task) {
			try {
				Device device = task.get();
				if (!device.isNull())
					success(device);
				else
					error();
			}
			catch (InterruptedException e) {error();}
			catch (ExecutionException e) {error();}
			lock = false;
		}
		
		private void success(Device device){
			Device.saveDevice(context,device);
			switchTorch(context,device);
		}
		
		private void error(){
			Toast.makeText(context, "Wrong flash device", Toast.LENGTH_SHORT).show();
		}
		
	}
}