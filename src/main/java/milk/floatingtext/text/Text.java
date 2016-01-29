package milk.floatingtext.text;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.ByteEntityData;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.AddPlayerPacket;

public class Text extends Entity{

    long timeout;

    public Text(FullChunk chunk, CompoundTag nbt, String text, Long second){
        super(chunk, nbt);

        this.timeout = second;
        this.setNameTag(text);
        this.setNameTagVisible();
        this.setDataProperty(DATA_NO_AI, new ByteEntityData((byte) 1));
    }

    @Override
    public int getNetworkId(){
        return NETWORK_ID;
    }

    public static Text create(String text, Position pos){
        return create(text, pos, Long.MAX_VALUE);
    }

    public static Text create(String text, Position pos, long second){
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
            AddEntityPacket pk = new AddEntityPacket();
            pk.eid = this.getId();
            pk.type = EntityItem.NETWORK_ID;
            pk.x = (float) this.x;
            pk.y = (float) this.y - 0.75f;
            pk.z = (float) this.z;
            pk.speedX = pk.speedY = pk.speedZ = 0;
            pk.yaw = (float) this.yaw;
            pk.pitch = (float) this.pitch;
            pk.metadata = this.dataProperties;
            player.dataPacket(pk);

            this.hasSpawned.put(player.getLoaderId(), player);
        }
    }

    @Override
    protected void updateMovement(){

    }

    @Override
    public boolean onUpdate(int tick){
        if(--this.timeout < 0){
            this.close();
            return false;
        }
        return true;
    }

}
