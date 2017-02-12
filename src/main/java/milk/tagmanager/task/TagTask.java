package milk.tagmanager.task;

import cn.nukkit.Server;
import milk.tagmanager.text.Tag;

import java.util.ArrayList;

public class TagTask implements Runnable{

    public void run(){
        if(Server.getInstance().getOnlinePlayers().isEmpty()){
            return;
        }

        for(Tag tag : new ArrayList<>(Tag.list.values())) tag.onUpdate();
    }

}