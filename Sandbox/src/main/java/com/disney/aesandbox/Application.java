package com.disney.aesandbox;

import com.disney.aesandbox.commandline.CommandLine;
import com.disney.aesandbox.commandline.CommandLineTask;
import com.disney.aesandbox.commandline.tasks.*;
import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.Micronaut;

import java.util.ArrayList;
import java.util.List;

public class Application {

    public static void main(String[] args) {
        ApplicationContext ctx = Micronaut.run(Application.class);
        List<CommandLineTask> tasks = makeTaskList(ctx);
        CommandLine cl = new CommandLine(tasks);
        cl.start();
    }

    private static List<CommandLineTask> makeTaskList(ApplicationContext ctx) {

        List<CommandLineTask> tasks = new ArrayList<>();

        tasks.add(new ActivationTokenTaskOneHourExp());
        tasks.add(new ActivationTokenTaskThirtyDayExp());
        tasks.add(new SampleEntitlementTokenTask());
        tasks.add(new ValidateActivationLinkTask());
        tasks.add(new ValidateGetEntitlementTokenTask());
        tasks.add(new ValidateSetEntitlementTokenTask());
        tasks.add(new SampleJWKSTask());
        tasks.add(new ForcedKeyRotationTask());
        tasks.add(new KeyRotationDemoTask());
        tasks.add(new KeyRotationTestTask());

        return tasks;
    }
}
