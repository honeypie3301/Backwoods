package net.mcreator.thebackwoods.procedures;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import org.joml.Vector3f;

@EventBusSubscriber
public class FractusInvasionProcedureProcedure {

	// CONFIGURABLE CONSTANTS FOR SPAWN TIMING, QUANTITIES AND SCALING
	private static final int BASE_COOLDOWN_TICKS = 2400;          // 2 minutes default interval between breaches
	private static final int MIN_COOLDOWN_TICKS = 900;            // 45 seconds minimum interval under maximum escalation
	private static final int COOLDOWN_REDUCTION_INTERVAL = 1200;  // Decrease cooldown threshold for every 1 minute held (1200 ticks)
	private static final int COOLDOWN_REDUCTION_AMOUNT = 300;     // Reduce cooldown by 15 seconds per step of held time

	private static final int BREACH_PREPARATION_DURATION = 140;   // 7 seconds of theatrical outer blue vortex build-up (Stage 2)
	private static final int BREACH_ACTIVE_DURATION = 120;        // 6 seconds of steady gateway opening and staggered summons (Stage 3)

	private static final double PORTAL_MAX_RADIUS = 2.8;          // Significantly increased size for massive Avengers visual scale

	// CONFIGURABLE: Fractus spawn quantities per breach occurrence
	private static final int MIN_SPAWN_COUNT = 3;                 // Minimum Fractus spawned under normal conditions
	private static final int MAX_SPAWN_COUNT = 7;                 // Maximum base Fractus spawned (before escalation bonus)

	@SubscribeEvent
	public static void onEntityTick(EntityTickEvent.Pre event) {
		Entity entity = event.getEntity();
		if (entity instanceof Player player && !player.level().isClientSide() && player.level() instanceof ServerLevel level) {
			if (level.dimension() == Level.OVERWORLD || level.dimension() == Level.NETHER) {
				processInvasionStates(level, player);
			}
		}
	}

