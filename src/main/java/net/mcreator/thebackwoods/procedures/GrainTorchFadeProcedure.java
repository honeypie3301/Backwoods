package net.mcreator.thebackwoods.procedures;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber
public class GrainTorchFadeProcedure {

	// ---------- TUNABLE ----------
	private static final int CHECK_INTERVAL_TICKS = 10;
	private static final double DISAPPEAR_CHANCE = 0.06;
	private static final double WATCH_DOT_THRESHOLD = 0.70;
	private static final int MAX_TRACKED_TORCHES = 256;
	// -----------------------------

	private static final String NBT_LIST_KEY = "grain_tracked_torches";

	private static final ResourceKey<Level> THE_GRAIN = ResourceKey.create(
		Registries.DIMENSION,
		ResourceLocation.parse("the_backwoods:the_grain")
	);

	@SubscribeEvent
	public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
		Entity e = event.getEntity();
		if (!(e instanceof Player player))
			return;

		Level level = player.level();
		if (level.isClientSide())
			return;

		if (!level.dimension().equals(THE_GRAIN))
			return;

		BlockState placed = event.getPlacedBlock();
		if (!isTorch(placed))
			return;

		BlockPos pos = event.getPos();
		addTrackedTorch(player, pos);
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (player == null)
			return;

		Level level = player.level();
		if (level.isClientSide())
			return;

		if (!level.dimension().equals(THE_GRAIN))
			return;

		if (player.tickCount % CHECK_INTERVAL_TICKS != 0)
			return;

		List<BlockPos> tracked = getTrackedTorches(player);
		if (tracked.isEmpty())
			return;

		List<BlockPos> survivors = new ArrayList<>(tracked.size());

		for (BlockPos pos : tracked) {
			BlockState state = level.getBlockState(pos);

			// Drop from tracking if torch no longer exists.
			if (!isTorch(state))
				continue;

			boolean watched = isPlayerWatchingTorch(level, player, pos);

			if (!watched && Math.random() < DISAPPEAR_CHANCE) {
				level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
				continue;
			}

			survivors.add(pos);
		}

		saveTrackedTorches(player, survivors);
	}

	private static boolean isTorch(BlockState state) {
		return state.is(Blocks.TORCH)
				|| state.is(Blocks.WALL_TORCH)
				|| state.is(Blocks.SOUL_TORCH)
				|| state.is(Blocks.SOUL_WALL_TORCH)
				|| state.is(Blocks.REDSTONE_TORCH)
				|| state.is(Blocks.REDSTONE_WALL_TORCH);
	}

	private static boolean isPlayerWatchingTorch(LevelAccessor world, Player player, BlockPos torchPos) {
		Vec3 eye = player.getEyePosition();
		Vec3 target = new Vec3(torchPos.getX() + 0.5, torchPos.getY() + 0.5, torchPos.getZ() + 0.5);

		Vec3 toTorch = target.subtract(eye).normalize();
		double dot = player.getLookAngle().normalize().dot(toTorch);
		boolean facingTorch = dot > WATCH_DOT_THRESHOLD;
		if (!facingTorch)
			return false;

		HitResult hit = world.clip(new ClipContext(
				eye,
				target,
				ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE,
				player
		));

		if (hit.getType() == HitResult.Type.MISS)
			return true;

		if (hit.getType() == HitResult.Type.BLOCK) {
			BlockPos hitPos = BlockPos.containing(hit.getLocation());
			return hitPos.equals(torchPos);
		}

		return false;
	}

	private static void addTrackedTorch(Player player, BlockPos pos) {
		List<BlockPos> tracked = getTrackedTorches(player);

		for (BlockPos p : tracked) {
			if (p.equals(pos)) {
				return;
			}
		}

		tracked.add(pos);

		if (tracked.size() > MAX_TRACKED_TORCHES) {
			tracked = tracked.subList(tracked.size() - MAX_TRACKED_TORCHES, tracked.size());
		}

		saveTrackedTorches(player, tracked);
	}

	private static List<BlockPos> getTrackedTorches(Player player) {
		List<BlockPos> out = new ArrayList<>();
		CompoundTag data = player.getPersistentData();

		if (!data.contains(NBT_LIST_KEY, Tag.TAG_LIST))
			return out;

		ListTag list = data.getList(NBT_LIST_KEY, Tag.TAG_STRING);
		for (int i = 0; i < list.size(); i++) {
			String s = list.getString(i);
			BlockPos p = parsePos(s);
			if (p != null) {
				out.add(p);
			}
		}

		return out;
	}

	private static void saveTrackedTorches(Player player, List<BlockPos> tracked) {
		ListTag list = new ListTag();
		for (BlockPos p : tracked) {
			list.add(StringTag.valueOf(encodePos(p)));
		}
		player.getPersistentData().put(NBT_LIST_KEY, list);
	}

	private static String encodePos(BlockPos pos) {
		return pos.getX() + "," + pos.getY() + "," + pos.getZ();
	}

	private static BlockPos parsePos(String s) {
		try {
			String[] parts = s.split(",");
			if (parts.length != 3)
				return null;
			int x = Integer.parseInt(parts[0]);
			int y = Integer.parseInt(parts[1]);
			int z = Integer.parseInt(parts[2]);
			return new BlockPos(x, y, z);
		} catch (Exception ignored) {
			return null;
		}
	}
}