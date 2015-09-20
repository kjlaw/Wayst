package com.champions.wayst;

import android.util.Log;

import java.io.IOException;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.utils.Async;

public class SparkComm {

    private static final String TAG = SparkComm.class.getSimpleName();

    private static boolean initialized = false;
    private static ParticleDevice mDevice;

    public enum Cmd {
        TURNLEFT("turnLeft"),
        TURNRIGHT("turnRight"),
        DESTREACHED("destReached"),
        NAVON("navOn"),
        NAVOFF("navOff");

        public final String desc;

        Cmd(String desc) {
            this.desc = desc;
        }
        @Override
        public String toString() {
            return desc;
        }

    }

    public static void init() {
        final String email = "samvitmonga@gmail.com";
        final String password = "uwaterloo";

        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Integer>() {
            @Override
            public Integer callApi(ParticleCloud sparkCloud) throws ParticleCloudException, IOException {
                Log.d(TAG, "callApi");
                sparkCloud.logIn(email, password);
                sparkCloud.getDevices();
                mDevice = sparkCloud.getDevice("51ff6c065067545714240187");
                Integer variable;
                try {
                    variable = mDevice.getVariable("testValue");
                } catch (ParticleDevice.VariableDoesNotExistException e) {
                    Log.d(TAG, "Error reading variable");
                    variable = -1;
                }
                return variable;
            }

            @Override
            public void onSuccess(Integer value) {
                Log.d(TAG, "Logged in");
                setInitialized(true);
            }

            @Override
            public void onFailure(ParticleCloudException e) {
                setInitialized(false);
                Log.d(TAG, e.getBestMessage());
                e.printStackTrace();
            }
        });
    }

    public static boolean isInitialized() {
        return initialized;
    }

    private static void setInitialized(boolean enable) { initialized = enable; }

    public static void callFunc(final Cmd cmd) {
        Async.executeAsync(mDevice, new Async.ApiWork<ParticleDevice, Integer>() {

            public Integer callApi(ParticleDevice particleDevice) throws ParticleCloudException, IOException {
                Log.d(TAG, "callFunc callApi");
                try {
                    return particleDevice.callFunction(cmd.toString());
                } catch (ParticleDevice.FunctionDoesNotExistException e) {
                    e.printStackTrace();
                }
                return -1;
            }

            @Override
            public void onSuccess(Integer returnValue) {
                Log.d(TAG, "callFunc onSuccess");
            }

            @Override
            public void onFailure(ParticleCloudException e) {
                Log.d(TAG, "callFunc onFailure");
            }
        });
    }
}
