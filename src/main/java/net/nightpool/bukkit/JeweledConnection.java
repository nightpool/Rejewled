package net.nightpool.bukkit;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.server.Chunk;
import net.minecraft.server.ChunkMap;
import net.minecraft.server.ChunkSection;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.INetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NibbleArray;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet14BlockDig;
import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.Packet56MapChunkBulk;
import net.minecraft.server.PlayerConnection;
import net.minecraft.server.World;

import org.bukkit.Material;

public class JeweledConnection extends PlayerConnection {
    private final int updateRadius = 2;
    public RejewledPlugin pl;
    public static byte[] buildBuffer = new byte[196864];
    public boolean firstPacket = true;
    static public Set<Integer> transparents = new HashSet<Integer>();
    static {
        for (Material i : Material.values()) {
            if (i.isTransparent()) {
                if(i.equals(Material.LAVA)){continue;}
                transparents.add(i.getId());
            }
        }
    }

    public JeweledConnection(RejewledPlugin p, MinecraftServer minecraftserver, INetworkManager networkmanager,
            EntityPlayer player) {
        super(minecraftserver, networkmanager, player);
        this.pl = p;
    }

    @Override
    public void a(Packet14BlockDig packet) {
        if (packet.e == 0x0 || packet.e == 0x2) { // If starting or finished a dig
            makeBlocksDirtyInRadius(player.world, packet.a, packet.b, packet.c, updateRadius);
        }
        super.a(packet);
    }

    @Override
    public void sendPacket(Packet packet) {
        if (packet instanceof Packet51MapChunk) {
            packet = new DejewledPacket51(this, (Packet51MapChunk) packet);
        } else if (packet instanceof Packet56MapChunkBulk) {
            packet = new DejewledPacket56(this, (Packet56MapChunkBulk) packet);
        }
        super.sendPacket(packet);
    }

    // Update the blocks in a radius around the punched block becuase of
    // the fact that lag can cause some ores not to show up right away thus
    // a player might miss them, by updating N blocks around it, there's a greater
    // chance the block will be updated before the player gets to it
    private void makeBlocksDirtyInRadius(World world, int x, int y, int z, int radius) {
        for (int a = x - radius; a <= x + radius; a++) {
            for (int b = y - radius; b <= y + radius; b++) {
                for (int c = z - radius; c <= z + radius; c++) {
                    if (a == x && b == y && c == z)
                        continue; // Skip the actual block we're hitting to prevent it from reappearing
                    if (pl.hiddenBlocks.contains(world.getTypeId(a, b, c))) { // Only update blocks that are obfuscated
                        world.notify(a, b, c); // Mark the block as dirty, so it's updated to the client, bypasses antixray check
                    }
                }
            }
        }
    }

