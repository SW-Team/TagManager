package milk.floatingtext.text;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.ByteEntityData;
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
import cn.nukkit.network.protocol.AddPlayerPacket;

import java.util.UUID;

public class Text extends Entity{

    Long timeout;

    public Text(FullChunk chunk, CompoundTag nbt, String text, Long second){
        super(chunk, nbt);

        this.timeout = second;
        this.setNameTag(text);
        this.setDataProperty(new ByteEntityData(DATA_NO_AI, (byte) 1));
        this.setDataProperty(new ByteEntityData(DATA_FLAGS, 1 << Entity.DATA_FLAG_INVISIBLE));
    }

    @Override
    public int getNetworkId(){
        return NETWORK_ID;
    }

    public static Text create(String text, Position pos){
        return create(text, pos, null);
    }

    public static Text create(String text, Position pos, Long second){
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

    @Override
    public void attack(EntityDamageEvent ev){}

    @Override
    public void heal(EntityRegainHealthEvent ev){}

    @Override
    public void spawnTo(Player player){
        if(!this.hasSpawned.containsKey(player.getLoaderId()) && player.usedChunks.containsKey(Level.chunkHash(this.chunk.getX(), this.chunk.getZ()))){
            AddPlayerPacket pk = new AddPlayerPacket();
            pk.uuid = UUID.randomUUID();
            pk.username = "";
            pk.eid = this.getId();
            pk.x = (float) this.x;
            pk.y = (float) (this.y - 1.62);
            pk.z = (float) this.z;
            pk.speedX = pk.speedY = pk.speedZ = pk.yaw = pk.pitch = 0;
            pk.metadata = this.dataProperties;
            pk.item = Item.get(Item.AIR);
            player.dataPacket(pk);

            this.hasSpawned.put(player.getLoaderId(), player);
        }
    }

    @Override
    protected void updateMovement(){}

    @Override
    public boolean entityBaseTick(int tickDiff){
        return true;
    }

    @Override
    public boolean onUpdate(int tick){
        if(this.timeout != null && --this.timeout < 0){
            this.close();
            return false;
        }
        return true;
    }

}
