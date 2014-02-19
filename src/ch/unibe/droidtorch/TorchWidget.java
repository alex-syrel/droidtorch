package ch.unibe.droidtorch;

import java.io.BufferedReader;
import java.io.IOException;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.RemoteViews;
import android.widget.Toast;

public class TorchWidget extends AppWidgetProvider {

	public static final String ACTION_SWITCH_TORCH = "actionSwitchTorch";
	public static final String PREF_TORCH_STATE = "torch_state";
	public static final String FLASH_DEVICE = "/sys/class/camera/flash/rear_flash";				// samsung galaxy
	//public static final String FLASH_DEVICE = "/sys/class/leds/led:flash_torch/brightness";			// nexus 5

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,int[] appWidgetIds) {

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

		if (ACTION_SWITCH_TORCH.equals(action)) {
			boolean isOn = readTorchState(context);
			if (isFileExists(FLASH_DEVICE)){
				LinuxShell.execute("echo " + (isOn ? 0 : 200) + " > " + FLASH_DEVICE);
				saveTorchState(context,!isOn);

				// updating widget status image
				ComponentName thisWidget = new ComponentName(context.getApplicationContext(),TorchWidget.class);
				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
				RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.torch);
				int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

				initWidgetView(!isOn, remoteViews);
				appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
			}
			else {
				Toast.makeText(context, "Wrong flash device", Toast.LENGTH_SHORT).show();
			}
		}
		super.onReceive(context, intent);
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
	 * Returns {@code true} if file {@code filename} exists
	 * in filesystem, {@code false} otherwise.
	 * 
	 * @param filepath - file location
	 * @return true if file exists, false otherwise
	 */
	private boolean isFileExists(String filepath){
		BufferedReader reader = LinuxShell.execute("if test -w "+filepath+"; then echo \"true\"; else echo \"false\"; fi");
		boolean exists = false;
		try {
			exists = Boolean.valueOf(reader.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return exists;
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

}