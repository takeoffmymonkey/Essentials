
package com.example.android.essentials;


import java.util.concurrent.TimeUnit;

/**
 * Created by takeoff on 027 27 Jul 17.
 */

public class Schedule {

    static final long LEVEL_0 = 0;
    static final long LEVEL_1 = TimeUnit.MINUTES.toMillis(20);
    static final long LEVEL_2 = TimeUnit.HOURS.toMillis(24);
    static final long LEVEL_3 = TimeUnit.DAYS.toMillis(14);
    static final long LEVEL_4 = TimeUnit.DAYS.toMillis(60);

/*
    static final long LEVEL_1 = 20000;
    static final long LEVEL_2 = 30000;
    static final long LEVEL_3 = 60000;
    static final long LEVEL_4 = 180000;
    static final long LEVEL_0 = 0;
*/


    public static long getDelayByLevel(int level) {

        long delay;
        switch (level) {
            case 0: {
                delay = Schedule.LEVEL_0;
                break;
            }
            case 1: {
                delay = Schedule.LEVEL_1;
                break;
            }
            case 2: {
                delay = Schedule.LEVEL_2;
                break;
            }
            case 3: {
                delay = Schedule.LEVEL_3;
                break;
            }
            case 4: {
                delay = Schedule.LEVEL_4;
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
