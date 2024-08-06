package cogi234.rebalance.mixin;

import cogi234.rebalance.Cogi234sRebalance;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartEntityMixin extends VehicleEntity {
    public AbstractMinecartEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    // This is almost entirely copied from audaki's minecraft cart engine mod
    // Link: https://github.com/audaki/minecraft-cart-engine

    @Shadow
    protected abstract boolean willHitBlockAt(BlockPos pos);

    @Shadow
    public abstract Vec3d snapPositionToRail(double x, double y, double z);

    @Shadow
    protected abstract void applySlowdown();

    @Shadow
    protected abstract double getMaxSpeed();

    @Shadow
    public abstract AbstractMinecartEntity.Type getMinecartType();

    @Shadow
    private static Pair<Vec3i, Vec3i> getAdjacentRailPositionsByShape(RailShape shape) {
        // This is just fake code, the shadowed private function will be executed
        return Pair.of(Direction.NORTH.getVector(), Direction.SOUTH.getVector());
    }

    @Unique
    private static boolean isEligibleFastRail(BlockState state) {
        return state.isOf(Blocks.RAIL) || (state.isOf(Blocks.POWERED_RAIL) && state.get(PoweredRailBlock.POWERED));
    }

    @Unique
    private static RailShape getRailShape(BlockState state) {
        if (!(state.getBlock() instanceof AbstractRailBlock railBlock))
            throw new IllegalArgumentException("No rail shape found");

        return state.get(railBlock.getShapeProperty());
    }

    @Inject(at = @At("HEAD"), method = "moveOnRail", cancellable = true)
    protected void moveOnRailOverwrite(BlockPos pos, BlockState state, CallbackInfo ci) {
        // We only change logic for rideable minecarts, so we don't break hopper/chest minecart creations
        if (this.getMinecartType() != AbstractMinecartEntity.Type.RIDEABLE) {
            return;
        }

        // We only change logic when the minecart is currently being ridden by a living entity (player/villager/mob)
        boolean hasLivingRider = this.getFirstPassenger() instanceof LivingEntity;
        if (!hasLivingRider) {
            return;
        }

        this.modifiedMoveOnRail(pos, state);
        ci.cancel();
    }

    @Unique
    protected void modifiedMoveOnRail(BlockPos startBlockPos, BlockState state) {
        World world = this.getWorld();

        final double tps = 20.;
        final double maxSpeed = ((double)Cogi234sRebalance.CONFIG.minecartMaxSpeed()) / tps;
        final double maxMomentum = maxSpeed * 5.;
        final double vanillaMaxSpeed = 8. / tps;
        final double vanillaMaxMomentum = 40. / tps;

        this.onLanding();

        double thisX = this.getX();
        double thisY = this.getY();
        double thisZ = this.getZ();

        Vec3d startPositionSnappedToRail = this.snapPositionToRail(thisX, thisY, thisZ);

        thisY = startBlockPos.getY();
        boolean onPoweredRail = false;
        boolean currentlyBraking = false;
        if (state.isOf(Blocks.POWERED_RAIL)) {
            onPoweredRail = state.get(PoweredRailBlock.POWERED);
            currentlyBraking = !onPoweredRail;
        }

        double gravityAcceleration = 0.0078125;
        if (this.isTouchingWater()) {
            gravityAcceleration *= 0.2;
        }

        Vec3d momentum = this.getVelocity();
        RailShape railShape = getRailShape(state);
        boolean isAscending = railShape.isAscending();
        boolean isDiagonal = (railShape == RailShape.SOUTH_WEST || railShape == RailShape.NORTH_EAST ||
                railShape == RailShape.SOUTH_EAST || railShape == RailShape.NORTH_WEST);

        switch (railShape) {
            case ASCENDING_EAST -> {
                this.setVelocity(momentum.add(-gravityAcceleration, 0.0D, 0.0D));
                ++thisY;
            }
            case ASCENDING_WEST -> {
                this.setVelocity(momentum.add(gravityAcceleration, 0.0D, 0.0D));
                ++thisY;
            }
            case ASCENDING_NORTH -> {
                this.setVelocity(momentum.add(0.0D, 0.0D, gravityAcceleration));
                ++thisY;
            }
            case ASCENDING_SOUTH -> {
                this.setVelocity(momentum.add(0.0D, 0.0D, -gravityAcceleration));
                ++thisY;
            }
        }


        momentum = this.getVelocity();
        Pair<Vec3i, Vec3i> exitPair = getAdjacentRailPositionsByShape(railShape);
        Vec3i exitRelativePosition1 = exitPair.getFirst();
        Vec3i exitRelativePosition2 = exitPair.getSecond();
        // The exit relative X and Z here can be either -1, 0 or 1
        //
        // Example for an EAST_WEST rail would be:
        // exitRelPos1.getX() = -1
        // exitRelPos1.getZ() = 0
        // exitRelPos2.getX() = 1
        // exitRelPos2.getZ() = 0
        // Therefore
        // exitDiffX = 2
        // exitDiffZ = 0
        // exitHypotenuse = 4
        //
        // Example for an SOUTH_EAST rail would be:
        // exitRelPos1.getX() = 0
        // exitRelPos1.getZ() = 1
        // exitRelPos2.getX() = 1
        // exitRelPos2.getZ() = 0
        // Therefore
        // exitDiffX = 1
        // exitDiffZ = -1
        // exitHypotenuse = 1.414
        //
        // Note: exitDiffX and exitDiffY can be either -1, 0, 1 or 2
        // (-2 isn’t possible, I think the south-east rule starts here)
        //
        // By some magic, this works out to find the correct new velocity depending on incoming velocity and rail shape
        double exitDiffX = exitRelativePosition2.getX() - exitRelativePosition1.getX();
        double exitDiffZ = exitRelativePosition2.getZ() - exitRelativePosition1.getZ();
        double exitHypotenuse = Math.sqrt(exitDiffX * exitDiffX + exitDiffZ * exitDiffZ);
        double k = momentum.x * exitDiffX + momentum.z * exitDiffZ;
        // Every rail shape has a "forward" movement according to the diffs using the exits()
        // If it’s backwards the direction is flipped.
        boolean movementIsBackwards = k < 0.0D;
        if (movementIsBackwards) {
            exitDiffX = -exitDiffX;
            exitDiffZ = -exitDiffZ;
        }

        // Clamp to the max momentum
        double horizontalMomentum = Math.min(this.getVelocity().horizontalLength(), maxMomentum);
        // The horizontal speed is redistributed using the hypotenuse of the exit rail positions
        this.setVelocity(new Vec3d(horizontalMomentum * exitDiffX / exitHypotenuse, momentum.y, horizontalMomentum * exitDiffZ / exitHypotenuse));


        BlockPos exitPos;
        boolean exitIsAir;
        {
            BlockPos pos = isAscending ? startBlockPos.up() : startBlockPos;
            BlockPos exitPosition1 = pos.add(exitRelativePosition1);
            if (world.getBlockState(new BlockPos(exitPosition1.getX(), exitPosition1.getY() - 1, exitPosition1.getZ())).isIn(BlockTags.RAILS)) {
                exitPosition1 = exitPosition1.down();
            }
            BlockPos exitPosition2 = pos.add(exitRelativePosition2);
            if (world.getBlockState(new BlockPos(exitPosition2.getX(), exitPosition2.getY() - 1, exitPosition2.getZ())).isIn(BlockTags.RAILS)) {
                exitPosition2 = exitPosition2.down();
            }

            Vec3d posCenter = Vec3d.ofBottomCenter(pos);
            Vec3d exit1Center = Vec3d.ofBottomCenter(exitPosition1);
            Vec3d exit2Center = Vec3d.ofBottomCenter(exitPosition2);

            Vec3d momentumPos = posCenter.add(this.getVelocity()).multiply(1, 0, 1);
            exitPos = momentumPos.distanceTo(exit1Center.multiply(1, 0, 1)) < momentumPos.distanceTo(exit2Center.multiply(1, 0, 1)) ? exitPosition1 : exitPosition2;
            BlockState exitState = world.getBlockState(exitPos);
            exitIsAir = exitState.isOf(Blocks.AIR);
        }

        ArrayList<BlockPos> adjRailPositions = new ArrayList<>();
        Supplier<Double> calculateMaxSpeedForThisTick = () -> {

            double fallback = this.getMaxSpeed();

            if (!this.hasPassengers())
                return fallback;

            if (this.getVelocity().horizontalLength() < vanillaMaxSpeed)
                return fallback;

            if (!isEligibleFastRail(state))
                return fallback;

            HashSet<BlockPos> checkedPositions = new HashSet<>();
            checkedPositions.add(startBlockPos);


            BiFunction<BlockPos, RailShape, ArrayList<Pair<BlockPos, RailShape>>> checkNeighbors = (checkPos, checkRailShape) -> {
                Pair<Vec3i, Vec3i> nExitPair = getAdjacentRailPositionsByShape(checkRailShape);

                ArrayList<Pair<BlockPos, RailShape>> newNeighbors = new ArrayList<>();

                BlockPos sourcePos = checkRailShape.isAscending() ? checkPos.up() : checkPos;

                for (Vec3i nExitRelPos: List.of(nExitPair.getFirst(), nExitPair.getSecond())) {
                    BlockPos nPos = sourcePos.add(nExitRelPos);
                    if (world.getBlockState(new BlockPos(nPos.getX(), nPos.getY() - 1, nPos.getZ())).isIn(BlockTags.RAILS)) {
                        nPos = nPos.down();
                    }

                    if (checkedPositions.contains(nPos))
                        continue;

                    BlockState nState = world.getBlockState(nPos);
                    if (!isEligibleFastRail(nState))
                        return new ArrayList<>();

                    RailShape nShape = getRailShape(nState);
                    boolean sameDiagonal = (railShape == RailShape.SOUTH_WEST && nShape == RailShape.NORTH_EAST
                            || railShape == RailShape.NORTH_EAST && nShape == RailShape.SOUTH_WEST
                            || railShape == RailShape.SOUTH_EAST && nShape == RailShape.NORTH_WEST
                            || railShape == RailShape.NORTH_WEST && nShape == RailShape.SOUTH_EAST);

                    if (nShape != railShape && !sameDiagonal)
                        return new ArrayList<>();

                    checkedPositions.add(nPos);
                    adjRailPositions.add(nPos);
                    // Adding the neighbor rail shape currently has no use, since we abort on rail shape change anyway
                    // Code stays as is for now so we can differentiate between types of rail shape changes later
                    newNeighbors.add(Pair.of(nPos, nShape));
                }

                return newNeighbors;
            };


            ArrayList<Pair<BlockPos, RailShape>> newNeighbors = checkNeighbors.apply(startBlockPos, railShape);

            double checkFactor = (isDiagonal || isAscending) ? 2. : 1.;
            final int cutoffPoint = 3;
            int sizeToCheck = (int)(2 * (cutoffPoint + (checkFactor * maxSpeed)));
            sizeToCheck -= (sizeToCheck % 2);

            while (!newNeighbors.isEmpty() && adjRailPositions.size() < sizeToCheck) {
                ArrayList<Pair<BlockPos, RailShape>> tempNewNeighbors = new ArrayList<>(newNeighbors);
                newNeighbors.clear();

                for (Pair<BlockPos, RailShape> newNeighbor : tempNewNeighbors) {
                    ArrayList<Pair<BlockPos, RailShape>> result = checkNeighbors.apply(newNeighbor.getFirst(), newNeighbor.getSecond());

                    // Abort when one direction is empty
                    if (result.isEmpty()) {
                        newNeighbors.clear();
                        break;
                    }

                    newNeighbors.addAll(result);
                }
            }

            int railCountEachDirection = adjRailPositions.size() / 2;
            final double cutoffSpeedPerSec = 20.;
            switch (railCountEachDirection) {
                case 0:
                case 1:
                    return fallback;
                case 2:
                    return 12. / tps;
                case 3:
                    return cutoffSpeedPerSec / tps;
                default:
            }

            int railCountPastBegin = railCountEachDirection - cutoffPoint;
            return (cutoffSpeedPerSec + ((20. / checkFactor) * railCountPastBegin)) / tps;
        };

        double maxSpeedForThisTick = Math.min(calculateMaxSpeedForThisTick.get(), maxSpeed);
        if (isDiagonal || isAscending) {
            // Diagonal and Ascending/Descending is 1.4142 times faster, we correct this here
            maxSpeedForThisTick = Math.min(maxSpeedForThisTick, 0.7071 * maxSpeed);
        }

        // This allows the player to push the minecart a bit from inside
        // Basically, if there's a player inside with a velocity (from trying to move) and the minecart is slow, we accelerate a bit
        Entity entity = this.getFirstPassenger();
        if (entity instanceof PlayerEntity) {
            Vec3d playerVelocity = entity.getVelocity();
            double squaredPlayerVelocity = playerVelocity.horizontalLengthSquared();
            double squaredVelocity = this.getVelocity().horizontalLengthSquared();
            if (squaredPlayerVelocity > 1.0E-4D && squaredVelocity < 0.01D) {
                this.setVelocity(this.getVelocity().add(playerVelocity.x * 0.1D, 0.0D, playerVelocity.z * 0.1D));
                //We don't break when the player is trying to move the minecart
                currentlyBraking = false;
            }
        }

        /*
        Braking Algorithm
        Original:
            double o;
            if (bl2) {
                o = this.getVelocity().horizontalLength();
                if (o < 0.03) {
                    this.setVelocity(Vec3.ZERO);
                } else {
                    this.setVelocity(this.getVelocity().multiply(0.5, 0.0, 0.5));
                }
            }
         */
        if (currentlyBraking) {
            momentum = this.getVelocity();
            horizontalMomentum = momentum.horizontalLength();

            if (horizontalMomentum < 0.03) {
                this.setVelocity(Vec3d.ZERO);
            } else {
                //We brake extra hard if we go faster than vanilla
                if (horizontalMomentum > vanillaMaxSpeed) {
                    double ratioToSlowdown = vanillaMaxSpeed / horizontalMomentum;
                    this.setVelocity(momentum.multiply(ratioToSlowdown, 1., ratioToSlowdown));
                }

                double brakeFactor = 0.59;
                this.setVelocity(this.getVelocity().multiply(brakeFactor, 0., brakeFactor));
            }
        }

        double exitPos1X = (double) startBlockPos.getX() + 0.5D + (double) exitRelativePosition1.getX() * 0.5D;
        double exitPos1Z = (double) startBlockPos.getZ() + 0.5D + (double) exitRelativePosition1.getZ() * 0.5D;
        double exitPos2X = (double) startBlockPos.getX() + 0.5D + (double) exitRelativePosition2.getX() * 0.5D;
        double exitPos2Z = (double) startBlockPos.getZ() + 0.5D + (double) exitRelativePosition2.getZ() * 0.5D;
        exitDiffX = exitPos2X - exitPos1X;
        exitDiffZ = exitPos2Z - exitPos1Z;
        double snappedDistance;
        if (exitDiffX == 0.0D) {
            snappedDistance = thisZ - (double) startBlockPos.getZ();
        } else if (exitDiffZ == 0.0D) {
            snappedDistance = thisX - (double) startBlockPos.getX();
        } else {
            double a = thisX - exitPos1X;
            double b = thisZ - exitPos1Z;
            snappedDistance = (a * exitDiffX + b * exitDiffZ) * 2.0D;
        }

        thisX = exitPos1X + exitDiffX * snappedDistance;
        thisZ = exitPos1Z + exitDiffZ * snappedDistance;

        double movementAmount = this.hasPassengers() ? 0.75D : 1.0D;
        momentum = this.getVelocity();
        // The clamp here differentiates between momentum and actual allowed speed in this tick
        Vec3d movement = new Vec3d(
                MathHelper.clamp(movementAmount * momentum.x, -maxSpeedForThisTick, maxSpeedForThisTick),
                0.0D,
                MathHelper.clamp(movementAmount * momentum.z, -maxSpeedForThisTick, maxSpeedForThisTick));

        double extraY = 0;
        if (railShape.isAscending()) {
            if (exitPos.getY() > startBlockPos.getY()) {
                extraY = (int) (0.5 + movement.horizontalLength());
                thisY += extraY;
            }
        }

        this.setPos(thisX, thisY, thisZ);
        this.move(MovementType.SELF, movement);

//        System.out.println("Actual: " + movement.horizontalDistance()
//                + " " + level.getBlockState(new BlockPos(MathHelper.floor(this.getX()), MathHelper.floor(this.getY() - 2.), MathHelper.floor(this.getZ()))).is(BlockTags.RAILS)
//                + " " + level.getBlockState(new BlockPos(MathHelper.floor(this.getX()), MathHelper.floor(this.getY() - 1.), MathHelper.floor(this.getZ()))).is(BlockTags.RAILS)
//                + " " + level.getBlockState(new BlockPos(MathHelper.floor(this.getX()), MathHelper.floor(this.getY() - 0.), MathHelper.floor(this.getZ()))).is(BlockTags.RAILS)
//                + " " + level.getBlockState(new BlockPos(MathHelper.floor(this.getX()), MathHelper.floor(this.getY() + 1.), MathHelper.floor(this.getZ()))).is(BlockTags.RAILS));

        {
            // Snap down after extra snap ups on ascending rails
            // Also snap down on descending rails
            if (railShape.isAscending()
                    && !world.getBlockState(new BlockPos(MathHelper.floor(this.getX()), MathHelper.floor(this.getY()), MathHelper.floor(this.getZ()))).isIn(BlockTags.RAILS)
                    && !world.getBlockState(new BlockPos(MathHelper.floor(this.getX()), MathHelper.floor(this.getY() - 1.), MathHelper.floor(this.getZ()))).isIn(BlockTags.RAILS)) {

                if (world.getBlockState(new BlockPos(MathHelper.floor(this.getX()), MathHelper.floor(this.getY() - 2.), MathHelper.floor(this.getZ()))).isIn(BlockTags.RAILS)) {
                    this.setPos(this.getX(), this.getY() - 1, this.getZ());
                } else if (world.getBlockState(new BlockPos(MathHelper.floor(this.getX()), MathHelper.floor(this.getY() - 3.), MathHelper.floor(this.getZ()))).isIn(BlockTags.RAILS)) {
                    this.setPos(this.getX(), this.getY() - 2, this.getZ());
                }
            }

            // Old vanilla code to snap down on descending rails (only the descending exit had a different Y rel pos)
//        if (exitRelPos1.getY() != 0 && MathHelper.floor(this.getX()) - startPos.getX() == exitRelPos1.getX() && MathHelper.floor(this.getZ()) - startPos.getZ() == exitRelPos1.getZ()) {
//            this.setPos(this.getX(), this.getY() + (double) exitRelPos1.getY(), this.getZ());
//        } else if (exitRelPos2.getY() != 0 && MathHelper.floor(this.getX()) - startPos.getX() == exitRelPos2.getX() && MathHelper.floor(this.getZ()) - startPos.getZ() == exitRelPos2.getZ()) {
//            this.setPos(this.getX(), this.getY() + (double) exitRelPos2.getY(), this.getZ());
//        }
        }

        this.applySlowdown();

        // Compensate momentum for the snapping we did
        Vec3d endPositionSnappedToRail = this.snapPositionToRail(this.getX(), this.getY(), this.getZ());
        if (endPositionSnappedToRail != null && startPositionSnappedToRail != null) {
            double snappedToRailDiffPerTick = (startPositionSnappedToRail.y - endPositionSnappedToRail.y) / tps;
            momentum = this.getVelocity();
            horizontalMomentum = momentum.horizontalLength();
            if (horizontalMomentum > 0.0D) {
                this.setVelocity(momentum.multiply((horizontalMomentum + snappedToRailDiffPerTick) / horizontalMomentum, 1.0D, (horizontalMomentum + snappedToRailDiffPerTick) / horizontalMomentum));
            }
            this.setPos(this.getX(), endPositionSnappedToRail.y, this.getZ());
        }

        // If we passed over a block boundary, we clamp our horizontal momentum in some way
        // This seems to be an attempt at fixing block skips, from the original code
        int endBlockX = MathHelper.floor(this.getX());
        int endBlockZ = MathHelper.floor(this.getZ());
        if (endBlockX != startBlockPos.getX() || endBlockZ != startBlockPos.getZ()) {
            momentum = this.getVelocity();
            horizontalMomentum = momentum.horizontalLength();
            this.setVelocity(
                    horizontalMomentum * MathHelper.clamp(endBlockX - startBlockPos.getX(), -1.0D, 1.0D),
                    momentum.y,
                    horizontalMomentum * MathHelper.clamp(endBlockZ - startBlockPos.getZ(), -1.0D, 1.0D));
        }

        // Give speedup or kickstart when standing at block + powered rail
        if (onPoweredRail) {
            momentum = this.getVelocity();
            horizontalMomentum = momentum.horizontalLength();
            final double accelerationPerTick = 0.021D;
            if (horizontalMomentum > 0.01D) {
                if (this.hasPassengers()) {
                    // TODO: Rewrite the comment/naming so it makes more sense (very confusing since TPS is 20 and we can only skip 1 block with current speeds)

                    // Based on a 10 ticks per second basis spent per powered block we calculate a fair acceleration per tick
                    // due to spending less ticks per powered block on higher speeds (and even skipping blocks)
                    final double basisTicksPerSecond = 10.0D;
                    final double tickMovementForBasisTps = 1.0D / basisTicksPerSecond;
                    final double maxSkippedBlocksToConsider = 3.0D;

                    double acceleration = accelerationPerTick;
                    final double distanceMovedHorizontally = movement.horizontalLength();

                    if (distanceMovedHorizontally > tickMovementForBasisTps) {
                        acceleration *= Math.min((1.0D + maxSkippedBlocksToConsider) * basisTicksPerSecond, distanceMovedHorizontally / tickMovementForBasisTps);

                        // Add progressively slower (or faster) acceleration for higher speeds;
                        double highspeedFactor = 1.0D + MathHelper.clamp(-0.45D * (distanceMovedHorizontally / tickMovementForBasisTps / basisTicksPerSecond), -0.7D, 2.0D);
                        acceleration *= highspeedFactor;
                    }
                    this.setVelocity(momentum.add(acceleration * (momentum.x / horizontalMomentum), 0.0D, acceleration * (momentum.z / horizontalMomentum)));
                }
                else {
                    this.setVelocity(momentum.add(momentum.x / horizontalMomentum * 0.06D, 0.0D, momentum.z / horizontalMomentum * 0.06D));
                }
            } else {
                momentum = this.getVelocity();
                double xMomentum = momentum.x;
                double yMomentum = momentum.z;
                final double railStopperAcceleration = accelerationPerTick * 16.0D;
                if (railShape == RailShape.EAST_WEST) {
                    if (this.willHitBlockAt(startBlockPos.west())) {
                        xMomentum = railStopperAcceleration;
                    } else if (this.willHitBlockAt(startBlockPos.east())) {
                        xMomentum = -railStopperAcceleration;
                    }
                } else {
                    if (railShape != RailShape.NORTH_SOUTH) {
                        return;
                    }

                    if (this.willHitBlockAt(startBlockPos.north())) {
                        yMomentum = railStopperAcceleration;
                    } else if (this.willHitBlockAt(startBlockPos.south())) {
                        yMomentum = -railStopperAcceleration;
                    }
                }

                this.setVelocity(xMomentum, momentum.y, yMomentum);
            }
        }

        // TODO: Falling is still inconsistent, is it a vanilla inconsistency?
        // This would slow down to the vanilla max speed on exiting the rail
//        if (exitIsAir) {
//            momentum = this.getVelocity();
//            horizontalMomentum = momentum.horizontalDistance();
//            if (horizontalMomentum > vanillaMaxSpeed) {
//                double ratioToSlowdown = vanillaMaxSpeed / horizontalMomentum;
//                this.setVelocity(momentum.multiply(ratioToSlowdown, 1., ratioToSlowdown));
//            }
//        }
    }
}
