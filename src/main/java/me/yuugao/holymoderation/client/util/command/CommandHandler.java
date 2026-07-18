package me.yuugao.holymoderation.client.util.command;

import me.yuugao.obfuscator.DontObf;
import me.yuugao.obfuscator.ObfRule;

@FunctionalInterface
public interface CommandHandler {
    void execute(CommandContext ctx);
}