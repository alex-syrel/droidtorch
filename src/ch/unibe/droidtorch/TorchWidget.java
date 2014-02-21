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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.widget.RemoteViews;
import android.widget.Toast;

import ch.unibe.droidtorch.ObservableAsyncTask.OnTaskCompletedListener;

public class TorchWidget extends AppWidgetProvider {

	public static final String ACTION_SWITCH_TORCH = "actionSwitchTorch";
	public static final String PREF_TORCH_STATE = "torch_state";
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

		System.out.println("update");
		Intent action = new Intent(context, TorchWidget.class);
		action.setAction(ACTION_SWITCH_TORCH);

		PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context,0, action, 0);

		// creating widget view
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.torch);
		// setting onclick intent action
		remoteViews.setOnClickPendingIntent(R.id.torch, actionPendingIntent);

		boolean isOn = readTorchState(context);
		initWidgetView(isOn, remoteViews);
		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		System.out.println("receive");
		if (ACTION_SWITCH_TORCH.equals(action)) {
			Device device = null;
			String name = readTorchDevice(context);
			try {
				if (name != null) device = Device.valueOf(name);
			}
			catch (IllegalArgumentException e){
				e.printStackTrace();
			}
			if (device == null)	
				selectTorchDevice(context);
			else 
				switchTorch(context,device);
		}
		super.onReceive(context, intent);
	}

	private void switchTorch(Context context, Device device) {
		boolean isOn = readTorchState(context);
		LinuxShell.execute("echo " + (isOn ? 0 : device.getBrightness()) + " > " + device.getTorch());
		saveTorchState(context,!isOn);

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
		new TorchDevice(new OnTaskCompletedListener<List<Device>, Void, Device>() {
			@Override
			public void onTaskCompleted(AsyncTask<List<Device>, Void, Device> task) {
				Device device = null;
				try {
					device = task.get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				if (device != null){
					saveTorchDevice(context,device.name());
					switchTorch(context,device);
				}
				else {
					Toast.makeText(context, "Wrong flash device", Toast.LENGTH_SHORT).show();
				}
				lock = false;
			}
		}).execute(TORCH_DEVICES);
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
	 * @param context
	 * @return
	 */
	private boolean readTorchState(Context context){
		SharedPreferences sharedPrefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		return sharedPrefs.getBoolean(PREF_TORCH_STATE, false);
	}

	private String readTorchDevice(Context context){
		SharedPreferences sharedPrefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		return sharedPrefs.getString(PREF_TORCH_DEVICE, null);
	}
	
	/**
	 * 
	 * @param context
	 * @param state
	 */
	private void saveTorchState(Context context, boolean state){
		SharedPreferences sharedPrefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		Editor editor = sharedPrefs.edit();
		editor.putBoolean(PREF_TORCH_STATE, state);
		editor.commit();
	}
	
	/**
	 * 
	 * @param context
	 * @param state
	 */
	private void saveTorchDevice(Context context, String device){
		SharedPreferences sharedPrefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		Editor editor = sharedPrefs.edit();
		editor.putString(PREF_TORCH_DEVICE, device);
		editor.commit();
	}

}