package org.comroid.spiroid.api.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Contract;

import static java.lang.Math.*;
import static org.comroid.spiroid.api.util.MathUtil.raising;

public final class WorldUtil {
    //region Coordinate Array Indices
    public static final int X = 0;
    public static final int Y = 1;
    public static final int Z = 2;
    //endregion

    private WorldUtil() {
    }

    public static double dist$pythagoras(int[] pos1, int[] pos2) {
        return sqrt(pow(pos2[X] - pos1[X], 2) + pow(pos2[Z] - pos1[Z], 2));
    }

    public static int[] mid(int[][] pos) {
        return new int[]{
                MathUtil.mid(pos[X][X], pos[Z][X]),
                MathUtil.mid(pos[X][Y], pos[Z][Y]),
                MathUtil.mid(pos[X][Z], pos[Z][Z])
        };
    }

    public static boolean inside(int[][] area, int[] xyz) {
        return raising(min(area[X][X], area[Y][X]), xyz[X], max(area[X][X], area[Y][X]))
                && raising(min(area[X][Y], area[Y][Z]), xyz[Y], max(area[X][Y], area[Y][Z]))
                && raising(min(area[X][Z], area[Y][Z]), xyz[Z], max(area[X][Z], area[Y][Z]));
    }

    public static int[] xyz(Location location) {
        return new int[]{location.getBlockX(), location.getBlockY(), location.getBlockZ()};
    }

    public static Location location(World world, int[] xyz) {
        return world.getBlockAt(xyz[X], xyz[Y], xyz[Z]).getLocation();
    }

    @Contract(mutates = "param1")
    public static int[][] expandVert(int[][] positions) {
        positions[X][Y] = 0;
        positions[Y][Y] = 256;
        return positions;
    }

    public static int[][] retract(int[][] pos, int retractBy) {
        return new int[][]{
                new int[]{min(pos[X][X], pos[Y][X]) + retractBy, pos[X][Y], min(pos[X][Z], pos[Y][Z]) + retractBy},
                new int[]{max(pos[X][X], pos[Y][X]) - retractBy, pos[X][Y], max(pos[X][Z], pos[Y][Z]) - retractBy}
        };
    }

    public static int[][] sort(int[] pos1, int[] pos2) {
        return new int[][]{
                new int[]{min(pos1[X], pos2[X]), min(pos1[Y], pos2[Y]), min(pos1[Z], pos2[Z])},
                new int[]{max(pos1[X], pos2[X]), max(pos1[Y], pos2[Y]), max(pos1[Z], pos2[Z])}
        };
    }

    public static void breakDependent(Player player, Block block) {
        switch (player.getGameMode()) {
            case CREATIVE:
            case SPECTATOR:
                block.setType(Material.AIR);
                break;
            case SURVIVAL:
            case ADVENTURE:
                block.breakNaturally();
                break;
        }
    }

    @MagicConstant(valuesFromClass = ChestState.class)
    public static int chestState(Block block) {
        BlockState state = block.getState();
        if (state instanceof Chest) {
            Chest chest = (Chest) state;
            Inventory inventory = chest.getInventory();
            if (inventory instanceof DoubleChestInventory) {
                DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
                return ChestState.DOUBLE_CHEST;
            }
            return ChestState.SIMPLE_CHEST;
        }
        return ChestState.NO_CHEST;
    }

    public static boolean isExcludedWorld(Player player) {
        //if (Plugin.Permission.ADMIN.check(player, ""))
        return false;
        //return Plugin.getConfig("config")
        ////    .contains(player.getWorld().getName());
    }

    public final class ChestState {
        public static final int NO_CHEST = 0;
        public static final int SIMPLE_CHEST = 1;
        public static final int DOUBLE_CHEST = 2;
    }
}