	private static void processInvasionStates(ServerLevel level, Player player) {
		int checkTimer = player.getPersistentData().getInt("fractus_core_check_timer");
		boolean hasCore = false;
		if (checkTimer <= 0) {
			hasCore = hasFractusCoreNearby(level, player);
			player.getPersistentData().putBoolean("fractus_has_core_cached", hasCore);
			player.getPersistentData().putInt("fractus_core_check_timer", 20);
		} else {
			player.getPersistentData().putInt("fractus_core_check_timer", checkTimer - 1);
			hasCore = player.getPersistentData().getBoolean("fractus_has_core_cached");
		}

		// Global escalation hold-tracking (Stage 1)
		int holdTime = player.getPersistentData().getInt("fractus_core_hold_time");
		if (hasCore) {
			holdTime++;
		} else {
			if (holdTime > 0) {
				holdTime = Math.max(0, holdTime - 5); // Decays 5 times faster if discarded
			}
		}
		player.getPersistentData().putInt("fractus_core_hold_time", holdTime);

		// Tick down active breach cooldowns
		int cooldown = player.getPersistentData().getInt("fractus_invasion_cooldown");
		if (cooldown > 0) {
			player.getPersistentData().putInt("fractus_invasion_cooldown", cooldown - 1);
			return;
		}

		int portalState = player.getPersistentData().getInt("fractus_portal_state"); // 0 = Idle, 1 = Forming (Stage 2), 2 = Active (Stage 3)

		// State 0: IDLE
		if (portalState == 0) {
			// Require the player to hold the core for at least 10 seconds (200 ticks) before the portal triggers
			if (hasCore && holdTime >= 200) {
				BlockPos breachPos = findBreachPosition(level, player);
				if (breachPos != null) {
					double px = breachPos.getX() + 0.5;
					double py = breachPos.getY() + 1.8; // Levitated slightly higher to fit the larger portal perfectly
					double pz = breachPos.getZ() + 0.5;

					// Calculate angle facing the player's core
					double dx = player.getX() - px;
					double dz = player.getZ() - pz;
					double yaw = Math.atan2(dz, dx); // Radians angle towards player

					player.getPersistentData().putInt("fractus_portal_state", 1);
					player.getPersistentData().putInt("fractus_portal_ticks", BREACH_PREPARATION_DURATION);
					player.getPersistentData().putDouble("fractus_portal_x", px);
					player.getPersistentData().putDouble("fractus_portal_y", py);
					player.getPersistentData().putDouble("fractus_portal_z", pz);
					player.getPersistentData().putDouble("fractus_portal_yaw", yaw);

					// Trigger initialization thunder sound
					level.playSound(null, px, py, pz, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 2.5F, 0.45F);
					level.playSound(null, px, py, pz, SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 4.0F, 0.50F);
				}
			}
		}
		// State 1: BREACH FORMING (Stage 2)
		else if (portalState == 1) {
			int ticksLeft = player.getPersistentData().getInt("fractus_portal_ticks") - 1;
			double px = player.getPersistentData().getDouble("fractus_portal_x");
			double py = player.getPersistentData().getDouble("fractus_portal_y");
			double pz = player.getPersistentData().getDouble("fractus_portal_z");
			double yaw = player.getPersistentData().getDouble("fractus_portal_yaw");

			// Periodic powerful beacon low-pitch rumble loop simulating cosmic expansion
			if (ticksLeft % 20 == 0) {
				level.playSound(null, px, py, pz, SoundEvents.BEACON_AMBIENT, SoundSource.HOSTILE, 3.5F, 0.45F);
			}
			if (ticksLeft % 25 == 0) {
				level.playSound(null, px, py, pz, SoundEvents.PORTAL_AMBIENT, SoundSource.HOSTILE, 2.0F, 0.55F);
			}

			// Render Space Stone colorable vanilla dust portal growing outwards
			double progress = (double) (BREACH_PREPARATION_DURATION - ticksLeft) / BREACH_PREPARATION_DURATION;
			spawnSpaceStoneVisuals(level, px, py, pz, yaw, progress * PORTAL_MAX_RADIUS);

			if (ticksLeft <= 0) {
				player.getPersistentData().putInt("fractus_portal_state", 2);
				player.getPersistentData().putInt("fractus_portal_ticks", BREACH_ACTIVE_DURATION);

				// Determine exact stable spawn quantities based on min/max settings
				int minS = MIN_SPAWN_COUNT;
				int maxS = MAX_SPAWN_COUNT;
				int baseCount = minS;
				if (maxS > minS) {
					baseCount = minS + level.getRandom().nextInt(maxS - minS + 1);
				}
				int bonus = Math.min(3, (holdTime / COOLDOWN_REDUCTION_INTERVAL) / 2); // Escalation bonus
				int totalToSpawn = baseCount + bonus;
				player.getPersistentData().putInt("fractus_spawn_total_count", totalToSpawn);

				// High-energy ignition sound when portal becomes fully active
				level.playSound(null, px, py, pz, SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 3.0F, 0.50F);
			} else {
				player.getPersistentData().putInt("fractus_portal_ticks", ticksLeft);
			}
		}
		// State 2: ARRIVAL / ACTIVE PORTAL (Stage 3)
		else if (portalState == 2) {
			int ticksLeft = player.getPersistentData().getInt("fractus_portal_ticks") - 1;
			double px = player.getPersistentData().getDouble("fractus_portal_x");
			double py = player.getPersistentData().getDouble("fractus_portal_y");
			double pz = player.getPersistentData().getDouble("fractus_portal_z");
			double yaw = player.getPersistentData().getDouble("fractus_portal_yaw");

			// Continuously stream the space sound hum
			if (ticksLeft % 15 == 0) {
				level.playSound(null, px, py, pz, SoundEvents.BEACON_AMBIENT, SoundSource.HOSTILE, 4.0F, 0.35F);
			}

			// Render visually stable or slightly pulsing portal size
			double currentRadius = PORTAL_MAX_RADIUS;
			if (ticksLeft <= 30) {
				// Smoothly shrink on final collapse phase
				currentRadius = ((double) ticksLeft / 30.0) * PORTAL_MAX_RADIUS;
			}
			spawnSpaceStoneVisuals(level, px, py, pz, yaw, currentRadius);

			// Staggered summoning sequence: even and smooth distribution
			int totalToSpawn = player.getPersistentData().getInt("fractus_spawn_total_count");
			if (totalToSpawn > 0) {
				int interval = BREACH_ACTIVE_DURATION / (totalToSpawn + 1);
				if (interval <= 0) interval = 12;
				int currentElapsed = BREACH_ACTIVE_DURATION - ticksLeft;
				if (currentElapsed > 0 && currentElapsed % interval == 0 && (currentElapsed / interval) <= totalToSpawn) {
					spawnAndLaunchFractus(level, player, px, py, pz, yaw);
				}
			}

			if (ticksLeft <= 0) {
				// Collapse sound trigger
				level.playSound(null, px, py, pz, SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 3.0F, 0.60F);
				level.playSound(null, px, py, pz, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0F, 1.80F);

				// Put portal into final cooldown
				int reduction = Math.min(BASE_COOLDOWN_TICKS - MIN_COOLDOWN_TICKS, (holdTime / COOLDOWN_REDUCTION_INTERVAL) * COOLDOWN_REDUCTION_AMOUNT);
				int nextCooldown = BASE_COOLDOWN_TICKS - reduction;
				// organic variance (+/- 15 second delay fluctuation)
				nextCooldown += (level.getRandom().nextInt(600) - 300);

				player.getPersistentData().putInt("fractus_invasion_cooldown", Math.max(MIN_COOLDOWN_TICKS, nextCooldown));
				player.getPersistentData().putInt("fractus_portal_state", 0);
				player.getPersistentData().putInt("fractus_spawn_total_count", 0);
			} else {
				player.getPersistentData().putInt("fractus_portal_ticks", ticksLeft);
			}
		}
	}

