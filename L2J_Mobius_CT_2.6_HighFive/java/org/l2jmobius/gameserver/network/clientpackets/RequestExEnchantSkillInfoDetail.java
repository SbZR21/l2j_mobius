/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.data.xml.impl.EnchantSkillGroupsData;
import org.l2jmobius.gameserver.model.EnchantSkillLearn;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.ExEnchantSkillInfoDetail;

/**
 * Format (ch) ddd c: (id) 0xD0 h: (subid) 0x31 d: type d: skill id d: skill lvl
 * @author -Wooden-
 */
public class RequestExEnchantSkillInfoDetail implements IClientIncomingPacket
{
	private int _type;
	private int _skillId;
	private int _skillLvl;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_type = packet.readD();
		_skillId = packet.readD();
		_skillLvl = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		if ((_skillId <= 0) || (_skillLvl <= 0))
		{
			return;
		}
		
		final PlayerInstance player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		int reqSkillLvl = -2;
		if ((_type == 0) || (_type == 1))
		{
			reqSkillLvl = _skillLvl - 1; // enchant
		}
		else if (_type == 2)
		{
			reqSkillLvl = _skillLvl + 1; // untrain
		}
		else if (_type == 3)
		{
			reqSkillLvl = _skillLvl; // change route
		}
		
		final int playerSkillLvl = player.getSkillLevel(_skillId);
		
		// dont have such skill
		if (playerSkillLvl == 0)
		{
			return;
		}
		
		// if reqlvl is 100,200,.. check base skill lvl enchant
		if ((reqSkillLvl % 100) == 0)
		{
			final EnchantSkillLearn esl = EnchantSkillGroupsData.getInstance().getSkillEnchantmentBySkillId(_skillId);
			if (esl != null)
			{
				// if player dont have min level to enchant
				if (playerSkillLvl != esl.getBaseLevel())
				{
					return;
				}
			}
			// enchant data dont exist?
			else
			{
				return;
			}
		}
		// change route is different skill lvl but same enchant
		else if ((playerSkillLvl != reqSkillLvl) && (_type == 3) && ((playerSkillLvl % 100) != (_skillLvl % 100)))
		{
			return;
		}
		
		// send skill enchantment detail
		player.sendPacket(new ExEnchantSkillInfoDetail(_type, _skillId, _skillLvl, player));
	}
}
