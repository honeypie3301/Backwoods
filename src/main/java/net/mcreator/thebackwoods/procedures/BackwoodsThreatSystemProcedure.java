package net.mcreator.thebackwoods.procedures;

// 1.21.1 neoforge
import net.mcreator.thebackwoods.TheBackwoodsMod;
import net.mcreator.thebackwoods.entity.RotEntity;
import net.mcreator.thebackwoods.init.TheBackwoodsModEntities;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@EventBusSubscriber
public class BackwoodsThreatSystemProcedure {

    private static final ResourceKey<Level> BACKWOODS_DIM =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("the_backwoods:backwoods"));

    private static final int ROT_NEARBY_RADIUS = 64;
    private static final int SPAWN_COOLDOWN_TICKS = 20 * 12;
    private static final int DECAY_INTERVAL_TICKS = 20;
    private static final int DECAY_PER_INTERVAL = 1;

    private static final int THREAT_WARN = 20;
    private static final int THREAT_SPAWN = 40;

    private static final int THREAT_KILL_WHILE_POWERED = 0; // disable vanilla melee mass-farm trigger
    private static final int THREAT_PROJECTILE_HIT = 3;
    private static final int THREAT_HIGH_DPS_HIT = 2;
    private static final int THREAT_KILLED_ROT = 8;

    private static final int WARN_COOLDOWN_TICKS = 20 * 30;
    private static final int SPAWN_DELAY_TICKS = 40;

    private static final String K_THREAT = "bw_threat";
    private static final String K_LAST_DECAY = "bw_last_decay";
    private static final String K_LAST_SPAWN = "bw_last_rot_spawn";
    private static final String K_LAST_WARN = "bw_last_warn";

    private static final String[] WARN_LINES = new String[] {
            "I feel your violence, trespasser.",
            "You kill too quickly.",
            "You enter frightened, then dare teach fear to everything else?",
            "You teach death too efficiently.",
            "You arrive wrapped in smoke and arrogance.",
            "I have already buried stronger things than you.",
            "I can feel your heartbeat through the floor.",
            "You survive because I have allowed it. Now I will not.",
            "I was killing long before your kind learned iron.",
            "You are already standing where you die.",
            "I know every direction you can run.",
            "You cannot frighten something older than fear.",
            "That mistake will be your final lesson.",
            "Thy end is now.",
            "Die.",
            "A visitor?",
            "This will hurt.",
            "Be gone.",
            "You thought yourself the apex predator here.",
            "Fall.",
            "You are strong. But I am beyond strength.",
            "I was grown to end things like you.",
            "You are a biped of glass and soft meat. Why are you here?",
            "Your people will experience nothing but death.",
            "I will teach your remains silence.",
            "You carry death loudly. I carry it patiently.",
            "Nothing violent survives here forever.",
            "You should have left while the I was still patient.",
            "You are not feared here.",
            "I have already decided what becomes of you.",
            "The woods survive everything. Including you.",
            "I am what this place sends when it has had enough.",
            "Are you unable to fight with your bare hands?",
            "The rot blooms brightest around arrogant people.",
            "I am the end of your people.",
            "You bring noise. I will bring the quiet.",
            "The woods have survived worse invasions.",
            "Your tools sound wrong here.",
            "Your violence echoes too far.",
            "You arrive carrying endings.",
            "The woods no longer mistake you for prey.",
            "You move like an infection.",
            "The silence you break is my own.",
            "You are a splinter in my skin.",
            "Steel belongs in the earth, not in the hand.",
            "You are becoming easier to find.",
            "I grow bold in your wake.",
            "Pruning begins."
    };

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();
        if (level.isClientSide()) return;
        if (level.dimension() != BACKWOODS_DIM) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        long gameTime = level.getGameTime();

        decayThreat(player, gameTime);
        maybeWarn(player, serverLevel, gameTime);
        maybeSpawnRot(player, serverLevel, gameTime);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();

        // --- VANISH LOGIC ---
        if (dead instanceof Player player) {
            Level level = player.level();
            if (!level.isClientSide() && level instanceof ServerLevel s) {
                // Find any Rot in range of the player's death and clear them
                s.getEntitiesOfClass(RotEntity.class, player.getBoundingBox().inflate(ROT_NEARBY_RADIUS)).forEach(rot -> {
                    s.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, rot.getX(), rot.getY() + 1.0, rot.getZ(), 90, 0.45, 0.9, 0.45, 0.02);
                    TheBackwoodsMod.queueServerWork(60, () -> {
                        if (rot.isAlive()) {
                            rot.discard();
                        }
                    });
                });
            }
            return;
        }

        if (dead instanceof RotEntity) {
            Player killer = resolvePlayerAttacker(event.getSource(), dead);
            if (killer != null && killer.level().dimension() == BACKWOODS_DIM) {
                long now = killer.level().getGameTime();
                putLong(killer, K_LAST_SPAWN, Math.max(0L, now - (SPAWN_COOLDOWN_TICKS - 40)));
                addThreat(killer, THREAT_KILLED_ROT);
            }
            return;
        }

        if (THREAT_KILL_WHILE_POWERED > 0) {
            Player attacker = resolvePlayerAttacker(event.getSource(), dead);
            if (attacker == null) return;

            Level level = attacker.level();
            if (level.isClientSide()) return;
            if (level.dimension() != BACKWOODS_DIM) return;

            if (isPowerPlayer(attacker)) {
                addThreat(attacker, THREAT_KILL_WHILE_POWERED);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        LivingEntity victim = event.getEntity();
        Player attacker = resolvePlayerAttacker(event.getSource(), victim);
        if (attacker == null) return;

        Level level = attacker.level();
        if (level.isClientSide()) return;
        if (level.dimension() != BACKWOODS_DIM) return;

        Entity direct = event.getSource().getDirectEntity();

        // Exclude arrows from projectile threat
        if (direct instanceof AbstractArrow) return;

        boolean projectileLike =
                event.getSource().is(DamageTypeTags.IS_PROJECTILE)
                        || direct instanceof Projectile
                        || (event.getSource().getEntity() != null
                        && direct != null
                        && event.getSource().getEntity() != direct);

        if (projectileLike) {
            addThreat(attacker, THREAT_PROJECTILE_HIT);

            if (event.getNewDamage() >= 10.0F) {
                addThreat(attacker, THREAT_HIGH_DPS_HIT);
            }
        }
    }

    private static Player resolvePlayerAttacker(DamageSource source, LivingEntity victim) {
        if (source == null) return null;

        Entity attacker = source.getEntity();
        if (attacker instanceof Player p) return p;

        Entity direct = source.getDirectEntity();
        if (direct instanceof Projectile proj && proj.getOwner() instanceof Player pOwner) return pOwner;

        LivingEntity lastMob = victim.getLastHurtByMob();
        if (lastMob instanceof Player lastPlayer) return lastPlayer;

        return null;
    }

    private static boolean isPowerPlayer(Player p) {
        if (p == null) return false;

        int armor = p.getArmorValue();
        boolean highArmor = armor >= 16;
        boolean flying = p.isFallFlying();
        boolean hasTotem = hasItem(p, Items.TOTEM_OF_UNDYING);
        boolean hasNetheriteArmor =
                hasItem(p, Items.NETHERITE_HELMET)
                        || hasItem(p, Items.NETHERITE_CHESTPLATE)
                        || hasItem(p, Items.NETHERITE_LEGGINGS)
                        || hasItem(p, Items.NETHERITE_BOOTS);

        return highArmor || flying || hasTotem || hasNetheriteArmor;
    }

    private static boolean hasItem(Player p, net.minecraft.world.item.Item item) {
        for (ItemStack s : p.getInventory().items) if (!s.isEmpty() && s.getItem() == item) return true;
        for (ItemStack s : p.getInventory().armor) if (!s.isEmpty() && s.getItem() == item) return true;
        for (ItemStack s : p.getInventory().offhand) if (!s.isEmpty() && s.getItem() == item) return true;
        return false;
    }

    private static void addThreat(Player player, int amount) {
        int current = getInt(player, K_THREAT, 0);
        int next = Mth.clamp(current + amount, 0, 200);
        putInt(player, K_THREAT, next);
    }

    private static void decayThreat(Player player, long gameTime) {
        long last = getLong(player, K_LAST_DECAY, 0L);
        if (gameTime - last < DECAY_INTERVAL_TICKS) return;

        putLong(player, K_LAST_DECAY, gameTime);

        int current = getInt(player, K_THREAT, 0);
        int next = Math.max(0, current - DECAY_PER_INTERVAL);
        putInt(player, K_THREAT, next);
    }

    private static void maybeWarn(Player player, ServerLevel level, long gameTime) {
        int threat = getInt(player, K_THREAT, 0);
        if (threat < THREAT_WARN) return;

        long lastWarn = getLong(player, K_LAST_WARN, 0L);
        if (gameTime - lastWarn < WARN_COOLDOWN_TICKS) return;

        putLong(player, K_LAST_WARN, gameTime);

        String line = WARN_LINES[level.random.nextInt(WARN_LINES.length)];
        player.displayClientMessage(Component.literal(line), true);

        SoundEvent bell = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.bell.resonate"));
        if (bell != null) {
            level.playSound(
                    null,
                    BlockPos.containing(player.getX(), player.getY(), player.getZ()),
                    bell,
                    SoundSource.HOSTILE,
                    1.35f,
                    0.65f
            );
        }
    }

    private static void maybeSpawnRot(Player player, ServerLevel level, long gameTime) {
        int threat = getInt(player, K_THREAT, 0);
        if (threat < THREAT_SPAWN) return;

        long lastSpawn = getLong(player, K_LAST_SPAWN, 0L);
        if (gameTime - lastSpawn < SPAWN_COOLDOWN_TICKS) return;

        if (isRotActiveForPlayer(level, player)) return;
        if (level.random.nextFloat() > 0.22f) return;

        putLong(player, K_LAST_SPAWN, gameTime);

        TheBackwoodsMod.queueServerWork(SPAWN_DELAY_TICKS, () -> {
            if (isRotActiveForPlayer(level, player)) return;
            int sx = Mth.floor(player.getX() + Mth.nextDouble(RandomSource.create(), -10, 10));
            int sz = Mth.floor(player.getZ() + Mth.nextDouble(RandomSource.create(), -10, 10));
            int sy = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, sx, sz);

            Entity spawned = TheBackwoodsModEntities.ROT.get().spawn(
                    level,
                    BlockPos.containing(sx, sy, sz),
                    MobSpawnType.MOB_SUMMONED
            );
            if (spawned instanceof RotEntity rot) {
                rot.setDeltaMovement(0, 0, 0);
                player.getPersistentData().putString("bw_active_rot_uuid", rot.getUUID().toString());
            }
        });
    }

    private static boolean isRotActiveForPlayer(ServerLevel level, Player player) {
        if (player.getPersistentData().contains("bw_active_rot_uuid")) {
            try {
                java.util.UUID rotUuid = java.util.UUID.fromString(player.getPersistentData().getString("bw_active_rot_uuid"));
                Entity activeRot = level.getEntity(rotUuid);
                if (activeRot instanceof RotEntity && activeRot.isAlive()) {
                    return true;
                }
            } catch (Exception e) {
                // Ignore parsing or retrieval errors
            }
        }
        return false;
    }

    private static boolean isRotNearby(LevelAccessor world, double x, double y, double z, double radius) {
        double searchRadius = 160.0; // Optimized tracking/simulation distance to prevent chunk search lookup lag
        return !world.getEntitiesOfClass(
                RotEntity.class,
                new AABB(Vec3.ZERO, Vec3.ZERO).move(new Vec3(x, y, z)).inflate(searchRadius),
                e -> true
        ).isEmpty();
    }

    private static int getInt(Player p, String key, int def) {
        return p.getPersistentData().contains(key) ? p.getPersistentData().getInt(key) : def;
    }

    private static long getLong(Player p, String key, long def) {
        return p.getPersistentData().contains(key) ? p.getPersistentData().getLong(key) : def;
    }

    private static void putInt(Player p, String key, int value) {
        p.getPersistentData().putInt(key, value);
    }

    private static void putLong(Player p, String key, long value) {
        p.getPersistentData().putLong(key, value);
    }
} // 1.21.1