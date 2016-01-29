package milk.floatingtext;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import milk.floatingtext.text.Text;

import java.util.HashMap;

public class FloatingText extends PluginBase implements Listener{

    HashMap<String, String> data = new HashMap<>();

    @Override
    public void onEnable(){
        Entity.registerEntity(Text.class, true);
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getLogger().info(TextFormat.GOLD + "[FloatingText]플러그인이 로드 되었습니다");
    }

    @EventHandler
    public void onPlayerBlockEvent(PlayerInteractEvent ev){
        if(ev.getFace() == 255 || ev.getAction() != PlayerInteractEvent.RIGHT_CLICK_BLOCK){
            return;
        }

        Player player = ev.getPlayer();
        if(!data.containsKey(player.getName().toLowerCase())){
            return;
        }
        Text.create(data.get(player.getName().toLowerCase()), ev.getBlock());
    }

    @Override
    public boolean onCommand(CommandSender k, Command cmd, String label, String[] args){
        String output = "[FloatingText]";
        if(!(k instanceof Player)){
            k.sendMessage(output + "게임에서 입력해주세요");
            return true;
        }

        Player sender = (Player) k;
        switch(args.length){
            case 1:
                output += "텍스트를 띄울곳을 터치해주세요";
                data.put(sender.getName().toLowerCase(), args[0]);
                break;
            case 4:
            case 5:
                Position pos = new Position(Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]));
                if(args.length == 5){
                    pos.level = Server.getInstance().getLevelByName(args[4]);
                }
                if(pos.level == null){
                    pos.level = sender.level;
                }
                Text.create(args[0], pos);
                output += "텍스트를 성공적으로 띄웠어요";
                break;
            default:
                output = cmd.getUsage();
                break;
        }
        sender.sendMessage(output);
        return true;
    }

}
