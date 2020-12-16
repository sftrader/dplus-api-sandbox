package com.disney.aesandbox.commandline;

import java.io.Console;
import java.util.List;

public class CommandLine extends Thread {

    private List<CommandLineTask> tasks;
    private String prompt;

    public CommandLine(List<CommandLineTask> tasks) {
        this.tasks = tasks;
        tasks.add(CommandLineTask.EXIT_TASK);
        prompt = constructPrompt();
    }

    public void run() {

        boolean exit = false;
        Console cons = System.console();

        while (!exit) {
            cons.writer().print(prompt);
            cons.writer().flush();
            String input = cons.readLine();
            if (!isExit(input)) {
                try {
                    int task_index = Integer.parseInt(input);
                    if (task_index <= tasks.size()) {         // not zero-indexed...yet
                        tasks.get(task_index-1).run();          // now it is!

                    } else {
                        cons.writer().println("Error: not a valid option.");
                    }
                } catch (NumberFormatException nfe) {
                    cons.writer().println("Error: not a number.");
                }
            } else {
                exit = true;
            }
        }

        System.exit(0);
    }

    private String constructPrompt() {
        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (CommandLineTask task : tasks) {
            sb.append('\t');
            sb.append(String.format("%2s", index++));
            sb.append(' ');
            sb.append(task.getPrompt());
            sb.append('\n');
        }

        sb.append('\n');
        sb.append("==> ");

        return sb.toString();
    }

    private boolean isExit(String input) {
        try {
            return Integer.parseInt(input) == tasks.size();
        } catch (Exception e) {
            return false;
        }
    }
}