	private static void spawnSpaceStoneVisuals(ServerLevel level, double cx, double cy, double cz, double yaw, double radius) {
		if (radius <= 0.05) return;

		net.minecraft.util.RandomSource rnd = level.getRandom();

		// Face orientation trigonometry
		double perpX = -Math.sin(yaw);
		double perpZ = Math.cos(yaw);

		// Backward vector tunneling deep into the breach
		double bx = -Math.cos(yaw);
		double bz = -Math.sin(yaw);

		// Volumetric Space Stone Color Palette:
		// 1. Light smoke/storm gray for a detailed, smoky outer rim (modified from pitch charcoal)
		DustParticleOptions spaceSmoke = new DustParticleOptions(new Vector3f(0.35F, 0.38F, 0.42F), 1.95F);
		// 2. Swirling cosmic royal blue
		DustParticleOptions spaceVortex = new DustParticleOptions(new Vector3f(0.00F, 0.30F, 0.95F), 1.65F);
		// 3. Blinding, high-density Space Stone cyan for the innermost depth
		DustParticleOptions spaceCyan = new DustParticleOptions(new Vector3f(0.15F, 0.88F, 1.00F), 1.75F);
		// 4. White-blue high-energy electric arcs
		DustParticleOptions electricArc = new DustParticleOptions(new Vector3f(0.50F, 0.95F, 1.00F), 1.15F);

		// We construct a massive, volumetric 8-slice 3D funnel curving backwards to save performance
		int totalSlices = 8;
		for (int s = 0; s < totalSlices; s++) {
			double d = s * 0.28; // 0.28 blocks interval backwards (tunnel depth expansion)
			// Radius tapers down smoothly matching a beautiful cosmic rendering funnel
			double rAtD = radius * (1.0 - (s / (double) totalSlices) * 0.55);
			if (rAtD <= 0.1) continue;

			// A. Outer smoky border shell per slice (thick and volumetric)
			int borderCount = (int) (rAtD * 16);
			for (int i = 0; i < borderCount; i++) {
				double theta = rnd.nextDouble() * 2.0 * Math.PI;
				double jitterRadius = rAtD + (rnd.nextDouble() - 0.5) * 0.25;
				
				// Push center parabolic curvature factor (outer smoke curves slightly back too)
				double depthOffset = d + (1.0 - (jitterRadius / rAtD)) * 0.52;
				
				double px = cx + depthOffset * bx + jitterRadius * Math.cos(theta) * perpX;
				double py = cy + jitterRadius * Math.sin(theta);
				double pz = cz + depthOffset * bz + jitterRadius * Math.cos(theta) * perpZ;

				level.sendParticles(spaceSmoke, px, py, pz, 1, 0.0, 0.0, 0.0, 0.01);
			}

			// B. Hollow central swirling energy disk per slice (high density, but hollowed out for visual authenticity and client performance)
			int swirlCount = (int) (rAtD * 26);
			for (int i = 0; i < swirlCount; i++) {
				double theta = rnd.nextDouble() * 2.0 * Math.PI;
				
				// Hollow funnel shape: outer/middle slices are hollow tubes, funnel bottom is a solid glowing core
				double minFraction = (s < 3) ? 0.65 : ((s < 5) ? 0.35 : 0.0);
				double r = rAtD * (minFraction + (1.0 - minFraction) * rnd.nextDouble());

				// Parabolic center-push depth offset: particles closer to the center are pushed deeper backwards!
				double depthOffset = d + (1.0 - (r / rAtD)) * 0.52;

				double px = cx + depthOffset * bx + r * Math.cos(theta) * perpX;
				double py = cy + r * Math.sin(theta);
				double pz = cz + depthOffset * bz + r * Math.cos(theta) * perpZ;

				// Make the energetic core pure bright cyan at the singularity point
				DustParticleOptions activeColor = (s >= 5 || r < rAtD * 0.25) ? spaceCyan : spaceVortex;

				// Swirl velocity vector physics creating an orbital vortex loop at that depth slice
				double vx = -Math.sin(theta) * perpX * 0.065;
				double vy = Math.cos(theta) * 0.065;
				double vz = -Math.sin(theta) * perpZ * 0.065;

				level.sendParticles(activeColor, px, py, pz, 1, vx, vy, vz, 0.02);
			}
		}

		// C. Crackling electrical discharge loops extending from front-face to core
		int electricCount = (int) (radius * 6);
		for (int i = 0; i < electricCount; i++) {
			double theta = rnd.nextDouble() * 2.0 * Math.PI;
			double d = rnd.nextDouble() * 1.8; // random depth arc
			double r = (0.2 + rnd.nextDouble() * 0.8) * radius * (1.0 - (d / 1.8) * 0.55);
			
			double depthOffset = d + (1.0 - (r / (radius * (1.0 - (d / 1.8) * 0.55)))) * 0.52;

			double px = (cx + depthOffset * bx) + r * Math.cos(theta) * perpX;
			double py = cy + r * Math.sin(theta);
			double pz = (cz + depthOffset * bz) + r * Math.cos(theta) * perpZ;

			double rx = (rnd.nextDouble() - 0.5) * 0.25;
			double ry = (rnd.nextDouble() - 0.5) * 0.25;
			double rz = (rnd.nextDouble() - 0.5) * 0.25;

			level.sendParticles(electricArc, px + rx, py + ry, pz + rz, 1, 0.0, 0.0, 0.0, 0.02);
		}
	}

