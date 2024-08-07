package cogi234.rebalance.mixin;

import cogi234.rebalance.Cogi234sRebalance;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.spawner.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PhantomSpawner.class, priority = 1000)
public abstract class PhantomSpawnerMixin {
    @Shadow
    private int cooldown;

    /**
     * @author cogi234
     * @reason Change how phantoms spawn entirely
     */
    @Inject(at = @At("HEAD"), method = "spawn", cancellable = true)
    public void spawnOverwrite(ServerWorld world, boolean spawnMonsters, boolean spawnAnimals, CallbackInfoReturnable<Integer> cir) {
        if (Cogi234sRebalance.CONFIG.overwritePhantomSpawning()) {
            if (!spawnMonsters) {
                cir.setReturnValue(0);
                cir.cancel();
            } else if (!world.getGameRules().getBoolean(GameRules.DO_INSOMNIA)) {
                cir.setReturnValue(0);
                cir.cancel();
            } else {
                Random random = world.random;
                this.cooldown--;
                if (this.cooldown > 0) {
                    cir.setReturnValue(0);
                    cir.cancel();
                } else {
                    //Tries to spawn at the intervals specified in the config, converted from seconds to ticks
                    this.cooldown = this.cooldown + (Cogi234sRebalance.CONFIG.minPhantomSpawnCooldown() +
                            random.nextInt(Math.abs(Cogi234sRebalance.CONFIG.maxPhantomSpawnCooldown() - Cogi234sRebalance.CONFIG.minPhantomSpawnCooldown()))) * 20;
                    //If it's not night, they don't spawn
                    if (world.getAmbientDarkness() < 5 && world.getDimension().hasSkyLight()) {
                        cir.setReturnValue(0);
                        cir.cancel();
                    } else {
                        int phantomsSpawned = 0;

                        for (ServerPlayerEntity serverPlayerEntity : world.getPlayers()) {
                            //Phantoms don't spawn around spectators and players with night vision (unless we disable that in the config)
                            if (!serverPlayerEntity.isSpectator() && (!serverPlayerEntity.hasStatusEffect(StatusEffects.NIGHT_VISION) || Cogi234sRebalance.CONFIG.phantomsSpawnWithNightVision())) {
                                BlockPos playerPosition = serverPlayerEntity.getBlockPos();
                                //They only spawn around players when they're on the surface, or in a dimension without skylight
                                if (!world.getDimension().hasSkyLight() || playerPosition.getY() >= world.getSeaLevel() && world.isSkyVisible(playerPosition)) {
                                    LocalDifficulty localDifficulty = world.getLocalDifficulty(playerPosition);
                                    //They spawn if local difficulty is higher than a random number between 0 and 3
                                    //And with a random chance from the config
                                    if (localDifficulty.isHarderThan(random.nextFloat() * 3.0F) && random.nextFloat() < Cogi234sRebalance.CONFIG.phantomSpawnChance()) {
                                        BlockPos spawnPosition = playerPosition.up(20 + random.nextInt(15)).east(-10 + random.nextInt(21)).south(-10 + random.nextInt(21));
                                        BlockState blockState = world.getBlockState(spawnPosition);
                                        FluidState fluidState = world.getFluidState(spawnPosition);
                                        if (SpawnHelper.isClearForSpawn(world, spawnPosition, blockState, fluidState, EntityType.PHANTOM)) {
                                            EntityData entityData = null;
                                            int numberToSpawn = 1 + random.nextInt(localDifficulty.getGlobalDifficulty().getId() + 1);

                                            for (int i = 0; i < numberToSpawn; i++) {
                                                PhantomEntity phantomEntity = EntityType.PHANTOM.create(world);
                                                if (phantomEntity != null) {
                                                    phantomEntity.refreshPositionAndAngles(spawnPosition, 0.0F, 0.0F);
                                                    entityData = phantomEntity.initialize(world, localDifficulty, SpawnReason.NATURAL, entityData);
                                                    world.spawnEntityAndPassengers(phantomEntity);
                                                    phantomsSpawned++;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        cir.setReturnValue(phantomsSpawned);
                        cir.cancel();
                    }
                }
            }
        }
    }
}
