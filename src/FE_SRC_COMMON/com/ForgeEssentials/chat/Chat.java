package com.ForgeEssentials.chat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.ServerChatEvent;

import com.ForgeEssentials.core.PlayerInfo;
import com.ForgeEssentials.permission.Group;
import com.ForgeEssentials.permission.PermissionsAPI;
import com.ForgeEssentials.permission.SqlHelper;
import com.ForgeEssentials.permission.Zone;
import com.ForgeEssentials.permission.ZoneManager;
import com.ForgeEssentials.permission.query.PermQueryPlayer;
import com.ForgeEssentials.util.FEChatFormatCodes;
import com.ForgeEssentials.util.FunctionHelper;
import com.ForgeEssentials.util.OutputHandler;
import com.ForgeEssentials.util.AreaSelector.Point;

import cpw.mods.fml.common.network.IChatListener;

public class Chat implements IChatListener
{
	public static List<String>	bannedWords	= new ArrayList<String>();
	public static boolean		censor;

	@ForgeSubscribe
	public void chatEvent(ServerChatEvent event)
	{
		/*
		 * Mute?
		 */

		if (event.player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getBoolean("mute"))
		{
			event.setCanceled(true);
			event.player.sendChatToPlayer("You are muted.");
			return;
		}

		String message = event.message;
		String nickname = event.username;

		if (censor)
		{
			for (String word : bannedWords)
			{
				message = replaceAllIgnoreCase(message, word, "###");
			}
		}

		/*
		 * Nickname
		 */

		if (event.player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).hasKey("nickname"))
		{
			nickname = event.player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getString("nickname");
		}

		/*
		 * Colorize!
		 */

		if (event.message.contains("&"))
		{
			if (PermissionsAPI.checkPermAllowed(new PermQueryPlayer(event.player, "ForgeEssentials.chat.usecolor")))
			{
				message = FunctionHelper.formatColors(event.message);
			}
		}

		String rank = "";
		String zoneID = "";
		String gPrefix = "";
		String gSuffix = "";
		
		PlayerInfo info = PlayerInfo.getPlayerInfo(event.player);
		String playerPrefix = info.prefix == null ? "" : info.prefix.trim();
		String playerSuffix = info.suffix == null ? "" : info.suffix.trim();
		
		Zone zone = ZoneManager.getWhichZoneIn(new Point(event.player), event.player.worldObj);
		zoneID = zone.getZoneName();
		
		// group stuff!! NO TOCUH!!!
		{
			rank = getGroupRankString(event.username);
			
			gPrefix = getGroupPrefixString(event.username);
			gPrefix = FunctionHelper.formatColors(gPrefix);
			
			gSuffix = getGroupSuffixString(event.username);
			gSuffix = FunctionHelper.formatColors(gSuffix);
		}

		String format = ConfigChat.chatFormat;
		format = ConfigChat.chatFormat == null || ConfigChat.chatFormat.trim().isEmpty() ? "<%username>%message" : ConfigChat.chatFormat;
		
		// replace group, zone, and rank
		format.replaceAll("%rank", rank);
		format.replaceAll("%zone", zoneID);
		format.replaceAll("%groupPrefix", gPrefix);
		format.replaceAll("%groupSuffix", gSuffix);
		
		// replace colors
		format.replaceAll("%red", FEChatFormatCodes.RED.toString());
		format.replaceAll("%yellow", FEChatFormatCodes.YELLOW.toString());
		format.replaceAll("%black", FEChatFormatCodes.BLACK.toString());
		format.replaceAll("%darkblue", FEChatFormatCodes.DARKBLUE.toString());
		format.replaceAll("%darkgreen", FEChatFormatCodes.DARKGREEN.toString());
		format.replaceAll("%darkaqua", FEChatFormatCodes.DARKAQUA.toString());
		format.replaceAll("%darkred", FEChatFormatCodes.DARKRED.toString());
		format.replaceAll("%purple", FEChatFormatCodes.PURPLE.toString());
		format.replaceAll("%gold", FEChatFormatCodes.GOLD.toString());
		format.replaceAll("%grey", FEChatFormatCodes.GREY.toString());
		format.replaceAll("%darkgrey", FEChatFormatCodes.DARKGREY.toString());
		format.replaceAll("%indigo", FEChatFormatCodes.INDIGO.toString());
		format.replaceAll("%green", FEChatFormatCodes.GREEN.toString());
		format.replaceAll("%aqua", FEChatFormatCodes.AQUA.toString());
		format.replaceAll("%pink", FEChatFormatCodes.PINK.toString());
		format.replaceAll("%white", FEChatFormatCodes.WHITE.toString());
		
