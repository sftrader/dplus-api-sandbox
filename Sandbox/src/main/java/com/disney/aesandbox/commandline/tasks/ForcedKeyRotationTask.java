package com.disney.aesandbox.commandline.tasks;

import com.disney.aesandbox.commandline.CommandLineTask;
import com.disney.aesandbox.keymgmt.DemoOnlyTransientKeyManager;

public class ForcedKeyRotationTask extends CommandLineTask {

    public ForcedKeyRotationTask() {
        super("Force a key rotation, which will cause a different key to be used for signing.");
    }

    @Override
    public void run() {
        DemoOnlyTransientKeyManager.INSTANCE.rotateKeys();
    }
}
