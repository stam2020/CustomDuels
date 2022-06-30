package plainplaying.plugin.duels;

import org.bukkit.entity.Player;

public class Fight {
    public Player player;
    public Player opponent;
    public int time;
    public boolean sameKit;
    public String kit1;
    public String kit2;
    public String world;
    public Fight(Player player, Player opponent, int time, boolean sameKit, String kit1, String kit2, String world){
        this.player = player;
        this.opponent = opponent;
        this.time = time;
        this.kit1 = kit1;
        this.kit2 = kit2;
        this.world = world;
    }
}
