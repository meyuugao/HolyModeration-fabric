package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.MessageReceiveEvent;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import obfuscator.DontObf;
import obfuscator.ObfRule;

@DontObf(ObfRule.OBF_STRING)
public class MessageModule extends Module {
    @Subscribe
    public void onMessageModify(MessageReceiveEvent event) {
        Text newComponent;
        Text component;
        String componentMessage;
        String message;

        component = event.getMessage();
        newComponent = component;
        componentMessage = component.getString();
        message = componentMessage.replaceAll("§[0-9a-zA-Z]", "");
        if (componentMessage.contains("§6§6")) {
            MutableText suggestTextComponent = Text.literal("");
            String regex = "§6§6(.*?)§f";
            List<String> matches = new ArrayList<>();
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(componentMessage);
            while (matcher.find()) {
                matches.add(matcher.group());
            }

            for (int i = 0; i < matches.size(); i++) {
                if (i == matches.size() - 1) {
                    suggestTextComponent.append(Text.literal(componentMessage.split(matches.get(i))[0]));
                    suggestTextComponent.append(serviceContext.getChatService().suggestTextComponent(matches.get(i)));
                    if (componentMessage.split(matches.get(i)).length != 1) {
                        suggestTextComponent.append(Text.literal(componentMessage.split(matches.get(i))[1]));
                    }
                } else {
                    suggestTextComponent.append(Text.literal(componentMessage.split(matches.get(i))[0]));
                    suggestTextComponent.append(serviceContext.getChatService().suggestTextComponent(matches.get(i)));
                    componentMessage = componentMessage.replace(componentMessage.split(matches.get(i))[0] + matches.get(i), "");
                }
            }

            newComponent = suggestTextComponent;
            component = newComponent;
        }

        String[] messageSplit = message.split(" ");
        String commandValue = messageSplit[1];
        if (serviceContext.getChatService().isArrayContains(new String[]{"вк", "внестирепорт", "внестипроверка",
                "внестиавтобай", "внестиавтоселл", "внестикандидат", "внеститопклан", "внестикастомка", "внестиперсонал",
                "внестимногопроверок", "закончитьчистый", "закончитьбанда", "закончитьбаннет", "закончитьавтобай", "закончитьавтоселл"}, commandValue)) {
            MutableText textComponent = Text.literal("");

            switch (commandValue) { //tip: удали этот модуль к чертям и делай все компоненты сразу через clientmessage как и пишешь текст
                case ("вк"): {
                    textComponent.append(serviceContext.getChatService().generateComponent(
                            Text.of(WHITE + BOLD + "Ваш вк: " + AQUA + BOLD + message.split("вк ")[1].split(" id")[0] + " ("),
                            serviceContext.getChatService().openURLTextComponent(WHITE + BOLD + "vk.com/id" + message.split(" id")[1], "Нажмите, чтобы открыть ссылку на свой вк.", "https://vk.com/id" + message.split(" id")[1]),
                            Text.of(AQUA + BOLD + ")")));
                    break;
                }
                case ("внестирепорт"): {
                    textComponent.append(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Внести проверку по репорту", "Нажмите, чтобы внести проверку по репорту", "/startcheckout " + serviceContext.getStateService().getPlayer() + " report"));
                    break;
                }
                case ("внестипроверка"): {
                    textComponent.append(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Внести обычную проверку", "Нажмите, чтобы внести обычную проверку", "/startcheckout " + serviceContext.getStateService().getPlayer() + " checkout"));
                    break;
                }
                case ("внестиавтобай"): {
                    textComponent.append(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Внести проверку автобаера", "Нажмите, чтобы внести проверку автобаера", "/startcheckout " + serviceContext.getStateService().getPlayer() + " autobuy"));
                    break;
                }
                case ("внестиавтоселл"): {
                    textComponent.append(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Внести проверку автоселлера", "Нажмите, чтобы внести проверку автоселлера", "/startcheckout " + serviceContext.getStateService().getPlayer() + " autosell"));
                    break;
                }
                case ("внестикандидат"): {
                    textComponent.append(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Внести проверку кандидата", "Нажмите, чтобы внести проверку кандидата", "/startcheckout " + serviceContext.getStateService().getPlayer() + " candidate"));
                    break;
                }
                case ("внестикастомка"): {
                    textComponent.append(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Внести проверку кастомки", "Нажмите, чтобы внести проверку кастомки", "/startcheckout " + serviceContext.getStateService().getPlayer() + " customka"));
                    break;
                }
                case ("внестиперсонал"): {
                    textComponent.append(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Внести проверку персонала", "Нажмите, чтобы внести проверку персонала", "/startcheckout " + serviceContext.getStateService().getPlayer() + " personal"));
                    break;
                }
                case ("внестимногопроверок"): {
                    textComponent.append(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Внести проверку игрока, у которого много пройденных проверок", "Нажмите, чтобы внести проверку игрока, у которого много пройденных проверок", "/startcheckout " + serviceContext.getStateService().getPlayer() + " toManyChecks"));
                    break;
                }
                case ("закончитьчистый"): {
                    textComponent.append(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Закончить проверку с результатом 'чистый'", "Нажмите, чтобы закончить проверку с результатом 'чистый'", "/endcheckout clean"));
                    break;
                }
                case ("закончитьбанда"): {
                    textComponent.append(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Закончить проверку с результатом 'бан' + снести стеш", "Нажмите, чтобы закончить проверку с результатом 'бан' + снести стеш", "/endcheckout ban " + messageSplit[2] + " true"));
                    break;
                }
                case ("закончитьбаннет"): {
                    textComponent.append(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Закончить проверку с результатом 'бан' + не сносить стеш", "Нажмите, чтобы закончить проверку с результатом 'бан' + не сносить стеш", "/endcheckout ban " + messageSplit[2] + " false"));
                    break;
                }
                case ("закончитьавтобай"): {
                    textComponent.append(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Закончить проверку с результатом 'автобай'", "Нажмите, чтобы закончить проверку с результатом 'автобай'", "/endcheckout autobuy"));
                    break;
                }
                case ("закончитьавтоселл"): {
                    textComponent.append(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Закончить проверку с результатом 'автоселл'", "Нажмите, чтобы закончить проверку с результатом 'автоселл'", "/endcheckout autosell"));
                    break;
                }
            }

            component = newComponent = serviceContext.getChatService().generateComponent(serviceContext.getChatService().HMTextComponent, textComponent);
        }

        boolean renderCopyButton = true;
        if (!serviceContext.getStateService().getPlayer().isEmpty()) {
            if ((message.contains(":") && message.split(":")[0].contains(serviceContext.getStateService().getPlayer())) && !message.startsWith("Игрок") && !message.startsWith("История") && !message.startsWith("[я ->")) {
                newComponent = serviceContext.getChatService().generateComponent(Text.literal(serviceContext.getConfigManager().getConfig().getPlayerMarker() + " §f" + serviceContext.getStateService().getPlayer() + " §5-> "), serviceContext.getChatService().copyTextComponent(message.split(": ")[message.split(": ").length - 1], "Оригинальное сообщение: " + message + "\nНажмите, чтобы скопировать сообщение игрока.", message.split(": ")[1])); //message.split(": ").length - 1 нужен для того, чтобы если в титуле было ": ", оно не ломалось к чертям
                renderCopyButton = false;
            }
        }

        if (serviceContext.getConfigManager().getConfig().isCopyButtonEnabled() && !message.startsWith("[HM]")) {
            if (renderCopyButton) {
                MutableText copyComponent = Text.literal(" ");
                copyComponent.append(serviceContext.getChatService().copyTextComponent(serviceContext.getConfigManager().getConfig().getCopyButtonText(), "Нажмите, чтобы скопировать сообщение.", message));

                newComponent = serviceContext.getChatService().generateComponent(component, copyComponent);
            }
        }

        event.setMessage(newComponent);
    }
}