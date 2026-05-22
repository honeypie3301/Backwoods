package net.mcreator.thebackwoods.procedures;

import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.fml.ModList;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

import net.mcreator.thebackwoods.init.TheBackwoodsModItems;

public class GiveTestKitProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (ModList.get().isLoaded("tacz")) {
			{
				Entity _ent = entity;
				if (!_ent.level().isClientSide() && _ent.getServer() != null) {
					_ent.getServer().getCommands()
							.performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, _ent.position(), _ent.getRotationVector(), _ent.level() instanceof ServerLevel ? (ServerLevel) _ent.level() : null, 4, _ent.getName().getString(),
									_ent.getDisplayName(), _ent.level().getServer(), _ent),
									"give @s tacz:modern_kinetic_gun[minecraft:custom_data={GunId:\"tacz:minigun\", GunFireMode:\"AUTO\", HasBulletInBarrel:1b, HeatAmount:0.0f, GunCurrentAmmoCount:1}]");
				}
			}
			{
				Entity _ent = entity;
				if (!_ent.level().isClientSide() && _ent.getServer() != null) {
					_ent.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, _ent.position(), _ent.getRotationVector(), _ent.level() instanceof ServerLevel ? (ServerLevel) _ent.level() : null, 4,
							_ent.getName().getString(), _ent.getDisplayName(), _ent.level().getServer(), _ent), "give @s tacz:ammo_box[minecraft:custom_data={AllTypeCreative:1b}]");
				}
			}
		}
		if (entity instanceof Player _player) {
			ItemStack _setstack = new ItemStack(Items.NETHERITE_AXE).copy();
			_setstack.setCount(1);
			ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
		}
		if (entity instanceof Player _player) {
			ItemStack _setstack = new ItemStack(Blocks.OAK_PLANKS).copy();
			_setstack.setCount(128);
			ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
		}
		if (entity instanceof Player _player) {
			ItemStack _setstack = new ItemStack(Items.GOLDEN_CARROT).copy();
			_setstack.setCount(64);
			ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
		}
		if (entity instanceof Player _player) {
			ItemStack _setstack = new ItemStack(TheBackwoodsModItems.BACKWOODS.get()).copy();
			_setstack.setCount(2);
			ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
		}
		if (entity instanceof Player _player) {
			ItemStack _setstack = new ItemStack(Items.TORCH).copy();
			_setstack.setCount(128);
			ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
		}
	}
}