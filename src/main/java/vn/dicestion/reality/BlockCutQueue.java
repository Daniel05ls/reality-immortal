package vn.dicestion.reality;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Shared per-dimension budget, independent of the number of simultaneous slashes. */
public final class BlockCutQueue {
    private static final int MAX_PENDING=8192;
    private static final Map<ServerLevel,LinkedHashMap<BlockPos,BlockState>> QUEUES=new WeakHashMap<>();
    private BlockCutQueue() {}
    public static void enqueue(ServerLevel level,BlockPos pos,BlockState state) {
        var queue=QUEUES.computeIfAbsent(level,k->new LinkedHashMap<>());
        if(queue.size()<MAX_PENDING) queue.putIfAbsent(pos.immutable(),state);
    }
    public static void tick(ServerLevel level) {
        var queue=QUEUES.get(level);
        if(queue==null) return;
        long deadline=System.nanoTime()+2_000_000L;
        int checked=0,broken=0;
        var it=queue.entrySet().iterator();
        while(it.hasNext() && checked<256 && broken<64 && System.nanoTime()<deadline) {
            var entry=it.next();it.remove();checked++;
            BlockPos pos=entry.getKey();
            if(!level.hasChunkAt(pos)) continue;
            BlockState state=level.getBlockState(pos);
            if(state!=entry.getValue() || state.isAir() || state.liquid()) continue;
            var blockEntity=level.getBlockEntity(pos);
            if(!level.setBlock(pos,Blocks.AIR.defaultBlockState(),3)) continue;
            if(state.getDestroySpeed(level,pos)<0) {
                var item=state.getBlock().asItem();
                if(item!=Items.AIR) Block.popResource(level,pos,new ItemStack(item));
            } else {
                Block.dropResources(state,level,pos,blockEntity);
            }
            // No per-block break sound/particle storm; blade particles remain visible.
            broken++;
        }
        if(queue.isEmpty()) QUEUES.remove(level);
    }
    public static void clear() { QUEUES.clear(); }
}
