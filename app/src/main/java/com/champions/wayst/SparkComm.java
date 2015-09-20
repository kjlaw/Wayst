package com.champions.wayst;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.utils.Async;


/**
 * Created by Digvijay on 15-09-19.
 */
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

    public static void init(Context context) {
        final String email = "samvitmonga@gmail.com";
        final String password = "uwaterloo";

        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Integer>() {
            @Override
            public Integer callApi(ParticleCloud sparkCloud) throws ParticleCloudException, IOException {
                sparkCloud.logIn(email, password);
                sparkCloud.getDevices();
                mDevice = sparkCloud.getDevice("51ff6c065067545714240187");
                Integer variable;
                try {
                    variable = mDevice.getVariable("testValue");
                } catch (ParticleDevice.VariableDoesNotExistException e) {
//                    Toaster.s(SparkComm.this, "Error reading variable");
                    Log.d(TAG, "Error reading variable");
                    variable = -1;
                }
                return variable;

            }

            @Override
            public void onSuccess(Integer value) {
                Log.d(TAG, "Logged in");
                setInitialized(true);
//                Toaster.l(SparkComm.this, "Logged in");
//                Intent intent = ValueActivity.buildIntent(LoginActivity.this, value, mDevice.getID());
//                startActivity(intent);
            }

            @Override
            public void onFailure(ParticleCloudException e) {
                setInitialized(false);
                Log.d(TAG, e.getBestMessage());
//                Toaster.l(SparkComm.this, e.getBestMessage());
                e.printStackTrace();
                Log.d("info", e.getBestMessage());
            }
        });
    }

    public static boolean isInitialized() {
        return initialized;
    }

    private static void setInitialized(boolean enable) { initialized = enable; }

    public static boolean callFunc(Cmd cmd) {
        if (initialized) {
            try {
                mDevice.callFunction(cmd.toString());
                return true;
            } catch (ParticleDevice.FunctionDoesNotExistException e) {
    //                    Toaster.s(SparkComm.this, "Error reading variable");
                Log.d(TAG, "Error finding function");
            } catch (ParticleCloudException e) {
                Log.d(TAG, "Not connected to Particle cloud");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
