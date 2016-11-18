package milk.floatingtext.text;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.LongEntityData;
import cn.nukkit.entity.data.StringEntityData;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityRegainHealthEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.AddItemEntityPacket;

public class Text extends Entity{

    private long timeout;

    public Text(FullChunk chunk, CompoundTag nbt, String text, long second){
        super(chunk, nbt);
        this.timeout = second;

        long flags = 0;
        flags |= 1 << DATA_FLAG_IMMOBILE;
        flags |= 1 << DATA_FLAG_CAN_SHOW_NAMETAG;
        flags |= 1 << DATA_FLAG_ALWAYS_SHOW_NAMETAG;
        this.setDataProperty(new LongEntityData(DATA_FLAGS, flags), false);
        this.setDataProperty(new StringEntityData(DATA_NAMETAG, text), false);
    }

    public static Text create(String text, Position pos){
        return create(text, pos, -1);
    }

    public static Text create(String text, Position pos, long second){
        FullChunk chunk = pos.getLevel().getChunk(((int) pos.x) >> 4, ((int) pos.z) >> 4, true);
        if(chunk == null){
            return null;
        }

        chunk.setGenerated();
        chunk.setPopulated();

        CompoundTag nbt = new CompoundTag()
            .putList(new ListTag<DoubleTag>("Pos")
                .add(new DoubleTag("", pos.x))
                .add(new DoubleTag("", pos.y))
                .add(new DoubleTag("", pos.z)))
            .putList(new ListTag<DoubleTag>("Motion")
                .add(new DoubleTag("", 0))
                .add(new DoubleTag("", 0))
                .add(new DoubleTag("", 0)))
            .putList(new ListTag<FloatTag>("Rotation")
                .add(new FloatTag("", 0))
                .add(new FloatTag("", 0)));

        Entity k = Entity.createEntity("Text", chunk, nbt, text, second);
        if(k == null || k.closed){
            return null;
        }

        k.spawnToAll();

        return (Text) k;
    }

    public void setTimeout(long timeout){
        this.timeout = Math.max(0, timeout);
    }

    public int getNetworkId(){
        return NETWORK_ID;
    }

    public void attack(EntityDamageEvent ev){}

    public void heal(EntityRegainHealthEvent ev){}

    public void spawnTo(Player player){
        if(!this.hasSpawned.containsKey(player.getLoaderId()) && player.usedChunks.containsKey(Level.chunkHash(this.chunk.getX(), this.chunk.getZ()))){
            this.hasSpawned.put(player.getLoaderId(), player);

            AddItemEntityPacket pk = new AddItemEntityPacket();
            pk.entityUniqueId = this.getId();
            pk.entityRuntimeId = this.getId();
            pk.x = (float) this.x;
            pk.y = (float) (this.y - 0.2);
            pk.z = (float) this.z;
            pk.speedX = pk.speedY = pk.speedZ = 0;
            pk.item = Item.get(Item.AIR);
            player.dataPacket(pk);

            this.sendData(player);
        }
    }

    protected void updateMovement(){}

    public boolean entityBaseTick(int tickDiff){
        return true;
    }

    public boolean onUpdate(int tick){
        if(this.timeout != -1 && --this.timeout < 0){
            this.close();
            return false;
        }
        return true;
    }

}
