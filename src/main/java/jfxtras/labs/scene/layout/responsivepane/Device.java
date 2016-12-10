package jfxtras.labs.scene.layout.responsivepane;

public class Device extends Size {

	/**
	 * 
	 * @param device
	 */
	Device(String device) {
		this.device = device;
	}
	final String device;
	
	// ========================================================================================================================================================================================================
	// Actual relevant size
	
	/**
	 * Convert the width to inches
	 * @param responsivePane 
	 * @return
	 */
	double toInches(ResponsivePane responsivePane) {
		size = responsivePane.getDeviceSize(device);
		return size.toInches(responsivePane);
	}
	private Size size; // for logging in toString

	
	// ========================================================================================================================================================================================================
	// CONVENIENCE
	
	static public Device size(DeviceType v) {
		return new Device(v.toString());
	}
	
	static public Device size(String v) {
		return new Device(v);
	}
	
	
	// ========================================================================================================================================================================================================
	// FXML
	
	/**
	 * @param s
	 * @return
	 */
	static public Device valueOf(String s) {
		return size(s);
	}
	
	
	// ========================================================================================================================================================================================================
	// SUPPORT
	
	public String toString() {
		return size + " (" + device + ")";
	}
}