		// replace MC formating
		format.replaceAll("%random", FEChatFormatCodes.RANDOM.toString());
		format.replaceAll("%bold", FEChatFormatCodes.BOLD.toString());
		format.replaceAll("%strike", FEChatFormatCodes.STRIKE.toString());
		format.replaceAll("%underline", FEChatFormatCodes.UNDERLINE.toString());
		format.replaceAll("%italics", FEChatFormatCodes.ITALICS.toString());
		format.replaceAll("%reset", FEChatFormatCodes.RESET.toString());
		
		// random nice things...
		format.replaceAll("%health", "" + event.player.getHealth());
		
		// essentials
		format.replace("%playerPrefix", playerPrefix);
		format.replaceAll("%splayerSuffix", playerSuffix);
		format.replaceAll("%username", nickname);
		format.replace("%message", message);
	}

	@Override
	public Packet3Chat serverChat(NetHandler handler, Packet3Chat message)
	{
		return message;
	}

	@Override
	public Packet3Chat clientChat(NetHandler handler, Packet3Chat message)
	{
		return message;
	}

	private String replaceAllIgnoreCase(String text, String search, String replacement)
	{
		if (search.equals(replacement))
		{
			return text;
		}
		StringBuffer buffer = new StringBuffer(text);
		String lowerSearch = search.toLowerCase();
		int i = 0;
		int prev = 0;
		while ((i = buffer.toString().toLowerCase().indexOf(lowerSearch, prev)) > -1)
		{
			buffer.replace(i, i + search.length(), replacement);
			prev = i + replacement.length();
		}
		return buffer.toString();
	}
	
	private String getGroupRankString(String username)
	{
		Matcher match = ConfigChat.groupRegex.matcher(ConfigChat.groupRankFormat);
		
		ArrayList<TreeSet<Group>> list = getGroupsList(match, username);
		
		String end = ConfigChat.groupRankFormat;
		
		TreeSet<Group> set;
		String temp = "";
		for (int i = 1; i <= match.groupCount(); i++)
		{
			set = list.get(i-1);
			for (Group g: set)
				temp = temp+g.name;
			
			end.replace(match.group(i), temp);
			temp = "";
		}
		
		return end;
	}
	
	private String getGroupPrefixString(String username)
	{
		Matcher match = ConfigChat.groupRegex.matcher(ConfigChat.groupPrefixFormat);
		
		ArrayList<TreeSet<Group>> list = getGroupsList(match, username);
		
		String end = ConfigChat.groupPrefixFormat;
		
		TreeSet<Group> set;
		String temp = "";
		for (int i = 1; i <= match.groupCount(); i++)
		{
			set = list.get(i-1);
			for (Group g: set)
				temp = g.prefix+temp;
			
			end.replace(match.group(i), temp);
			temp = "";
		}
		
		return end;
	}
	
	private String getGroupSuffixString(String username)
	{
		Matcher match = ConfigChat.groupRegex.matcher(ConfigChat.groupSuffixFormat);
		
		ArrayList<TreeSet<Group>> list = getGroupsList(match, username);
		
		String end = ConfigChat.groupSuffixFormat;
		
		TreeSet<Group> set;
		String temp = "";
		for (int i = 1; i <= match.groupCount(); i++)
		{
			set = list.get(i-1);
			for (Group g: set)
				temp = temp+g.suffix;
			
			end.replace(match.group(i), temp);
			temp = "";
		}
		
		return end;
	}
	
	private ArrayList<TreeSet<Group>> getGroupsList(Matcher match, String username)
	{
		ArrayList<TreeSet<Group>> list = new ArrayList<TreeSet<Group>>();
		
		String whole;
		String[] p;
		TreeSet<Group> set;
		while (match.find())
		{
			whole = match.group();
			whole.replaceAll("{", "").replace("}", "");
			p = whole.split("\\<\\:\\>", 2);
			if (p[0].equalsIgnoreCase("..."))
				p[0] = null;
			if (p[1].equalsIgnoreCase("..."))
				p[1] = null;
			
			set = SqlHelper.getGroupsForChat(p[0], p[1], username);
			list.add(set);
		}
		
		list = removeDuplicates(list);
		return list;
	}
	
	private ArrayList<TreeSet<Group>> removeDuplicates(ArrayList<TreeSet<Group>> list)
	{
		HashSet<Group> used = new HashSet<Group>();
		
		for (TreeSet set: list)
		{
			for (Group g : used)
				set.remove(g);
			
			// add all the remaining...
			used.addAll(set);
		}
		
		return list;
	}
}
