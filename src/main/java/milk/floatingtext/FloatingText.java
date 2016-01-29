package milk.floatingtext;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;

public class FloatingText extends PluginBase implements Listener{

    @Override
    public void onEnable(){
        this.getServer().getLogger().info(TextFormat.GOLD + "[FloatingText]플러그인이 로드 되었습니다");
    }

}
