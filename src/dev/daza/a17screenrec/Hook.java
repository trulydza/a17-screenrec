package dev.daza.a17screenrec;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Samsung omitted SEC_FLOATING_FEATURE_FRAMEWORK_SUPPORT_SCREEN_RECORDER on the A17,
 * so TileFeatureChecker hides RecordScreenTile. Report that one flag as true.
 */
public class Hook implements IXposedHookLoadPackage {
    private static final String TAG = "A17ScreenRec";
    private static final String KEY = "SEC_FLOATING_FEATURE_FRAMEWORK_SUPPORT_SCREEN_RECORDER";
    private static boolean logged = false;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        String pkg = lpparam.packageName;
        if (!"com.android.systemui".equals(pkg)
                && !"com.samsung.android.app.smartcapture".equals(pkg)) {
            return;
        }

        final Class<?> feature;
        try {
            feature = XposedHelpers.findClass(
                    "com.samsung.android.feature.SemFloatingFeature", lpparam.classLoader);
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": SemFloatingFeature not found in " + pkg + ": " + t);
            return;
        }

        XC_MethodHook hook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args.length > 0 && KEY.equals(param.args[0])) {
                    param.setResult(Boolean.TRUE);
                    if (!logged) {
                        logged = true;
                        XposedBridge.log(TAG + ": forced " + KEY + "=true");
                    }
                }
            }
        };

        // getBoolean(String) and getBoolean(String, boolean): hook whichever exist.
        try {
            XposedHelpers.findAndHookMethod(feature, "getBoolean", String.class, hook);
            XposedBridge.log(TAG + ": hooked getBoolean(String) in " + pkg);
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": getBoolean(String) hook failed in " + pkg + ": " + t);
        }
        try {
            XposedHelpers.findAndHookMethod(feature, "getBoolean", String.class, boolean.class, hook);
            XposedBridge.log(TAG + ": hooked getBoolean(String,boolean) in " + pkg);
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": getBoolean(String,boolean) hook skipped in " + pkg + ": " + t);
        }
    }
}
