package milk.tagmanager.text;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.entity.data.LongEntityData;
import cn.nukkit.entity.data.StringEntityData;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.network.protocol.AddItemEntityPacket;
import cn.nukkit.network.protocol.RemoveEntityPacket;
import cn.nukkit.network.protocol.SetEntityDataPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Tag extends Position{

    public static HashMap<Long, Tag> list = new HashMap<>();

    private long id;
    private long timeout = -1;

    public boolean closed = false;

    private FullChunk chunk = null;

    protected double lastX = 0;
    protected double lastY = 0;
    protected double lastZ = 0;

    protected Map<Integer, Player> hasSpawned = new HashMap<>();

    protected final EntityMetadata dataProperties = new EntityMetadata()
        .putLong(Entity.DATA_FLAGS, 0)
        .putShort(Entity.DATA_AIR, 400)
        .putShort(Entity.DATA_MAX_AIR, 400)
        .putString(Entity.DATA_NAMETAG, "")
        .putLong(Entity.DATA_LEAD_HOLDER_EID, -1)
        .putFloat(Entity.DATA_SCALE, 1f);

    private ArrayList<String> hidePlayer = new ArrayList<>();

    public Tag(Position pos, String text){
        this(pos, text, null);
    }

    public Tag(Position pos, String text, Long second){
        FullChunk chunk;
        if(text.isEmpty() || !pos.isValid() || (chunk = pos.level.getChunk((int) pos.x >> 4, (int) pos.z >> 4, true)) == null){
            return;
        }

        this.id = 100;//TODO

        list.put(this.id, this);
        if(second != null){
            this.timeout = second;
        }

        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.level = pos.level;

        this.chunk = chunk;

        long flags = 0;
        flags |= 1 << Entity.DATA_FLAG_IMMOBILE;
        flags |= 1 << Entity.DATA_FLAG_CAN_SHOW_NAMETAG;
        flags |= 1 << Entity.DATA_FLAG_ALWAYS_SHOW_NAMETAG;
        this.dataProperties.put(new LongEntityData(Entity.DATA_FLAGS, flags));
        this.dataProperties.put(new StringEntityData(Entity.DATA_NAMETAG, text));
    }

    public String getText(){
        return this.dataProperties.getString(Entity.DATA_NAMETAG);
    }

    public void onUpdate(){
        if(this.closed){
            return;
        }

        if(this.timeout != -1 && --this.timeout < 0){
            this.close();
            return;
        }
        this.updateMovement();

        this.hasSpawned.forEach((id, player) -> {
            if(player.closed || !player.usedChunks.containsKey(Level.chunkHash(this.chunk.getX(), this.chunk.getZ()))){
                this.despawnFrom(player);
            }
        });
        this.level.getChunkPlayers((int) this.x >> 4, (int) this.z >> 4).forEach((id, player) -> this.spawnTo(player));
    }

    public void setTimeout(long second){
        this.timeout = Math.max(0, second) * 20;
    }

    public void setHidePlayer(Player player){
        this.despawnFrom(player);
        this.hidePlayer.add(player.getName());
    }

    public void updateMovement(){
        if(this.lastX != this.x || this.lastY != this.y || this.lastZ != this.z){
            this.lastX = this.x;
            this.lastY = this.y;
            this.lastZ = this.z;
            this.level.addEntityMovement(this.chunk.getX(), this.chunk.getZ(), this.id, this.x, this.y, this.z, 0, 0, 0);
        }
    }

    public void spawnTo(Player player){
        if(!this.hidePlayer.contains(player.getName()) && !this.hasSpawned.containsKey(player.getLoaderId()) && player.usedChunks.containsKey(Level.chunkHash(this.chunk.getX(), this.chunk.getZ()))){
            this.hasSpawned.put(player.getLoaderId(), player);

            AddItemEntityPacket pk = new AddItemEntityPacket();
            pk.entityUniqueId = this.id;
            pk.entityRuntimeId = this.id;
            pk.x = (float) this.x;
            pk.y = (float) (this.y - 0.4);
            pk.z = (float) this.z;
            pk.speedX = pk.speedY = pk.speedZ = 0;
            pk.item = Item.get(Item.AIR);
            player.dataPacket(pk);

            SetEntityDataPacket pk2 = new SetEntityDataPacket();
            pk2.eid = this.id;
            pk2.metadata = this.dataProperties;
            player.dataPacket(pk2);
        }
    }

    public void despawnFrom(Player player){
        if(this.hasSpawned.containsKey(player.getLoaderId())){
            if(player.isOnline()){
                RemoveEntityPacket pk = new RemoveEntityPacket();
                pk.eid = this.id;
                player.dataPacket(pk);
            }
            this.hasSpawned.remove(player.getLoaderId());
        }
    }

    public void close(){
        list.remove(this.id);

        this.closed = true;
        this.hasSpawned.forEach((id, player) -> this.despawnFrom(player));
    }

}
