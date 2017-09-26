package util;

import com.amazon.speech.Sdk;
import com.google.common.base.Strings;

import java.time.ZoneOffset;
import java.util.TimeZone;

import static com.amazon.speech.Sdk.*;

/**
 * Basic Utils Methods of Application.
 */
public class App {

    private static final String APP_IDS_DEFAULT = "amzn1.ask.skill.3a540a25-ab37-4ab6-a82c-55670933f866";
    private static final String DISABLE_SIGNATURE_DEFAULT = Boolean.FALSE.toString();
    private static final String TIMESTAMP_TOLERANCE_DEFAULT = "400";

    public static void prepareSystemProperties() {
        if (Strings.isNullOrEmpty(System.getProperty(Sdk.DISABLE_REQUEST_SIGNATURE_CHECK_SYSTEM_PROPERTY))) {
            System.setProperty(DISABLE_REQUEST_SIGNATURE_CHECK_SYSTEM_PROPERTY, DISABLE_SIGNATURE_DEFAULT);
        }
        if (Strings.isNullOrEmpty(System.getProperty(Sdk.TIMESTAMP_TOLERANCE_SYSTEM_PROPERTY))) {
            System.setProperty(TIMESTAMP_TOLERANCE_SYSTEM_PROPERTY, TIMESTAMP_TOLERANCE_DEFAULT);
        }
        if (Strings.isNullOrEmpty(System.getProperty(Sdk.SUPPORTED_APPLICATION_IDS_SYSTEM_PROPERTY))) {
            System.setProperty(SUPPORTED_APPLICATION_IDS_SYSTEM_PROPERTY, APP_IDS_DEFAULT);
        }
        // set Amazon timezone for correct checking timestamp
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
    }
}