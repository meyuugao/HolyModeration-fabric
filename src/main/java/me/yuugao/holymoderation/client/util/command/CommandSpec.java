package me.yuugao.holymoderation.client.util.command;

import java.util.List;

public final class CommandSpec {
    private final String name;
    private final List<Argument> arguments;
    private final CommandHandler handler;
    private final String description;
    /**
     * Logical group for /hm help (usually the owning module's display name).
     */
    private final String group;

    private CommandSpec(String name, List<Argument> arguments, CommandHandler handler,
                        String description, String group) {
        this.name = name;
        this.arguments = arguments;
        this.handler = handler;
        this.description = description;
        this.group = group;
    }

    public static CommandSpec of(String name, Argument... arguments) {
        return new CommandSpec(name, List.of(arguments), null, null, null);
    }

    public CommandSpec handler(CommandHandler handler) {
        return new CommandSpec(name, arguments, handler, description, group);
    }

    public CommandSpec description(String description) {
        return new CommandSpec(name, arguments, handler, description, group);
    }

    public CommandSpec group(String group) {
        return new CommandSpec(name, arguments, handler, description, group);
    }

    public String name() {
        return name;
    }

    public List<Argument> arguments() {
        return arguments;
    }

    public int argumentCount() {
        return arguments.size();
    }

    public CommandHandler handler() {
        return handler;
    }

    public String description() {
        return description;
    }

    public String group() {
        return group;
    }

    /**
     * Human-readable signature, e.g. "/hm textedit <номер> <новый_текст>".
     */
    public String syntax() {
        StringBuilder sb = new StringBuilder("/hm ").append(name);
        for (Argument a : arguments) {
            sb.append(" <").append(a.name()).append(">");
        }
        return sb.toString();
    }
}
