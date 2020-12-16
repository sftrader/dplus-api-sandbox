package com.disney.aesandbox.commandline;

public abstract class CommandLineTask implements Runnable {

    private String prompt;

    public static final CommandLineTask EXIT_TASK = new CommandLineTask() {
        @Override
        public void run() {
            System.exit(1);
        }
    };

    private CommandLineTask() {
        prompt = "Exit";
    }

    protected CommandLineTask(String prompt) {
        this.prompt = prompt;
    }

    public abstract void run();

    public String getPrompt() {
        return prompt;
    }

    public void printDescription() {}

    public void innerREPL() {}
}