	private static void spawnAndLaunchFractus(ServerLevel level, Player player, double cx, double cy, double cz, double yaw) {
		EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("the_backwoods:fractus"));
		if (type != null) {
			Entity summon = type.create(level);
			if (summon != null) {
				// Align initial orientation standing straight, facing forward yaw
				summon.moveTo(cx, cy - 0.5, cz, (float) (yaw * 180.0 / Math.PI), 0.0F);

				// Direct mathematical forward push vector out of the visual portal frame
				double forwardX = Math.cos(yaw);
				double forwardZ = Math.sin(yaw);

				// Imparts a beautiful smooth horizontal launch speed and subtle levitation thrust
				summon.setDeltaMovement(forwardX * 0.42, 0.14, forwardZ * 0.42);
				summon.hurtMarked = true; // Signals clients to sync new velocities smoothly

				if (summon instanceof Mob mob) {
					mob.setTarget(player); // Immediately locks hostility onto the core handler!
				}

				level.addFreshEntity(summon);

				// Heavy summoning spatial burst sounds and visual exit shockwaves
				level.playSound(null, cx, cy, cz, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.8F, 0.80F);
				level.playSound(null, cx, cy, cz, SoundEvents.SHULKER_TELEPORT, SoundSource.HOSTILE, 1.5F, 0.90F);

				// Beautiful cyan dust spatial emission puff
				level.sendParticles(new DustParticleOptions(new Vector3f(0.0F, 0.90F, 1.0F), 1.7F), cx, cy, cz, 45, 0.6, 0.6, 0.6, 0.12);
			}
		}
	}

	private static boolean hasFractusCore(Player player) {
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			net.minecraft.world.item.ItemStack stack = player.getInventory().getItem(i);
			if (!stack.isEmpty()) {
				String itemID = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
				if ("the_backwoods:fractus_core".equals(itemID)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean hasFractusCoreNearby(ServerLevel level, Player player) {
		if (hasFractusCore(player)) {
			return true;
		}

		// 1. Scan for dropped ItemEntity items on the ground within 128 blocks
		double scanRange = 128.0;
		java.util.List<net.minecraft.world.entity.item.ItemEntity> items = level.getEntitiesOfClass(
			net.minecraft.world.entity.item.ItemEntity.class,
			player.getBoundingBox().inflate(scanRange),
			e -> {
				net.minecraft.world.item.ItemStack stack = e.getItem();
				if (!stack.isEmpty()) {
					String itemID = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
					return "the_backwoods:fractus_core".equals(itemID);
				}
				return false;
			}
		);
		if (!items.isEmpty()) {
			return true;
		}

		// 2. Scan container block entities (chest, barrel, custom modded container)
		// within a 120-block horizontal radius and 60-block vertical radius around player.
		// Instead of iterating millions of block positions, we query only loaded chunks.
		int playerChunkX = player.blockPosition().getX() >> 4;
		int playerChunkZ = player.blockPosition().getZ() >> 4;
		int chunkRadius = 8; // 8 chunks covers up to 128 blocks
		double maxDistSq = 120.0 * 120.0;
		double maxYDiff = 60.0;

		for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
			for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
				int cx = playerChunkX + dx;
				int cz = playerChunkZ + dz;
				if (level.getChunkSource().hasChunk(cx, cz)) {
					net.minecraft.world.level.chunk.LevelChunk chunk = level.getChunk(cx, cz);
					for (net.minecraft.world.level.block.entity.BlockEntity be : chunk.getBlockEntities().values()) {
						if (be != null && !be.isRemoved()) {
							BlockPos pos = be.getBlockPos();
							double distSq = pos.distToCenterSqr(player.position());
							if (distSq <= maxDistSq && Math.abs(pos.getY() - player.getY()) <= maxYDiff) {
								if (be instanceof net.minecraft.world.Container container) {
									for (int i = 0; i < container.getContainerSize(); i++) {
										net.minecraft.world.item.ItemStack stack = container.getItem(i);
										if (!stack.isEmpty()) {
											String itemID = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
											if ("the_backwoods:fractus_core".equals(itemID)) {
												return true;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

		// 3. Scan for placed down Fractus Core blocks in surrounding loaded chunk columns (radius calculated perfectly) (Increased by 2.5x)
		BlockPos playerPos = player.blockPosition();
		int radiusXZ = 120;
		int radiusY = 60;
		BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
		int startX = playerPos.getX() - radiusXZ;
		int endX = playerPos.getX() + radiusXZ;
		int startZ = playerPos.getZ() - radiusXZ;
		int endZ = playerPos.getZ() + radiusXZ;
		int startY = playerPos.getY() - radiusY;
		int endY = playerPos.getY() + radiusY;

		int startChunkX = startX >> 4;
		int endChunkX = endX >> 4;
		int startChunkZ = startZ >> 4;
		int endChunkZ = endZ >> 4;

		for (int cx = startChunkX; cx <= endChunkX; cx++) {
			for (int cz = startChunkZ; cz <= endChunkZ; cz++) {
				if (level.getChunkSource().hasChunk(cx, cz)) {
					net.minecraft.world.level.chunk.LevelChunk chunk = level.getChunk(cx, cz);
					int minX = Math.max(startX, cx << 4);
					int maxX = Math.min(endX, (cx << 4) + 15);
					int minZ = Math.max(startZ, cz << 4);
					int maxZ = Math.min(endZ, (cz << 4) + 15);
					int minY = Math.max(level.getMinBuildHeight(), startY);
					int maxY = Math.min(level.getMaxBuildHeight(), endY);

					net.minecraft.world.level.chunk.LevelChunkSection[] sections = chunk.getSections();
					for (int sIndex = 0; sIndex < sections.length; sIndex++) {
						net.minecraft.world.level.chunk.LevelChunkSection section = sections[sIndex];
						if (section == null || section.hasOnlyAir()) {
							continue;
						}
						int sectMinY = level.getMinBuildHeight() + (sIndex << 4);
						int sectMaxY = sectMinY + 15;
						if (sectMaxY < minY || sectMinY > maxY) {
							continue;
						}

						boolean potentiallyHasCore = section.getStates().maybeHas(state -> {
							if (state.isAir()) return false;
							String name = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
							return "the_backwoods:fractus_core".equals(name) || name.contains("fractus_core");
						});

						if (!potentiallyHasCore) {
							continue;
						}

						int localMinY = Math.max(minY, sectMinY);
						int localMaxY = Math.min(maxY, sectMaxY);

						for (int x = minX; x <= maxX; x++) {
							for (int z = minZ; z <= maxZ; z++) {
								for (int y = localMinY; y <= localMaxY; y++) {
									mut.set(x, y, z);
									net.minecraft.world.level.block.state.BlockState blockState = chunk.getBlockState(mut);
									if (!blockState.isAir()) {
										String blockID = BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString();
										if ("the_backwoods:fractus_core".equals(blockID) || blockID.contains("fractus_core")) {
											return true;
										}
									}
								}
							}
						}
					}
				}
			}
		}

		return false;
	}

	private static BlockPos findBreachPosition(ServerLevel level, Player player) {
		double px = player.getX();
		double py = player.getY();
		double pz = player.getZ();
		net.minecraft.util.RandomSource random = level.getRandom();

		double playerYaw = (player.getYRot() + 90.0F) * (Math.PI / 180.0);

		// We prefer finding a floating position at the player's approximate Y height.
		// Check several vertical offsets near the player's current elevation first.
		int[] dyOffsets = {0, 1, 2, -1, -2, 3};

		// Pass 1: Try to find a completely clear floating space (3x3x4 of air) near the player's height
		for (int attempts = 0; attempts < 80; attempts++) {
			double angle = playerYaw + (random.nextDouble() - 0.5) * (Math.PI * 80.0 / 180.0);
			double dist = 5.5 + random.nextDouble() * 4.5; // From 5.5 to 10 blocks (closer, easier to see, highly cinematic!)

			double targetX = px + Math.cos(angle) * dist;
			double targetZ = pz + Math.sin(angle) * dist;

			for (int dy : dyOffsets) {
				BlockPos candidatePos = BlockPos.containing(targetX, py + dy, targetZ);

				boolean clear = true;
				CheckBounds:
				for (int dx = -1; dx <= 1; dx++) {
					for (int dz = -1; dz <= 1; dz++) {
						for (int h = 0; h < 4; h++) {
							BlockPos checkPos = candidatePos.offset(dx, h, dz);
							// If block is solid (not empty and not fluid), it's an obstruction
							if (!level.isEmptyBlock(checkPos) && level.getFluidState(checkPos).isEmpty()) {
								clear = false;
								break CheckBounds;
							}
						}
					}
				}

				if (clear) {
					return candidatePos;
				}
			}
		}

		// Pass 2 (Fallback): If no fully clear 3x3x4 floating space is found, try to locate any spot on surface close to player
		for (int attempts = 0; attempts < 50; attempts++) {
			double angle = playerYaw + (random.nextDouble() - 0.5) * (Math.PI * 80.0 / 180.0);
			double dist = 6.0 + random.nextDouble() * 5.0;

			double targetX = px + Math.cos(angle) * dist;
			double targetZ = pz + Math.sin(angle) * dist;

			BlockPos basePos = BlockPos.containing(targetX, py, targetZ);
			BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, basePos);

			// Check if the surface block is within a reasonable height range of the player (-5 to +8 blocks)
			if (Math.abs(surfacePos.getY() - py) <= 8.0) {
				boolean clear = true;
				CheckSurface:
				for (int dx = -1; dx <= 1; dx++) {
					for (int dz = -1; dz <= 1; dz++) {
						for (int h = 0; h < 3; h++) {
							BlockPos checkPos = surfacePos.offset(dx, h, dz);
							if (!level.isEmptyBlock(checkPos) && level.getFluidState(checkPos).isEmpty()) {
								clear = false;
								break CheckSurface;
							}
						}
					}
				}
				if (clear) {
					return surfacePos;
				}
			}
		}

		// Ultimate Fallback: Place perfectly in front of the player's gaze floating in air
		double fallbackX = px + Math.cos(playerYaw) * 6.0;
		double fallbackZ = pz + Math.sin(playerYaw) * 6.0;
		return BlockPos.containing(fallbackX, py + 1.0, fallbackZ);
	}
}
