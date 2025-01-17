package com.forgeessentials.core.misc;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// TODO (upgrade) : This should probably be completely replaced!

public class BlockPortalSize
{

    private final World field_150867_a;
    private final Direction.Axis field_150865_b;
    private final Direction field_150866_c;
    private final Direction field_150863_d;
    private BlockPos field_150861_f;
    private int field_150862_g;
    private int field_150868_h;

    public BlockPortalSize(World worldIn, BlockPos p_i45694_2_, Direction.Axis p_i45694_3_)
    {
        this.field_150867_a = worldIn;
        this.field_150865_b = p_i45694_3_;

        if (p_i45694_3_ == Direction.Axis.X)
        {
            this.field_150863_d = Direction.EAST;
            this.field_150866_c = Direction.WEST;
        }
        else
        {
            this.field_150863_d = Direction.NORTH;
            this.field_150866_c = Direction.SOUTH;
        }

        for (BlockPos blockpos1 = p_i45694_2_; p_i45694_2_.getY() > blockpos1.getY() - 21 && p_i45694_2_.getY() > 0
                && this.func_150857_a(worldIn.getBlockState(p_i45694_2_.below()).getBlock()); p_i45694_2_ = p_i45694_2_.below())
        {
            ;
        }

        int i = this.func_180120_a(p_i45694_2_, this.field_150863_d) - 1;

        if (i >= 0)
        {
            this.field_150861_f = p_i45694_2_.of(BlockPos.offset((long) i, this.field_150863_d));
            this.field_150868_h = this.func_180120_a(this.field_150861_f, this.field_150866_c);

            if (this.field_150868_h < 2 || this.field_150868_h > 21)
            {
                this.field_150861_f = null;
                this.field_150868_h = 0;
            }
        }

        if (this.field_150861_f != null)
        {
            this.field_150862_g = this.func_150858_a();
        }
    }

    protected int func_180120_a(BlockPos p_180120_1_, Direction p_180120_2_)
    {
        int i;

        for (i = 0; i < 22; ++i)
        {
            BlockPos blockpos1 = p_180120_1_.of(BlockPos.offset((long) i, p_180120_2_));

            if (!this.func_150857_a(this.field_150867_a.getBlockState(blockpos1).getBlock())
                    || this.field_150867_a.getBlockState(blockpos1.below()).getBlock() != Blocks.OBSIDIAN)
            {
                break;
            }
        }

        Block block = this.field_150867_a.getBlockState(p_180120_1_.of(BlockPos.offset((long) i, p_180120_2_))).getBlock();
        return block == Blocks.OBSIDIAN ? i : 0;
    }

    protected int func_150858_a()
    {
        int i;
        label56:

        for (this.field_150862_g = 0; this.field_150862_g < 21; ++this.field_150862_g)
        {
            for (i = 0; i < this.field_150868_h; ++i)
            {
                BlockPos blockpos = this.field_150861_f.of(BlockPos.offset((long) i, this.field_150866_c)).above(this.field_150862_g);
                Block block = this.field_150867_a.getBlockState(blockpos).getBlock();

                if (!this.func_150857_a(block))
                {
                    break label56;
                }

                if (block == Blocks.NETHER_PORTAL)
                {
                }

                if (i == 0)
                {
                    block = this.field_150867_a.getBlockState(blockpos.offset(this.field_150863_d)).getBlock();

                    if (block != Blocks.OBSIDIAN)
                    {
                        break label56;
                    }
                }
                else if (i == this.field_150868_h - 1)
                {
                    block = this.field_150867_a.getBlockState(blockpos.offset(this.field_150866_c)).getBlock();

                    if (block != Blocks.OBSIDIAN)
                    {
                        break label56;
                    }
                }
            }
        }

        for (i = 0; i < this.field_150868_h; ++i)
        {
            if (this.field_150867_a.getBlockState(this.field_150861_f.offset(this.field_150866_c, i).above(this.field_150862_g)).getBlock() != Blocks.OBSIDIAN)
            {
                this.field_150862_g = 0;
                break;
            }
        }

        if (this.field_150862_g <= 21 && this.field_150862_g >= 3)
        {
            return this.field_150862_g;
        }
        else
        {
            this.field_150861_f = null;
            this.field_150868_h = 0;
            this.field_150862_g = 0;
            return 0;
        }
    }

    protected boolean func_150857_a(Block p_150857_1_)
    {
        return p_150857_1_.getMaterial(p_150857_1_.defaultBlockState()) == Material.AIR || p_150857_1_ == Blocks.FIRE || p_150857_1_ == Blocks.NETHER_PORTAL;
    }

    public boolean func_150860_b()
    {
        return this.field_150861_f != null && this.field_150868_h >= 2 && this.field_150868_h <= 21 && this.field_150862_g >= 3 && this.field_150862_g <= 21;
    }

    public void func_150859_c()
    {
        for (int i = 0; i < this.field_150868_h; ++i)
        {
            BlockPos blockpos = this.field_150861_f.of(BlockPos.offset((long) i, this.field_150866_c));

            for (int j = 0; j < this.field_150862_g; ++j)
            {
                this.field_150867_a.setBlockState(blockpos.above(j),
                        Blocks.NETHER_PORTAL.defaultBlockState().withProperty(NetherPortalBlock.AXIS, this.field_150865_b), 2);
            }
        }
    }

}