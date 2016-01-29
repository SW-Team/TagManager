package milk.floatingtext;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import milk.floatingtext.text.Text;

import java.util.HashMap;

public class FloatingText extends PluginBase implements Listener{

    HashMap<String, String> data = new HashMap<>();

    @Override
    public void onEnable(){
        Entity.registerEntity(Text.class, true);
        this.getServer().getLogger().info(TextFormat.GOLD + "[FloatingText]플러그인이 로드 되었습니다");
    }

    @EventHandler
    public void onPlayerBlockEvent(PlayerInteractEvent ev){
        if(ev.getFace() == 255 || ev.getAction() != PlayerInteractEvent.RIGHT_CLICK_BLOCK){
            return;
        }

        Player player = ev.getPlayer();
        /*if(!data.containsKey(player.getName().toLowerCase())){
            return;
        }*/

        Position pos = ev.getBlock().getSide(ev.getFace());
        Text.create(/*data.get(player.getName().toLowerCase())*/"스트리이잉", pos);
        System.out.println("시바아아아아앍");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        String output = "[FloatingText]";
        sender.sendMessage(output);
        return true;
    }

}
