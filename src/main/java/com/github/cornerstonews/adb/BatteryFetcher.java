package com.github.cornerstonews.adb;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.MultiLineReceiver;
import com.google.common.util.concurrent.SettableFuture;

public class BatteryFetcher {
    private static final Logger LOG = LogManager.getLogger(BatteryFetcher.class);

    private static final Pattern BATTERY_HEALTH_LEVEL = Pattern.compile("\\s*health: (\\d+)");
    private static final Pattern BATTERY_TEMP_LEVEL = Pattern.compile("\\s*temperature: (\\d+)");

    private final IDevice mDevice;
    private long mLastSuccessTime;
    private Integer mBatteryHealthLevel;
    private Integer mBatteryTempLevel;
    private SettableFuture<String> mPendingHealthRequest;
    private SettableFuture<Double> mPendingTempRequest;

    public BatteryFetcher(IDevice device) {
        mDevice = device;
    }

    public synchronized Future<String> getBatteryHealth(long freshness, TimeUnit timeUnit) {
        SettableFuture<String> result;
        if (mBatteryHealthLevel == null || isFetchRequired(freshness, timeUnit)) {
            if (mPendingHealthRequest == null) {
                // no request underway - start a new one
                mPendingHealthRequest = SettableFuture.create();
                initiateBatteryQuery();
            } else {
                // fall through - return the already created future from the request already
                // underway
            }
            result = mPendingHealthRequest;
        } else {
            // cache is populated within desired freshness
            result = SettableFuture.create();
            result.set(getBatteryHealthString());
        }
        return result;
    }

    public synchronized Future<Double> getBatteryTemperature(long freshness, TimeUnit timeUnit) {
        SettableFuture<Double> result;
        if (mBatteryTempLevel == null || isFetchRequired(freshness, timeUnit)) {
            if (mPendingTempRequest == null) {
                // no request underway - start a new one
                mPendingTempRequest = SettableFuture.create();
                initiateBatteryQuery();
            } else {
                // fall through - return the already created future from the request already
                // underway
            }
            result = mPendingTempRequest;
        } else {
            // cache is populated within desired freshness
            result = SettableFuture.create();
            result.set(getConvertedBatteryTemperature());
        }
        return result;
    }

    private String getBatteryHealthString() {
        String health;
        switch (mBatteryHealthLevel) {
        case 2:
            health = "GOOD";
            break;

        case 3:
            health = "OVERHEAT";
            break;

        case 4:
            health = "DEAD";
            break;

        case 5:
            health = "OVER_VOLTAGE";
            break;

        case 6:
            health = "UNSPECIFIED_FAILURE";
            break;

        case 7:
            health = "COLD";
            break;

        case 1:
        default:
            health = "UNKNOWN";
        }

        return health;
    }

    private Double getConvertedBatteryTemperature() {
        // https://android.googlesource.com/platform/tools/tradefederation/+/refs/heads/master/src/com/android/tradefed/device/BatteryTemperature.java
        return mBatteryTempLevel / 10.0;
    }

    private boolean isFetchRequired(long freshness, TimeUnit timeUnit) {
        long freshnessMs = timeUnit.toMillis(freshness);
        return (System.currentTimeMillis() - mLastSuccessTime) > freshnessMs;
    }

    private void initiateBatteryQuery() {
        Thread fetchThread = new Thread() {
            @Override
            public void run() {
                Throwable exception;
                try {
                    // first try to get it from sysfs
                    SysFsBatteryHealthReceiver sysBattHealthReceiver = new SysFsBatteryHealthReceiver();
                    mDevice.executeShellCommand("cat /sys/class/power_supply/battery/health", sysBattHealthReceiver, 2000, TimeUnit.MILLISECONDS);
                    boolean batteryHealthReceived = setBatteryHealthLevel(sysBattHealthReceiver.getBatteryHealthLevel());
                    if (!batteryHealthReceived) {
                        // failed! try dumpsys
                        DumpSysBatteryReceiver healthReceiver = new DumpSysBatteryReceiver(BATTERY_HEALTH_LEVEL);
                        mDevice.executeShellCommand("dumpsys battery", healthReceiver, 2000, TimeUnit.MILLISECONDS);
                        batteryHealthReceived = setBatteryHealthLevel(healthReceiver.getBatteryLevel());

                    }

                    // Get temp from sysfs
                    SysFsBatteryTempReceiver sysBattTempReceiver = new SysFsBatteryTempReceiver();
                    mDevice.executeShellCommand("cat /sys/class/power_supply/battery/temp", sysBattHealthReceiver, 2000, TimeUnit.MILLISECONDS);
                    boolean batteryTempReceived = setBatteryTempLevel(sysBattTempReceiver.getBatteryTempLevel());
                    if (!batteryTempReceived) {
                        // failed! try dumpsys
                        DumpSysBatteryReceiver tempReceiver = new DumpSysBatteryReceiver(BATTERY_TEMP_LEVEL);
                        mDevice.executeShellCommand("dumpsys battery", tempReceiver, 2000, TimeUnit.MILLISECONDS);
                        batteryTempReceived = setBatteryTempLevel(tempReceiver.getBatteryLevel());

                    }

                    if (batteryHealthReceived && batteryTempReceived) {
                        return;
                    }

                    exception = new IOException("Unrecognized response to battery level queries");
                } catch (Throwable e) {
                    exception = e;
                }
                handleBatteryLevelFailure(exception);
            }
        };
        fetchThread.setDaemon(true);
        fetchThread.start();
    }

