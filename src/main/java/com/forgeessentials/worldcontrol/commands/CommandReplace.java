package com.forgeessentials.worldcontrol.commands;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.api.permissions.query.PermQuery.PermResult;
import com.forgeessentials.api.permissions.query.PermQueryPlayerArea;
import com.forgeessentials.core.PlayerInfo;
import com.forgeessentials.util.BackupArea;
import com.forgeessentials.util.ChatUtils;
import com.forgeessentials.util.FunctionHelper;
import com.forgeessentials.util.Localization;
import com.forgeessentials.util.OutputHandler;
import com.forgeessentials.util.AreaSelector.Selection;
import com.forgeessentials.util.tasks.TaskRegistry;
import com.forgeessentials.worldcontrol.TickTasks.TickTaskReplaceSelection;

public class CommandReplace extends WorldControlCommandBase
{

	public CommandReplace()
	{
		super(true);
	}

	@Override
	public String getName()
	{
		return "replace";
	}

	@Override
	public void processCommandPlayer(EntityPlayer player, String[] args)
	{
		if (args.length == 2)
		{
			PlayerInfo info = PlayerInfo.getPlayerInfo(player.username);
			if (info.getSelection() == null)
			{
				OutputHandler.chatError(player, Localization.get(Localization.ERROR_NOSELECTION));
				return;
			}
			int[] temp;
			int firstID = -1;
			int firstMeta = -1;
			int secondID = -1;
			int secondMeta = -1;

			// Begin parsing 1st argument pair

			try
			{
				temp = FunctionHelper.parseIdAndMetaFromString(args[0], true);
				firstID = temp[0];
				firstMeta = temp[1];
			}
			catch (Exception e)
			{
				OutputHandler.chatError(player, e.getMessage());
				return;
			}

			// Begin parsing 2nd argument pair if 1st was good.
			try
			{
				temp = FunctionHelper.parseIdAndMetaFromString(args[1], true);
				secondID = temp[0];
				secondMeta = temp[1];
			}
			catch (Exception e)
			{
				OutputHandler.chatError(player, e.getMessage());
				return;
			}

			if (firstID >= Block.blocksList.length || secondID >= Block.blocksList.length)
			{
				error(player, Localization.format("message.wc.blockIdOutOfRange", Block.blocksList.length));
			}
			else if (firstID != 0 && Block.blocksList[firstID] == null)
			{
				error(player, Localization.format("message.wc.invalidBlockId", firstID));
			}
			else if (secondID != 0 && Block.blocksList[secondID] == null)
			{
				error(player, Localization.format("message.wc.invalidBlockId", secondID));
			}
			else
			{
				Selection sel = info.getSelection();
				BackupArea back = new BackupArea();

				PermQueryPlayerArea query = new PermQueryPlayerArea(player, getCommandPerm(), sel, false);
				PermResult result = APIRegistry.perms.checkPermResult(query);

				switch (result)
					{
						case ALLOW:
							TaskRegistry.registerTask(new TickTaskReplaceSelection(player, firstID, firstMeta, secondID, secondMeta, back, sel));
							return;
						case PARTIAL:
							TaskRegistry.registerTask(new TickTaskReplaceSelection(player, firstID, firstMeta, secondID, secondMeta, back, sel, query.applicable));
						default:
							OutputHandler.chatError(player, Localization.get(Localization.ERROR_PERMDENIED));
							return;
					}

			}

			ChatUtils.sendMessage(player, "Working on replace.");
		}
		else
		{
			// The syntax of the command is not correct.
			error(player);
		}
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

}
