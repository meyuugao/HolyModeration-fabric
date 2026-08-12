package me.yuugao.holymoderation.client.util.command;

@FunctionalInterface
public interface CommandHandler {
    void execute(CommandContext ctx);
}