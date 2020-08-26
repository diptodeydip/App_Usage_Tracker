package com.example.app_usage_tracker;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import java.util.List;

import androidx.appcompat.app.AlertDialog;

public class AutoStartHelper {

    /***
     * Samsung
     */
    private final String BRAND_SAMSUNG = "samsung";
    private String PACKAGE_SAMSUNG_MAIN = "com.samsung.android.lool";
    private String PACKAGE_SAMSUNG_COMPONENT = "com.samsung.android.sm.ui.battery.BatteryActivity";

    /***
     * Xiaomi
     */
    private final String BRAND_XIAOMI = "xiaomi";
    private String PACKAGE_XIAOMI_MAIN = "com.miui.securitycenter";
    private String PACKAGE_XIAOMI_COMPONENT = "com.miui.permcenter.autostart.AutoStartManagementActivity";

    /***
     * Letv
     */
    private final String BRAND_LETV = "letv";
    private String PACKAGE_LETV_MAIN = "com.letv.android.letvsafe";
    private String PACKAGE_LETV_COMPONENT = "com.letv.android.letvsafe.AutobootManageActivity";

    /***
     * ASUS ROG
     */
    private final String BRAND_ASUS = "asus";
    private String PACKAGE_ASUS_MAIN = "com.asus.mobilemanager";
    private String PACKAGE_ASUS_COMPONENT = "com.asus.mobilemanager.powersaver.PowerSaverSettings";

    /***
     * Honor
     */
    private final String BRAND_HONOR = "honor";
    private String PACKAGE_HONOR_MAIN = "com.huawei.systemmanager";
    private String PACKAGE_HONOR_COMPONENT = "com.huawei.systemmanager.optimize.process.ProtectActivity";

    /**
     * Oppo
     */
    private final String BRAND_OPPO = "oppo";
    private String PACKAGE_OPPO_MAIN = "com.coloros.safecenter";
    private String PACKAGE_OPPO_FALLBACK = "com.oppo.safe";
    private String PACKAGE_OPPO_COMPONENT = "com.coloros.safecenter.permission.startup.StartupAppListActivity";
    private String PACKAGE_OPPO_COMPONENT_FALLBACK = "com.oppo.safe.permission.startup.StartupAppListActivity";
    private String PACKAGE_OPPO_COMPONENT_FALLBACK_A = "com.coloros.safecenter.startupapp.StartupAppListActivity";

    /**
     * Vivo
     */

    private final String BRAND_VIVO = "vivo";
    private String PACKAGE_VIVO_MAIN = "com.iqoo.secure";
    private String PACKAGE_VIVO_FALLBACK = "com.vivo.perm;issionmanager";
    private String PACKAGE_VIVO_COMPONENT = "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity";
    private String PACKAGE_VIVO_COMPONENT_FALLBACK = "com.vivo.permissionmanager.activity.BgStartUpManagerActivity";
    private String PACKAGE_VIVO_COMPONENT_FALLBACK_A = "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager";

    /**
     * Nokia
     */

    private final String BRAND_NOKIA = "nokia";
    private String PACKAGE_NOKIA_MAIN = "com.evenwell.powersaving.g3";
    private String PACKAGE_NOKIA_COMPONENT = "com.evenwell.powersaving.g3.exception.PowerSaverExceptionActivity";


    private AutoStartHelper() {
    }

    public static AutoStartHelper getInstance() {
        return new AutoStartHelper();
    }


    public DialogInterface.OnClickListener getAutoStartPermissionIntent(Context context) {

        String build_info = Build.BRAND.toLowerCase();
        switch (build_info) {
            case BRAND_SAMSUNG:
                return autoStartSamsung(context);
            case BRAND_ASUS:
                return autoStartAsus(context);
            case BRAND_XIAOMI:
                return autoStartXiaomi(context);
            case BRAND_LETV:
                return autoStartLetv(context);
            case BRAND_HONOR:
                return autoStartHonor(context);
            case BRAND_OPPO:
                return autoStartOppo(context);
            case BRAND_VIVO:
                return autoStartVivo(context);
            case BRAND_NOKIA:
                return autoStartNokia(context);
            default:
                return general(context);
        }

    }

    private DialogInterface.OnClickListener general(final Context context){

        return (dialog, which) -> {
            try
            {
                //Open the specific App Info page:
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                context.startActivity(intent);
            }
            catch ( ActivityNotFoundException e )
            {
                //Open the generic Apps page:
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                context.startActivity(intent);
            }
        };
    }

