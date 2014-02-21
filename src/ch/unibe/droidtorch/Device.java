package ch.unibe.droidtorch;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

enum Device {
	SAMSUNG_GALAXY_S4("/sys/class/camera/flash/rear_flash",1),
	NEXUS_5("/sys/class/leds/led:flash_torch/brightness",200),
	NULL("null",0);

	public static final String PREF_TORCH_STATE = "torch_state";
	public static final String PREF_TORCH_DEVICE = "torch_device";
	
	private final String torch;
	private final int brightness;
	
	private SharedPreferences sharedPrefs;

	public static Device loadDevice(Context context){
		return valueOf(readTorchDevice(context));
	}
	
	public static void saveDevice(Context context, Device device){
		SharedPreferences sharedPrefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		Editor editor = sharedPrefs.edit();
		editor.putString(PREF_TORCH_DEVICE, device.name());
		editor.commit();
	}
	
	private Device(String torch, int brightness){
		this.torch = torch;
		this.brightness = brightness;
	}

	public String getTorch(){
		return torch;
	}

	public int getBrightness(){
		return brightness;
	}
	
	public void turnOn(Context context){
		if (isNull()) throw new UnsupportedOperationException("Null torch can't be turned on");
		setBrightness(context,brightness);
	}
	
	public void turnOff(Context context){
		if (isNull()) throw new UnsupportedOperationException("Null torch can't be turned off");
		setBrightness(context,0);
	}
	
	public boolean isOn(Context context){
		return readBrightness(context) > 0;
	}
	
	public boolean isNull(){
		return this == NULL;
	}
	
	private void setBrightness(Context context,int brightness){
		assert !isNull();
		LinuxShell.execute("echo " + brightness + " > " + torch);
		saveBrightness(context, brightness);
	}
	
	private static String readTorchDevice(Context context){
		SharedPreferences sharedPrefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		return sharedPrefs.getString(PREF_TORCH_DEVICE, NULL.name());
	}
	
	/**
	 * Saves current torch state in {@code SharedPreferences} with {@code PREF_TORCH_STATE} key
	 * 
	 * @param context - {@code Context} object holding widget context
	 * @param state - {@code boolean} state of torch, true if on, false if off
	 */
	private void saveBrightness(Context context, int brightness){
		if (sharedPrefs == null)
			sharedPrefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		Editor editor = sharedPrefs.edit();
		editor.putInt(PREF_TORCH_STATE, brightness);
		editor.commit();
	}
	
	/**
	 * 
	 * @param context
	 * @return
	 */
	private int readBrightness(Context context){
		if (sharedPrefs == null)
			sharedPrefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		return sharedPrefs.getInt(PREF_TORCH_STATE, 0);
	}
}