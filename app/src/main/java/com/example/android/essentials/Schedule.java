
package com.example.android.essentials;


import java.util.concurrent.TimeUnit;

/**
 * Created by takeoff on 027 27 Jul 17.
 */

public class Schedule {

    private static final long LEVEL_0 = 0;
    private static final long LEVEL_1 = TimeUnit.MINUTES.toMillis(20);
    private static final long LEVEL_2 = TimeUnit.HOURS.toMillis(24);
    private static final long LEVEL_3 = TimeUnit.DAYS.toMillis(14);
    private static final long LEVEL_4 = TimeUnit.DAYS.toMillis(60);

    private static final long LEVEL_1_TEST = 20000;
    private static final long LEVEL_2_TEST = 30000;
    private static final long LEVEL_3_TEST = 60000;
    private static final long LEVEL_4_TEST = 180000;


    public static long getDelayByLevel(int level) {

        long delay;
        switch (level) {
            case 0: {
                delay = Schedule.LEVEL_0;
                break;
            }
            case 1: {
                if (Settings.getNotificationMode() == 1) delay = Schedule.LEVEL_1_TEST;
                else delay = Schedule.LEVEL_1;
                break;
            }
            case 2: {
                if (Settings.getNotificationMode() == 1) delay = Schedule.LEVEL_2_TEST;
                else delay = Schedule.LEVEL_2;
                break;
            }
            case 3: {
                if (Settings.getNotificationMode() == 1) delay = Schedule.LEVEL_3_TEST;
                else delay = Schedule.LEVEL_3;
                break;
            }
            case 4: {
                if (Settings.getNotificationMode() == 1) delay = Schedule.LEVEL_4_TEST;
                else delay = Schedule.LEVEL_4;
                break;
            }
            default: {
                delay = Schedule.LEVEL_0;
                break;
            }
        }
        return delay;
    }

}
