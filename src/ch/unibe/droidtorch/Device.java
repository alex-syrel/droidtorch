package ch.unibe.droidtorch;

enum Device {
	SAMSUNG_GALAXY_S4("/sys/class/camera/flash/rear_flash",1),
	NEXUS_5("/sys/class/leds/led:flash_torch/brightness",200);

	private final String torch;
	private final int brightness;

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
}