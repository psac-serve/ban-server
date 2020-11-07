package com.github.psacserve.Moderate;

import com.github.psacserve.BanServer;
import com.github.psacserve.Response.BanEntry;
import develop.p2p.lib.SQLModifier;
import org.apache.commons.lang3.RandomStringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

public class Ban
{
    public static void ban(String uuid, String bannedBy, String reason, Date date, boolean staff)
    {
        try (Connection connection = BanServer.bans.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement("INSERT INTO ban VALUES (?, ?, ?, ?, ?, ?, ?)"))
        {
            statement.setString(1, uuid.replace("-", ""));
            statement.setString(2, RandomStringUtils.randomAlphanumeric(8));
            statement.setString(3, String.valueOf(new Date().getTime()));
            statement.setString(4, reason);
            statement.setString(5, date == null ? "_PERM": String.valueOf(date.getTime()));
            statement.setInt(6, staff ? 0: 1);
            statement.setString(7, bannedBy);
            statement.execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    public static void pardon(String player, String pardonnedBy)
    {
        try(Connection ban = BanServer.bans.getConnection();
            PreparedStatement banLp = ban.prepareStatement("SELECT BANNEDBY, UUID, BANID, DATE, REASON, STAFF, EXPIRE FROM ban WHERE UUID=?");
            Connection log = BanServer.log.getConnection())
        {
            banLp.setString(1, player);
            ResultSet set = banLp.executeQuery();
            if (!set.next())
                return;
            SQLModifier.insert(log, "ban",
                    set.getString("UUID"),
                    set.getString("BANID"),
                    set.getString("DATE"),
                    set.getString("REASON"),
                    set.getString("EXPIRE"),
                    new Date().getTime(),
                    set.getString("STAFF"),
                    set.getString("BANNEDBY"),
                    pardonnedBy
            );
            SQLModifier.delete(ban, "ban", new HashMap<String, String>(){{put("UUID", player);}});
        }
        catch (Exception e)
        {
            BanServer.printStackTrace(e);
        }

    }

    public static BanEntry getBan(String uuid)
    {
        BanEntry ban = null;

        try (Connection connection = BanServer.bans.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT BANNEDBY, BANID, REASON, EXPIRE, STAFF, DATE FROM ban WHERE UUID=?"))
        {
            statement.setString(1, uuid.replace("-", ""));
            ResultSet set = statement.executeQuery();
            if (set.next())
            {
                final String expire = set.getString("EXPIRE");

                ban = new BanEntry();
                ban.id = set.getString("BANID");
                ban.reason = set.getString("REASON");
                ban.expire = expire.equals("_PERM") ? null: Long.parseLong(expire);
                ban.bannedDate = set.getLong("DATE");
                ban.unbannedDate = null;
                ban.unBanned = false;
                ban.hasStaff = set.getInt("STAFF") == 1;
                ban.bannedBy = set.getString("BANNEDBY");
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        return ban;
    }

    public static LinkedList<BanEntry> getBans(String uuid)
    {
        LinkedList<BanEntry> bans = new LinkedList<>();

        try (Connection connection = BanServer.log.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT BANNEDBY, UNBANNEDBY, BANID, REASON, STAFF, UNBANDATE, DATE, EXPIRE FROM ban WHERE UUID=?"))
        {
            statement.setString(1, uuid.replace("-", ""));
            ResultSet set = statement.executeQuery();
            while (set.next())
            {
                final BanEntry ban = new BanEntry();
                ban.id = set.getString("BANID");
                ban.reason = set.getString("REASON");
                ban.bannedDate = set.getLong("DATE");
                final String unban = set.getString("UNBANDATE");

                ban.unbannedDate = unban == null || unban.equals("") ? null: Long.parseLong(unban);
                ban.hasStaff = set.getInt("STAFF") == 1;
                ban.unBanned = true;
                ban.expire = set.getString("EXPIRE").equals("_PERM") ? null: set.getLong("EXPIRE");
                ban.bannedBy = set.getString("BANNEDBY");
                ban.unBannedBy = set.getString("UNBANNEDBY");
                bans.add(ban);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        BanEntry entry = getBan(uuid);

        if (entry != null)
            bans.add(entry);

        return bans;
    }

}
