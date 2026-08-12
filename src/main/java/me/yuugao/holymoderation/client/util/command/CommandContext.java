package me.yuugao.holymoderation.client.util.command;

public final class CommandContext {
    private final String[] args;

    public CommandContext(String[] args) {
        this.args = args;
    }

    public String arg(int i) {
        return args[i];
    }

    public boolean hasArg(int i) {
        return i < args.length;
    }

    public int length() {
        return args.length;
    }
}
