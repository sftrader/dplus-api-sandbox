package com.disney.aesandbox.commandline.tasks;

import java.util.concurrent.TimeUnit;

public class ActivationTokenTaskOneHourExp extends SampleActivationTokenTask {

    public ActivationTokenTaskOneHourExp() {
        super(TimeUnit.HOURS.toSeconds(1), "Create Sample Activation Token with 1-hour Expiration using an internally-generated private key for signing.");
    }
}
