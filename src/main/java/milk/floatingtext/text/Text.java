package milk.floatingtext.text;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityMetadata;
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
        this.setNameTagVisible();
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

        if(!chunk.isGenerated()){
            chunk.setGenerated();
        }

        if(!chunk.isPopulated()){
            chunk.setPopulated();
        }

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
        if(k == null){
            return null;
        }

        k.spawnToAll();
        return (Text) k;
    }

    @Override
    public void spawnTo(Player player){
        if(
            !this.hasSpawned.containsKey(player.getLoaderId())
            && player.usedChunks.containsKey(Level.chunkHash(this.chunk.getX(), this.chunk.getZ()))
        ){
            AddPlayerPacket pk = new AddPlayerPacket();
            pk.uuid = UUID.randomUUID();
            pk.username = "";
            pk.eid = this.getId();
            pk.x = (float) this.x;
            pk.y = (float) (this.y - 1.62);
            pk.z = (float) this.z;
            pk.speedX = 0;
            pk.speedY = 0;
            pk.speedZ = 0;
            pk.yaw = 0;
            pk.pitch = 0;
            pk.metadata = new EntityMetadata()
                .putByte(Entity.DATA_FLAGS, 1 << Entity.DATA_FLAG_INVISIBLE)
                .putString(Entity.DATA_NAMETAG, "")
                .putBoolean(Entity.DATA_SHOW_NAMETAG, true)
                .putBoolean(Entity.DATA_NO_AI, true)
                .putLong(Entity.DATA_LEAD_HOLDER, -1)
                .putByte(Entity.DATA_LEAD, 0);
            pk.item = Item.get(Item.AIR);
            player.dataPacket(pk);

            this.hasSpawned.put(player.getLoaderId(), player);
        }
    }

    @Override
    protected void updateMovement(){

    }

    @Override
    public boolean entityBaseTick(int tickDiff){
        return false;
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
