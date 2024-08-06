package cogi234.rebalance.util;

import cogi234.rebalance.PackedIceBlock;
import net.minecraft.block.*;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.sound.BlockSoundGroup;

public class ModBlocks {
    public static final Block PACKED_ICE = new PackedIceBlock(
                    AbstractBlock.Settings.create()
                            .mapColor(MapColor.PALE_PURPLE)
                            .instrument(NoteBlockInstrument.CHIME)
                            .slipperiness(0.98F)
                            .ticksRandomly()
                            .strength(0.5F)
                            .sounds(BlockSoundGroup.GLASS)
    );

}