    private DialogInterface.OnClickListener autoStartAsus(final Context context) {
        if (isPackageExists(context, PACKAGE_ASUS_MAIN)) {
            return (dialog, which) -> {

                try {
                    // PrefUtil.writeBoolean(context, PrefUtil.PREF_KEY_APP_AUTO_START, true);
                    startIntent(context, PACKAGE_ASUS_MAIN, PACKAGE_ASUS_COMPONENT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            };

        }
        return general(context);
    }

    private DialogInterface.OnClickListener autoStartSamsung(final Context context) {
        if (isPackageExists(context, PACKAGE_SAMSUNG_MAIN)) {
            return (dialog, which) -> {

                try {
                    // PrefUtil.writeBoolean(context, PrefUtil.PREF_KEY_APP_AUTO_START, true);
                    startIntent(context, PACKAGE_SAMSUNG_MAIN, PACKAGE_SAMSUNG_COMPONENT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            };

        }

        return general(context);

    }

    private DialogInterface.OnClickListener autoStartXiaomi(final Context context) {
        if (isPackageExists(context, PACKAGE_XIAOMI_MAIN)) {
            return (dialog, which) -> {

                try {
                    // PrefUtil.writeBoolean(context, PrefUtil.PREF_KEY_APP_AUTO_START, true);
                    startIntent(context, PACKAGE_XIAOMI_MAIN, PACKAGE_XIAOMI_COMPONENT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };


        }
        return general(context);
    }

    private DialogInterface.OnClickListener autoStartLetv(final Context context) {
        if (isPackageExists(context, PACKAGE_LETV_MAIN)) {
            return (dialog, which) -> {

                try {
                   // PrefUtil.writeBoolean(context, PrefUtil.PREF_KEY_APP_AUTO_START, true);
                    startIntent(context, PACKAGE_LETV_MAIN, PACKAGE_LETV_COMPONENT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };


        }
        return general(context);
    }


    private DialogInterface.OnClickListener autoStartHonor(final Context context) {
        if (isPackageExists(context, PACKAGE_HONOR_MAIN)) {
            return (dialog, which) -> {

                try {
                   // PrefUtil.writeBoolean(context, PrefUtil.PREF_KEY_APP_AUTO_START, true);
                    startIntent(context, PACKAGE_HONOR_MAIN, PACKAGE_HONOR_COMPONENT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };


        }
        return general(context);
    }

    private DialogInterface.OnClickListener autoStartOppo(final Context context) {
        if (isPackageExists(context, PACKAGE_OPPO_MAIN) || isPackageExists(context, PACKAGE_OPPO_FALLBACK)) {
            return (dialog, which) -> {

                try {
                   // PrefUtil.writeBoolean(context, PrefUtil.PREF_KEY_APP_AUTO_START, true);
                    startIntent(context, PACKAGE_OPPO_MAIN, PACKAGE_OPPO_COMPONENT);
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        //PrefUtil.writeBoolean(context, PrefUtil.PREF_KEY_APP_AUTO_START, true);
                        startIntent(context, PACKAGE_OPPO_FALLBACK, PACKAGE_OPPO_COMPONENT_FALLBACK);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        try {
                           // PrefUtil.writeBoolean(context, PrefUtil.PREF_KEY_APP_AUTO_START, true);
                            startIntent(context, PACKAGE_OPPO_MAIN, PACKAGE_OPPO_COMPONENT_FALLBACK_A);
                        } catch (Exception exx) {
                            exx.printStackTrace();
                        }

                    }

                }
            };


        }
        return general(context);
    }

    private DialogInterface.OnClickListener autoStartVivo(final Context context) {
        if (isPackageExists(context, PACKAGE_VIVO_MAIN) || isPackageExists(context, PACKAGE_VIVO_FALLBACK)) {
            return (dialog, which) -> {

                try {
                    //PrefUtil.writeBoolean(context, PrefUtil.PREF_KEY_APP_AUTO_START, true);
                    startIntent(context, PACKAGE_VIVO_MAIN, PACKAGE_VIVO_COMPONENT);
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                       // PrefUtil.writeBoolean(context, PrefUtil.PREF_KEY_APP_AUTO_START, true);
                        startIntent(context, PACKAGE_VIVO_FALLBACK, PACKAGE_VIVO_COMPONENT_FALLBACK);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        try {
                            //PrefUtil.writeBoolean(context, PrefUtil.PREF_KEY_APP_AUTO_START, true);
                            startIntent(context, PACKAGE_VIVO_MAIN, PACKAGE_VIVO_COMPONENT_FALLBACK_A);
                        } catch (Exception exx) {
                            exx.printStackTrace();
                        }

                    }

                }

            };
        }
        return general(context);
    }

    private DialogInterface.OnClickListener autoStartNokia(final Context context) {
        if (isPackageExists(context, PACKAGE_NOKIA_MAIN)) {
            return (dialog, which) -> {

                try {
                    //PrefUtil.writeBoolean(context, PrefUtil.PREF_KEY_APP_AUTO_START, true);
                    startIntent(context, PACKAGE_NOKIA_MAIN, PACKAGE_NOKIA_COMPONENT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        }
        return general(context);
    }


    private void startIntent(Context context, String packageName, String componentName) throws Exception {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packageName, componentName));
            context.startActivity(intent);
        } catch (Exception var5) {
            var5.printStackTrace();
            throw var5;
        }
    }

    private Boolean isPackageExists(Context context, String targetPackage) {
        List<ApplicationInfo> packages;
        PackageManager pm = context.getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo :
                packages) {
            if (packageInfo.packageName.equals(targetPackage)) {
                return true;
            }
        }

        return false;
    }
}