    private synchronized boolean setBatteryHealthLevel(Integer batteryHealthLevel) {
        if (batteryHealthLevel == null) {
            return false;
        }
        mLastSuccessTime = System.currentTimeMillis();
        mBatteryHealthLevel = batteryHealthLevel;
        if (mPendingHealthRequest != null) {
            mPendingHealthRequest.set(getBatteryHealthString());
        }
        mPendingHealthRequest = null;
        return true;
    }

    private synchronized boolean setBatteryTempLevel(Integer batteryTempLevel) {
        if (batteryTempLevel == null) {
            return false;
        }
        mLastSuccessTime = System.currentTimeMillis();
        mBatteryTempLevel = batteryTempLevel;
        if (mPendingTempRequest != null) {
            mPendingTempRequest.set(getConvertedBatteryTemperature());
        }
        mPendingTempRequest = null;
        return true;
    }

    static final class SysFsBatteryHealthReceiver extends MultiLineReceiver {
        private static final Pattern BATTERY_HEALTH_LEVEL = Pattern.compile("^(\\d+)[.\\s]*");
        private Integer mBatteryHealthLevel;

        public Integer getBatteryHealthLevel() {
            return mBatteryHealthLevel;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public void processNewLines(@NonNull String[] lines) {
            Arrays.stream(lines).forEach(line -> {
                Matcher batteryMatch = BATTERY_HEALTH_LEVEL.matcher(line);
                if (batteryMatch.matches() && mBatteryHealthLevel == null) {
                    mBatteryHealthLevel = Integer.parseInt(batteryMatch.group(1));
                }
            });
        }
    }

    static final class SysFsBatteryTempReceiver extends MultiLineReceiver {
        private static final Pattern BATTERY_TEMP_LEVEL = Pattern.compile("^(\\d+)[.\\s]*");
        private Integer mBatteryTempLevel;

        public Integer getBatteryTempLevel() {
            return mBatteryTempLevel;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public void processNewLines(@NonNull String[] lines) {
            Arrays.stream(lines).forEach(line -> {
                Matcher batteryMatch = BATTERY_TEMP_LEVEL.matcher(line);
                if (batteryMatch.matches() && mBatteryTempLevel == null) {
                    mBatteryTempLevel = Integer.parseInt(batteryMatch.group(1));
                }
            });
        }
    }

    private final class DumpSysBatteryReceiver extends MultiLineReceiver {
        private Integer mBatteryLevel;
        private Pattern matcher;

        public DumpSysBatteryReceiver(Pattern matcher) {
            this.matcher = matcher;
        }

        /**
         * Returns the parsed percent battery level, or null if not available.
         */
        public @Nullable Integer getBatteryLevel() {
            return mBatteryLevel;
        }

        @Override
        public void processNewLines(@NonNull String[] lines) {
            Arrays.stream(lines).forEach(line -> {
                Matcher batteryMatch = matcher.matcher(line);
                if (batteryMatch.matches()) {
                    mBatteryLevel = Integer.parseInt(batteryMatch.group(1));
                }
            });
        }

        @Override
        public boolean isCancelled() {
            return false;
        }
    }

    private synchronized void handleBatteryLevelFailure(Throwable e) {
        LOG.warn("'{}' getting battery level for device. Error: '{}'", e.getClass().getSimpleName(), e.getMessage());
        LOG.trace("'{}' getting battery level for device: '{}'", e.getClass().getSimpleName(), mDevice.getSerialNumber(), e);
        if (mPendingHealthRequest != null) {
            if (!mPendingHealthRequest.setException(e)) {
                // should never happen
                LOG.error("Future.setException failed");
                mPendingHealthRequest.set(null);
            }
        }
        mPendingHealthRequest = null;

        if (mPendingTempRequest != null) {
            if (!mPendingTempRequest.setException(e)) {
                // should never happen
                LOG.error("Future.setException failed");
                mPendingTempRequest.set(null);
            }
        }
        mPendingTempRequest = null;
    }
}
