package com.disney.aesandbox.commandline.tasks;

import java.util.concurrent.TimeUnit;

public class ActivationTokenTaskThirtyDayExp extends SampleActivationTokenTask {

    public ActivationTokenTaskThirtyDayExp() {
        super(TimeUnit.DAYS.toSeconds(30), "Create Sample Activation Token with 30-day Expiration using an internally-generated private key for signing.");
    }
}
