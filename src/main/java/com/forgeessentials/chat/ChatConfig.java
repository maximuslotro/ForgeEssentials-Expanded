package com.forgeessentials.chat;

import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.Set;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;

import com.forgeessentials.core.config.ConfigData;
import com.forgeessentials.core.config.ConfigLoaderBase;
import com.forgeessentials.util.output.LoggingHandler;

public class ChatConfig extends ConfigLoaderBase
{

    private static final String CATEGORY = ModuleChat.CONFIG_CATEGORY;

    private static final String CAT_GM = CATEGORY + "_Gamemodes";

    public static final String CHAT_FORMAT_HELP = "Format for chat. Always needs to contain all 5 \"%s\" placeholders like the default!";

    private static final String MUTEDCMD_HELP = "All commands in here will be blocked if the player is muted.";

    private static final String WELCOME_MESSAGE = "Welcome messages for new players. Can be color formatted (supports script arguments)";

    private static final String LOGIN_MESSAGE = "Login message shown each time the player logs in (supports script arguments)";

    private static final String DEFAULT_WELCOME_MESSAGE = "New player @player joined the server!";

    private static final String[] DEFAULT_LOGIN_MESSAGE = new String[] { "Welcome @player.", "This server is running ForgeEssentials" };

    public static String gamemodeCreative;

    public static String gamemodeAdventure;

    public static String gamemodeSurvival;

    public static String chatFormat = "%s%s<%s>%s%s ";

    public static String welcomeMessage;

    public static String[] loginMessage;

    public static Set<String> mutedCommands = new HashSet<>();

    static ForgeConfigSpec.ConfigValue<String> FEchatFormat;
    static ForgeConfigSpec.ConfigValue<String> FEwelcomeMessage;
    static ForgeConfigSpec.ConfigValue<String[]> FEloginMessage;
    static ForgeConfigSpec.ConfigValue<String> FEgamemodeSurvival;
    static ForgeConfigSpec.ConfigValue<String> FEgamemodeCreative;
    static ForgeConfigSpec.ConfigValue<String> FEgamemodeAdventure;
    static ForgeConfigSpec.BooleanValue FELogChat;
    static ForgeConfigSpec.ConfigValue<String[]> FEmutedCommands;

	@Override
	public void load(Builder BUILDER, boolean isReload)
    {
        BUILDER.comment("Chat configuration").push(CATEGORY);
        FEchatFormat = BUILDER.comment(CHAT_FORMAT_HELP).define("ChatFormat", "%s%s<%s>%s%s ");
        FELogChat = BUILDER.comment("Log all chat messages").define("LogChat", true);

        FEwelcomeMessage = BUILDER.comment(WELCOME_MESSAGE).define("WelcomeMessage", DEFAULT_WELCOME_MESSAGE);
        FEloginMessage = BUILDER.comment(LOGIN_MESSAGE).define("LoginMessage", DEFAULT_LOGIN_MESSAGE);
        BUILDER.pop();

        BUILDER.comment("Gamemode names").push(CAT_GM);
        FEgamemodeSurvival = BUILDER.define("Survival", "survival");
        FEgamemodeCreative = BUILDER.define("Creative", "creative");
        FEgamemodeAdventure = BUILDER.define("Adventure", "adventure");
        BUILDER.pop();

        BUILDER.push("mute");
        FEmutedCommands = BUILDER.comment(MUTEDCMD_HELP).define("mutedCommands", new String[] { "me" });
        BUILDER.pop();
    }

	@Override
	public void bakeConfig(boolean reload)
    {
        try
        {
            chatFormat = FEchatFormat.get();
            String.format(chatFormat, "", "", "", "", "");
        }
        catch (IllegalFormatException e)
        {
            LoggingHandler.felog.error("Invalid chat format specified in chat config!");
            chatFormat = "%s%s<%s>%s%s ";
        }

        welcomeMessage = FEwelcomeMessage.get();
        loginMessage = FEloginMessage.get();

        gamemodeSurvival = FEgamemodeSurvival.get();
        gamemodeCreative = FEgamemodeCreative.get();
        gamemodeAdventure = FEgamemodeAdventure.get();

        mutedCommands.clear();
        for (String cmd : FEmutedCommands.get())
            mutedCommands.add(cmd);

        ModuleChat.instance.setChatLogging(FELogChat.get());

    }
	@Override
	public ConfigData returnData() {
		return ModuleChat.data;
	}

}
