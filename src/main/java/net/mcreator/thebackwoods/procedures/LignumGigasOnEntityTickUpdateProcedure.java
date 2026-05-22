package net.mcreator.thebackwoods.procedures;

import net.mcreator.thebackwoods.entity.LignumGigasEntity;
// 1.21.1 neoforge
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LignumGigasOnEntityTickUpdateProcedure {
	private static final int EFFECT_TICK = 50;
	private static final int PUSH_INTERVAL = 3;

	private static final double AURA_RADIUS = 28.0;
	private static final double PUSH_H = 1.8;
	private static final double PUSH_V = 0.45;

	private static final int RUMBLE_INTERVAL_TICKS = 24;
	private static final float RUMBLE_VOLUME = 2.7f;
	private static final float RUMBLE_PITCH = 0.48f;

	// heavy sink on spawn
	private static final int SINK_TOTAL_STEPS = 5;
	private static final int SINK_INTERVAL_TICKS = 10;
	private static final int SINK_RADIUS = 9;
	private static final float SINK_MAX_BREAK_HARDNESS = 40.0f; // do not break >= 40

	public static void execute() { }

	public static void execute(LignumGigasEntity gigas) {
		if (gigas == null) return;
		Level level = gigas.level();
		if (level.isClientSide()) return;
		if (!gigas.getEntityData().get(LignumGigasEntity.DATA_seq_active)) return;

		int t = gigas.getEntityData().get(LignumGigasEntity.DATA_seq_t) + 1;
		gigas.getEntityData().set(LignumGigasEntity.DATA_seq_t, t);

		// ---- heavy sink sequence (ONE TIME EVER for this entity) ----
		boolean sinkDoneForever = gigas.getPersistentData().getBoolean("bw_sink_done_forever");
		if (!sinkDoneForever) {
			int sinkStep = gigas.getPersistentData().getInt("bw_sink_step");
			if (sinkStep < SINK_TOTAL_STEPS && t % SINK_INTERVAL_TICKS == 0) {
				int targetY = (int) Math.floor(gigas.getY()) - 1;
				carveRoughLayer(level, (int) Math.floor(gigas.getX()), targetY, (int) Math.floor(gigas.getZ()), sinkStep);

				gigas.setPos(gigas.getX(), gigas.getY() - 1.0, gigas.getZ());
				gigas.hurtMarked = true;

				sinkStep++;
				gigas.getPersistentData().putInt("bw_sink_step", sinkStep);

				if (sinkStep >= SINK_TOTAL_STEPS) {
					gigas.getPersistentData().putBoolean("bw_sink_done_forever", true);
				}
			}
		}

		boolean done = gigas.getEntityData().get(LignumGigasEntity.DATA_effect_done);
		if (!done && t >= EFFECT_TICK) {
			gigas.getEntityData().set(LignumGigasEntity.DATA_effect_done, true);

			level.playSound(null, BlockPos.containing(gigas.getX(), gigas.getY(), gigas.getZ()),
					SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 5.0f, 0.45f);

			dropPlayersInventoryNoArmor(level, gigas);
		}

		int soundCd = gigas.getEntityData().get(LignumGigasEntity.DATA_aura_sound_cd);
		if (soundCd <= 0) {
			level.playSound(null, BlockPos.containing(gigas.getX(), gigas.getY(), gigas.getZ()),
					SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, RUMBLE_VOLUME, RUMBLE_PITCH);
			gigas.getEntityData().set(LignumGigasEntity.DATA_aura_sound_cd, RUMBLE_INTERVAL_TICKS);
		} else {
			gigas.getEntityData().set(LignumGigasEntity.DATA_aura_sound_cd, soundCd - 1);
		}

		if (t % PUSH_INTERVAL == 0) {
			flingAll(level, gigas);
		}

		int auraLeft = gigas.getEntityData().get(LignumGigasEntity.DATA_aura_time_left) - 1;
		gigas.getEntityData().set(LignumGigasEntity.DATA_aura_time_left, auraLeft);

		if (auraLeft <= 0) {
			gigas.getEntityData().set(LignumGigasEntity.DATA_play_attack_anim, false);
			gigas.getEntityData().set(LignumGigasEntity.DATA_play_lower_anim, true);

			gigas.getEntityData().set(LignumGigasEntity.DATA_seq_active, false);
			gigas.getEntityData().set(LignumGigasEntity.DATA_seq_t, 0);
			gigas.getEntityData().set(LignumGigasEntity.DATA_effect_done, false);
			gigas.getEntityData().set(LignumGigasEntity.DATA_aura_time_left, 0);
			gigas.getEntityData().set(LignumGigasEntity.DATA_aura_sound_cd, 0);

			// do NOT reset bw_sink_step / bw_sink_done_forever
		}
	}

	private static void carveRoughLayer(Level level, int cx, int y, int cz, int step) {
		long seed = (cx * 73428767L) ^ (cz * 912931L) ^ (y * 19349663L) ^ (step * 83492791L);

		for (int dx = -SINK_RADIUS; dx <= SINK_RADIUS; dx++) {
			for (int dz = -SINK_RADIUS; dz <= SINK_RADIUS; dz++) {
				double dist = Math.sqrt(dx * dx + dz * dz);
				if (dist > SINK_RADIUS + 0.35) continue;

				double edgeFactor = dist / (double) SINK_RADIUS;
				double keepChance = 0.08 + (edgeFactor * 0.45);

				long h = seed + dx * 341873128712L + dz * 132897987541L;
				double r = ((h ^ (h >>> 13) ^ (h << 7)) & 1023) / 1023.0;
				if (r < keepChance) continue;

				BlockPos p = new BlockPos(cx + dx, y, cz + dz);
				if (!level.isEmptyBlock(p)) {
					float hardness = level.getBlockState(p).getDestroySpeed(level, p);
					if (hardness < 0.0f || hardness >= SINK_MAX_BREAK_HARDNESS) continue;
					level.destroyBlock(p, false);
				}
			}
		}
	}

	private static void dropPlayersInventoryNoArmor(Level level, LignumGigasEntity g) {
		List<Player> players = level.getEntitiesOfClass(
				Player.class,
				new AABB(new Vec3(g.getX(), g.getY(), g.getZ()), new Vec3(g.getX(), g.getY(), g.getZ())).inflate(AURA_RADIUS),
				p -> p != null && p.isAlive()
		);
		for (Player p : players) dropPlayerInventoryNoArmor(p);
	}

	private static void flingAll(Level level, LignumGigasEntity g) {
		double cx = g.getX(), cy = g.getY(), cz = g.getZ();

		List<Entity> entities = level.getEntitiesOfClass(
				Entity.class,
				new AABB(new Vec3(cx, cy, cz), new Vec3(cx, cy, cz)).inflate(AURA_RADIUS),
				e -> e != null && e.isAlive() && e != g
		);

		for (Entity e : entities) {
			double dx = e.getX() - cx;
			double dz = e.getZ() - cz;
			double len = Math.max(0.001, Math.sqrt(dx * dx + dz * dz));
			double nx = dx / len;
			double nz = dz / len;

			e.setDeltaMovement(nx * PUSH_H, Math.max(PUSH_V, e.getDeltaMovement().y + 0.1), nz * PUSH_H);
			e.hurtMarked = true;

			if (e instanceof Player p) dropPlayerInventoryNoArmor(p);
		}
	}

	private static void dropPlayerInventoryNoArmor(Player p) {
		for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
			ItemStack st = p.getInventory().getItem(i);
			if (!st.isEmpty()) {
				p.drop(st.copy(), true, false);
				p.getInventory().setItem(i, ItemStack.EMPTY);
			}
		}

		ItemStack off = p.getItemBySlot(EquipmentSlot.OFFHAND);
		if (!off.isEmpty()) {
			p.drop(off.copy(), true, false);
			p.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
		}

		p.getInventory().setChanged();
	}
}