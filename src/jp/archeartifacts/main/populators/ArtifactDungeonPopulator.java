package jp.archeartifacts.main.populators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import jp.archeartifacts.main.ArcheArtifacts;
import jp.archeartifacts.main.controllers.ArtifactController;

public class ArtifactDungeonPopulator extends BlockPopulator {
	
	List<Material> blockTypes;
	ArcheArtifacts plugin;
	public ArtifactDungeonPopulator() {
		plugin = ArcheArtifacts.getPlugin();
		blockTypes = new ArrayList<>(Arrays.asList(Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS,
				Material.CRACKED_STONE_BRICKS, Material.CRACKED_STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.MOSSY_STONE_BRICKS,
				Material.POLISHED_ANDESITE, Material.INFESTED_STONE_BRICKS, Material.INFESTED_MOSSY_STONE_BRICKS, Material.INFESTED_CRACKED_STONE_BRICKS));
	}

	@Override
	public void populate(World world, Random random, Chunk chunk) {
		
		ArtifactController artifactController = plugin.getArtifactController();
		if ((random.nextDouble() * 100) < artifactController.buriedReliquaryChance) {
			final int DEFAULT_AXIS_OFFSET = 1;
			int xOffset = random.nextInt(6) + 1;
			int zOffset = random.nextInt(6) + 1;
			int yMaxHeight = chunk.getChunkSnapshot().getHighestBlockYAt(xOffset + DEFAULT_AXIS_OFFSET + 4, zOffset + DEFAULT_AXIS_OFFSET + 4);
			int yOffset = Math.max(1, random.nextInt(yMaxHeight - 16) + 1);
			
			//Check corners, at least 6 need to be in solid blocks
			int solidCorners = 0;
			Predicate<Block> solidTest = solid -> solid.getType().isSolid();
			
			//Bottom Corners
			solidCorners = (solidTest.test(chunk.getBlock(xOffset + 8, yOffset, zOffset + 8)) ? ++solidCorners : solidCorners);
			solidCorners = (solidTest.test(chunk.getBlock(xOffset, yOffset, zOffset + 8)) ? ++solidCorners : solidCorners);
			solidCorners = (solidTest.test(chunk.getBlock(xOffset + 8, yOffset, zOffset)) ? ++solidCorners : solidCorners);
			solidCorners = (solidTest.test(chunk.getBlock(xOffset, yOffset, zOffset)) ? ++solidCorners : solidCorners);
			
			//Top Corners
			solidCorners = (solidTest.test(chunk.getBlock(xOffset + 8, yOffset + 10, zOffset + 8)) ? ++solidCorners : solidCorners);
			solidCorners = (solidTest.test(chunk.getBlock(xOffset, yOffset + 10, zOffset + 8)) ? ++solidCorners : solidCorners);
			solidCorners = (solidTest.test(chunk.getBlock(xOffset + 8, yOffset + 10, zOffset)) ? ++solidCorners : solidCorners);
			solidCorners = (solidTest.test(chunk.getBlock(xOffset, yOffset + 10, zOffset)) ? ++solidCorners : solidCorners);
			
			if (solidCorners >= 6) {
				for(int x = 0; x < 9; x++) {
					for(int z = 0; z < 9; z++) {
						for(int y = 0; y < 11; y++) {
							Block loopBlock = chunk.getBlock(x + xOffset, y + yOffset, z + zOffset);
							if ( (y == 0 || y == 10) || (x == 0 || x == 8) || (z == 0 || z == 8) ) { 
								Material blockMaterial = blockTypes.get(ThreadLocalRandom.current().nextInt(12));
								loopBlock.setType(blockMaterial, false);
							}
							else { loopBlock.setType(Material.AIR, false); }
						}
					}
				}
				
				//Tree base
				chunk.getBlock(xOffset + 3, yOffset + 1, zOffset + 3).setType(Material.STONE_BRICKS, false);
				chunk.getBlock(xOffset + 4, yOffset + 1, zOffset + 3).setType(Material.STONE_BRICKS, false);
				chunk.getBlock(xOffset + 5, yOffset + 1, zOffset + 3).setType(Material.STONE_BRICKS, false);
				
				chunk.getBlock(xOffset + 3, yOffset + 1, zOffset + 4).setType(Material.STONE_BRICKS, false);
				chunk.getBlock(xOffset + 4, yOffset + 1, zOffset + 4).setType(Material.DIRT, false);
				world.generateTree(new Location(world, (chunk.getX() * 16) + (xOffset + 4), yOffset + 2, (chunk.getZ() * 16) + (zOffset + 4)), TreeType.TREE);
				chunk.getBlock(xOffset + 5, yOffset + 1, zOffset + 4).setType(Material.STONE_BRICKS, false);
				
				chunk.getBlock(xOffset + 3, yOffset + 1, zOffset + 5).setType(Material.STONE_BRICKS, false);
				chunk.getBlock(xOffset + 4, yOffset + 1, zOffset + 5).setType(Material.STONE_BRICKS, false);
				chunk.getBlock(xOffset + 5, yOffset + 1, zOffset + 5).setType(Material.STONE_BRICKS, false);
				
				//Chest
				Block chestBlock = chunk.getBlock(xOffset + 4, yOffset + 5, zOffset + 4);
				chestBlock.setType(Material.CHEST, false);
				Chest chest = (Chest)chestBlock.getState();
				chest.update();
				Inventory chestInventory = chest.getBlockInventory();
				ItemStack randomArtifact = artifactController.generateArtifact();
				chestInventory.setItem(13, randomArtifact);
				
				//Mob Spawner
				Block spawnerBlock = chunk.getBlock(xOffset + 4, yOffset, zOffset + 4);
				spawnerBlock.setType(Material.SPAWNER, false);
				CreatureSpawner spawner = (CreatureSpawner)spawnerBlock.getState();
				spawner.setSpawnedType(EntityType.SKELETON);
				PersistentDataContainer dataContainer = spawner.getPersistentDataContainer();
				dataContainer.set(new NamespacedKey(ArcheArtifacts.getPlugin(), "RELIQUARY_SPAWNER"), PersistentDataType.BYTE, (byte)1);
				spawner.update();
			}
		}
	}
}