    public ChunkMap djChunkMap(Chunk chunk, boolean flag, int paramInt) {
        int i = 0;
        ChunkSection[] arrayOfChunkSection = chunk.i();
        int j = 0;
        ChunkMap localChunkMap = new ChunkMap();
        byte[] arrayOfByte1 = buildBuffer;

        if (flag) {
            chunk.seenByPlayer = true;
        }
        int k;
        for (k = 0; k < arrayOfChunkSection.length; ++k)
            if ((arrayOfChunkSection[k] != null) && (((!(flag)) || (!(arrayOfChunkSection[k].isEmpty()))))
                    && ((paramInt & 1 << k) != 0)) {
                localChunkMap.b |= 1 << k;
                if (arrayOfChunkSection[k].getExtendedIdArray() != null) {
                    localChunkMap.c |= 1 << k;
                    ++j;
                }
            }
        byte[] byteObject;
        NibbleArray nibObject;
        for (k = 0; k < arrayOfChunkSection.length; ++k) {
            if ((arrayOfChunkSection[k] != null) && (((!(flag)) || (!(arrayOfChunkSection[k].isEmpty()))))
                    && ((paramInt & 1 << k) != 0)) {
                byteObject = replaceCoveredBlocks(chunk, arrayOfChunkSection[k]);
                System.arraycopy(byteObject, 0, arrayOfByte1, i, byteObject.length);
                i += byteObject.length;
            }
        }
        for (k = 0; k < arrayOfChunkSection.length; ++k) {
            if ((arrayOfChunkSection[k] != null) && (((!(flag)) || (!(arrayOfChunkSection[k].isEmpty()))))
                    && ((paramInt & 1 << k) != 0)) {
                nibObject = arrayOfChunkSection[k].getDataArray();
                System.arraycopy(nibObject.a, 0, arrayOfByte1, i, nibObject.a.length);
                i += nibObject.a.length;
            }
        }
        for (k = 0; k < arrayOfChunkSection.length; ++k) {
            if ((arrayOfChunkSection[k] != null) && (((!(flag)) || (!(arrayOfChunkSection[k].isEmpty()))))
                    && ((paramInt & 1 << k) != 0)) {
                nibObject = arrayOfChunkSection[k].getEmittedLightArray();
                System.arraycopy(nibObject.a, 0, arrayOfByte1, i, nibObject.a.length);
                i += nibObject.a.length;
            }
        }
        if (!(chunk.world.worldProvider.g)) {
            for (k = 0; k < arrayOfChunkSection.length; ++k) {
                if ((arrayOfChunkSection[k] != null) && (((!(flag)) || (!(arrayOfChunkSection[k].isEmpty()))))
                        && ((paramInt & 1 << k) != 0)) {
                    nibObject = arrayOfChunkSection[k].getSkyLightArray();
                    System.arraycopy(nibObject.a, 0, arrayOfByte1, i, nibObject.a.length);
                    i += nibObject.a.length;
                }
            }
        }

        if (j > 0) {
            for (k = 0; k < arrayOfChunkSection.length; ++k) {
                if ((arrayOfChunkSection[k] != null) && (((!(flag)) || (!(arrayOfChunkSection[k].isEmpty()))))
                        && (arrayOfChunkSection[k].getExtendedIdArray() != null) && ((paramInt & 1 << k) != 0)) {
                    nibObject = arrayOfChunkSection[k].getExtendedIdArray();
                    System.arraycopy(nibObject.a, 0, arrayOfByte1, i, nibObject.a.length);
                    i += nibObject.a.length;
                }
            }
        }

        if (flag) {
            byte[] arrayOfByte2 = chunk.m();
            System.arraycopy(arrayOfByte2, 0, arrayOfByte1, i, arrayOfByte2.length);
            i += arrayOfByte2.length;
        }

        localChunkMap.a = new byte[i];
        System.arraycopy(arrayOfByte1, 0, localChunkMap.a, 0, i);

        return ((ChunkMap) localChunkMap);
    }

    public boolean isBlockTransparent(World world, int x, int y, int z) {
        int blockType = world.getTypeId(x, y, z);
        return transparents.contains(blockType);
    }

    public byte[] replaceCoveredBlocks(Chunk chunk, ChunkSection section) {

        /*******
         * WARNING WARNING WARNING DO NOT FORGET TO CLONE THE BLOCK DATA FOR
         * THIS SECTION OTHERWISE YOU WILL OVERWRITE WORLD DATA WHEN SETTING TO
         * STONE WARNING WARNING WARNING
         *********/
        byte[] blockData = section.getIdArray().clone(); // Get the block data for this section
        // logF("blockData-before ", blockData);

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {

                    int worldX = (chunk.x << 4) + x;
                    int worldY = section.getYPosition() + y;
                    int worldZ = (chunk.z << 4) + z;

                    int type = section.getTypeId(x, y, z);
                    if (chunk.world.getTypeId(worldX, worldY, worldZ) != type) {
                        pl.getLogger().warning("Block type mismatch " + chunk.world.getTypeId(worldX, worldY, worldZ)
                                + " vs " + type);
                    }

                    if (pl.hiddenBlocks.contains(type)) {
                        CHECKTYPE: // Check to see if there is air around the block
                        {
                            if (isBlockTransparent(chunk.world, worldX + 1, worldY, worldZ))
                                break CHECKTYPE;
                            if (isBlockTransparent(chunk.world, worldX - 1, worldY, worldZ))
                                break CHECKTYPE;
                            if (isBlockTransparent(chunk.world, worldX, worldY + 1, worldZ))
                                break CHECKTYPE;
                            if (isBlockTransparent(chunk.world, worldX, worldY - 1, worldZ))
                                break CHECKTYPE;
                            if (isBlockTransparent(chunk.world, worldX, worldY, worldZ + 1))
                                break CHECKTYPE;
                            if (isBlockTransparent(chunk.world, worldX, worldY, worldZ - 1))
                                break CHECKTYPE;
                            blockData[y << 8 | z << 4 | x] = 1; // Set it to smooth stone
                        }
                    }
                }
            }
        }
        return blockData;
    }
}
