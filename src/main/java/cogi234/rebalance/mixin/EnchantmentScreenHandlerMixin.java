package cogi234.rebalance.mixin;

import cogi234.rebalance.Cogi234sRebalance;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Util;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(EnchantmentScreenHandler.class)
public abstract class EnchantmentScreenHandlerMixin extends ScreenHandler {
    protected EnchantmentScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Shadow @Final
    private Inventory inventory;
    @Shadow @Final
    private ScreenHandlerContext context;
    @Shadow @Final
    private  Random random;
    @Shadow @Final
    private Property seed;
    @Shadow @Final
    public int[] enchantmentPower;
    @Shadow @Final
    public int[] enchantmentId;
    @Shadow @Final
    public int[] enchantmentLevel;

    @Shadow
    protected abstract List<EnchantmentLevelEntry> generateEnchantments(DynamicRegistryManager registryManager, ItemStack itemStack, int jx, int i);

    /**
     * @author cogi234
     * @reason I'm changing how the enchanting table works
     */
    @Overwrite
    public void onContentChanged(Inventory inventory) {
        if (inventory == this.inventory) {
            ItemStack itemStack = inventory.getStack(0);
            if (!itemStack.isEmpty() && itemStack.isEnchantable()) {
                this.context.run((world, pos) -> {
                    IndexedIterable<RegistryEntry<Enchantment>> indexedIterableEnchantmentRegistry = world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getIndexedEntries();
                    int bookshelfNumber = 0;

                    for (BlockPos blockPos : EnchantingTableBlock.POWER_PROVIDER_OFFSETS) {
                        if (EnchantingTableBlock.canAccessPowerProvider(world, pos, blockPos)) {
                            bookshelfNumber++;
                        }
                    }

                    this.random.setSeed((long)this.seed.get());

                    for (int slot = 0; slot < 3; slot++) {
                        this.enchantmentPower[slot] = newCalculateRequiredExperienceLevel(this.random, slot, bookshelfNumber, itemStack);
                        this.enchantmentId[slot] = -1;
                        this.enchantmentLevel[slot] = -1;
                    }

                    for (int slot = 0; slot < 3; slot++) {
                        if (this.enchantmentPower[slot] > 0) {
                            List<EnchantmentLevelEntry> list = this.generateEnchantments(world.getRegistryManager(), itemStack, slot, this.enchantmentPower[slot]);
                            if (list != null && !list.isEmpty()) {
                                EnchantmentLevelEntry enchantmentLevelEntry = (EnchantmentLevelEntry)list.get(this.random.nextInt(list.size()));
                                this.enchantmentId[slot] = indexedIterableEnchantmentRegistry.getRawId(enchantmentLevelEntry.enchantment);
                                this.enchantmentLevel[slot] = enchantmentLevelEntry.level;
                            }
                        }
                    }

                    this.sendContentUpdates();
                });
            } else {
                for (int i = 0; i < 3; i++) {
                    this.enchantmentPower[i] = 0;
                    this.enchantmentId[i] = -1;
                    this.enchantmentLevel[i] = -1;
                }
            }
        }
    }

    @ModifyVariable(method = "onButtonClick", at = @At("STORE"), ordinal = 1)
    private int changeEnchantCost(int originalCost) {
        return Cogi234sRebalance.CONFIG.enchantingCost();
    }

    @Unique
    private int newCalculateRequiredExperienceLevel(Random random, int slotIndex, int bookshelfCount, ItemStack stack) {
        Item item = stack.getItem();
        int enchantability = item.getEnchantability();
        if (enchantability <= 0) {
            return 0;
        } else {
            if (bookshelfCount > 15) {
                bookshelfCount = 15;
            }

            return random.nextInt(5) + bookshelfCount + 1;
        }
    }
}
