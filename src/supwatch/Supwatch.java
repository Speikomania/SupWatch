package supwatch;

/**
 *
 * @author Kevin
 */
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class Supwatch extends JavaPlugin {

    SQL_Wrapper sql;
    Logger log;

    @Override
    public void onEnable() {
        super.onEnable();
        log = getLogger();
        sql = new SQL_Wrapper(this.getConfig(), log);
        try {
            sql.connectToDB();

        } catch (Exception ex) {
            log.info("No DB Connection");
            this.getPluginLoader().disablePlugin(this);
        }

    }

    @Override
    public void onDisable() {
        if (sql.isOpen()) {
            sql.close();
        }
        super.onDisable();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
        if (cmd.getLabel().equalsIgnoreCase("supwatch") && sender.hasPermission("supwatch.canwatch")) {
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("help")) {
                    sender.sendMessage("supwatch [Playername] [mute|kick|tjail|all]");
                } else {
                    if (!playerexist(args[0])) {
                        sender.sendMessage(String.format("Spieler %s existiert nicht", args[0]));
                    } else {
                        ResultSet rs = sql.read(args[0], args[1]);
                        try {
                            int count = 0;
                            while (rs.next()) {
                                count++;
                                sender.sendMessage(String.format(
                                        "%s %s %s %s %d %s",
                                        rs.getString("player"),
                                        rs.getString("cmd"),
                                        rs.getString("com"),
                                        rs.getString("sup"),
                                        rs.getInt("time"),
                                        rs.getTimestamp("cur_timestamp").toString()));
                            }
                            sender.sendMessage(String.format("Spieler %s hat %d Einträge", args[0], count));
                        } catch (SQLException ex) {
                            log.log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        } else {
            if (sender.hasPermission("supwatch.bewatch")) {
                String com = cmd.getLabel().toLowerCase();
                switch (com) {
                    case "mute":
                        if (args.length >= 2 && playerexist(args[0]) && Pattern.matches("\\d*", args[1])) {
                            sql.write(com, sender.getName(), args[0],args[2], Integer.getInteger(args[1]));
                        }
                        ;

                    case "kick":
                        if (args.length >= 2 && playerexist(args[0])) {
                            sql.write(com, sender.getName(), args[0],args[1], 0);
                        }
                        ;
                        break;
                    case "tjail":
                        if (args.length >= 3 && playerexist(args[0]) && Pattern.matches("\\d*", args[2])) {
                            sql.write(com, sender.getName(), args[0],args[3], Integer.getInteger(args[2]));
                        }
                        ;
                        break;
                    case "jail":
                        if (args.length >= 3 && playerexist(args[0]) && Pattern.matches("\\d*", args[2])) {
                            sql.write(com, sender.getName(), args[0],args[3], Integer.getInteger(args[2]));
                        }
                        ;
                        break;
                }

            }
        }
        return false;
    }

    public boolean playerexist(String name) {

        try {
            for (OfflinePlayer p : this.getServer().getOfflinePlayers()) {
                if (p.getName().equalsIgnoreCase(name)) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }
}
