package edu.ucla.nesl.rulemanager;

public class Const {
	public static final String TAG = "RuleManager";

	public static final String RULE_TAG = "rulemanager";

	public static final String GPS_DISTANCE_FUNCTION_NAME = "gps_distance";
	public static final String LOCATION_STREAM_NAME = "PhoneGPS";
	public static final String LATITUDE_CHANNEL_NAME = "ch1";
	public static final String LONGITUDE_CHANNEL_NAME = "ch2";

	public static final int REQUEST_CODE_NORMAL = 1;
	public static final int REQUEST_CODE_NEW_LABEL = 2;
	public static final int REQUEST_CODE_SETUP_HOME_LOCATION = 3;
	public static final int REQUEST_CODE_SETUP_WORK_LOCATION = 4;
	public static final int REQUEST_CODE_SETUP_WORK_TIME = 5;
	
	public static final String PREFS_NAME = "rulemanager_prefs";
	public static final String PREFS_IS_FIRST = "is_first";
	public static final String PREFS_USERNAME = "username";
	public static final String PREFS_PASSWORD = "password";
	public static final String PREFS_SERVER_IP = "server_ip";
	
	public static final String LABEL_TYPE_TIME = "time";
	public static final String LABEL_TYPE_LOCATION = "location";
	
	public static final String BUNDLE_KEY_ID = "id";
	public static final String BUNDLE_KEY_ACTION = "action";
	public static final String BUNDLE_KEY_SENSOR = "sensor";
	public static final String BUNDLE_KEY_CONSUMER = "consumer";
	public static final String BUNDLE_KEY_TIME_LABEL = "time_label";
	public static final String BUNDLE_KEY_LOCATION_LABEL = "location_label";
	
	public static final String BUNDLE_KEY_LABEL_TYPE = "label_type";
	public static final String BUNDLE_KEY_LABEL_NAME = "label_name";
	public static final String BUNDLE_KEY_IS_SETUP_LABEL = "is_setup_label";
	public static final String BUNDLE_KEY_LATITUDE = "latitude";
	public static final String BUNDLE_KEY_LONGITUDE = "longitude";
	public static final String BUNDLE_KEY_RADIUS = "radius";
	
	public static final String BUNDLE_KEY_FROM_DATE = "from_date";
	public static final String BUNDLE_KEY_FROM_TIME = "from_time";
	public static final String BUNDLE_KEY_TO_DATE = "to_date";
	public static final String BUNDLE_KEY_TO_TIME = "to_time";
	public static final String BUNDLE_KEY_IS_ALL_DAY = "is_all_day";
	public static final String BUNDLE_KEY_IS_REPEAT = "is_repeat";
	public static final String BUNDLE_KEY_REPEAT_TYPE = "repeat_type";
	public static final String BUNDLE_KEY_REPEAT_DAY = "repeat_day";
	public static final String BUNDLE_KEY_IS_REPEAT_MON = "is_repeat_mon";
	public static final String BUNDLE_KEY_IS_REPEAT_TUE = "is_repeat_tue";
	public static final String BUNDLE_KEY_IS_REPEAT_WED = "is_repeat_wed";
	public static final String BUNDLE_KEY_IS_REPEAT_THU = "is_repeat_thu";
	public static final String BUNDLE_KEY_IS_REPEAT_FRI = "is_repeat_fri";
	public static final String BUNDLE_KEY_IS_REPEAT_SAT = "is_repeat_sat";
	public static final String BUNDLE_KEY_IS_REPEAT_SUN = "is_repeat_sun";
	
	public static final String ACTION_ALLOW = "allow";
	public static final String ACTION_DENY = "deny";

	public static final String ALL = "all";
	public static final String EVERYONE = "everyone";
	public static final String ALL_TIME = "All time";
	public static final String ALL_LOCATIONS = "All locations";

	public static final String SHARE = "Share";
	public static final String NOT_SHARE = "Don't share";

	public static final String OTHER_TIME = "Other time";
	public static final String OTHER_LOCATIONS = "Other locations";

	public static final String LOCATION_LABEL_PREFIX = "location_";
	public static final String TIME_LABEL_PREFIX = "time_";
}
