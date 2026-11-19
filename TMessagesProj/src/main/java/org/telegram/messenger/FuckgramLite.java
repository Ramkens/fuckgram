package org.telegram.messenger;

/** One switch that turns off what a weak phone cannot afford: heavy animations, blur, video backgrounds. */
public class FuckgramLite {

    public static boolean isOn() {
        return FuckgramConfig.get(FuckgramConfig.LITE);
    }

    public static void apply(boolean on) {
        FuckgramConfig.set(FuckgramConfig.LITE, on);
        if (on) {
            FuckgramConfig.setInt("lite_prev", LiteMode.getValue(true));
            LiteMode.setAllFlags(LiteMode.PRESET_LOW);
            FuckgramConfig.setInt(FuckgramConfig.TRANSITION, 1);
            FuckgramConfig.setInt(FuckgramConfig.TRANSITION_SPEED, 1);
        } else {
            int prev = FuckgramConfig.getInt("lite_prev");
            LiteMode.setAllFlags(prev != 0 ? prev : LiteMode.PRESET_HIGH);
            FuckgramConfig.setInt(FuckgramConfig.TRANSITION, 0);
            FuckgramConfig.setInt(FuckgramConfig.TRANSITION_SPEED, 2);
        }
    }

    /** On the very first start of a weak phone the mode is switched on once; the user can turn it off. */
    public static void autoOnce() {
        try {
            if (FuckgramConfig.getInt("lite_asked") == 0) {
                FuckgramConfig.setInt("lite_asked", 1);
                if (SharedConfig.getDevicePerformanceClass() == SharedConfig.PERFORMANCE_CLASS_LOW) {
                    apply(true);
                }
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }
}
