package com.disney.aesandbox.commandline.tasks;

import com.disney.aesandbox.commandline.CommandLineTask;
import com.disney.aesandbox.keymgmt.DemoOnlyTransientKeyManager;

import java.io.Console;

public class SampleJWKSTask extends CommandLineTask {

    public SampleJWKSTask() {
        super("Show sample JWKS");
    }

    @Override
    public void run() {
        Console cons = System.console();
        cons.writer().println(DemoOnlyTransientKeyManager.INSTANCE.getPrettyPrintedJsonPublicKeySet());
        cons.writer().println("\nThis set is also available via the HTTP endpoint.\n");
    }
}
