package com.github.psacserve.Moderate;

import com.github.psacserve.BanServer;
import com.github.psacserve.Response.BanEntry;
import org.apache.commons.lang3.RandomStringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.LinkedList;

public class Ban
{
    public static void ban(String uuid, String reason, Date date)
    {
        try (Connection connection = BanServer.bans.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement("INSERT INTO ban VALUES (?, ?, ?, ?, ?, ?)"))
        {
            statement.setString(1, uuid.replace("-", ""));
            statement.setString(2, RandomStringUtils.randomAlphanumeric(8));
            statement.setString(3, String.valueOf(new Date().getTime()));
            statement.setString(4, reason);
            statement.setString(5, date == null ? "_PERM": String.valueOf(date.getTime()));
            statement.setInt(6, 0);
            statement.execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    public static void pardon(String player)
    {

        try (Connection banC = BanServer.bans.getConnection();
             Connection logC = BanServer.log.getConnection();
             PreparedStatement del =
                     banC.prepareStatement("DELETE FROM ban WHERE UUID=?");
             PreparedStatement found = banC.prepareStatement("SELECT BANID, DATE, REASON, EXPIRE, STAFF FROM ban WHERE UUID=?");)
        {
            del.setString(1, player.replace("-", ""));
            found.setString(1, player.replace("-", ""));
            ResultSet set = found.executeQuery();
            while (set.next())
            {
                try(PreparedStatement in = logC.prepareStatement("INSERT INTO ban VALUES (?, ?, ?, ?, ?, ?)"))
                {
                    in.setString(1, set.getString("UUID"));
                    in.setString(2, set.getString("BANID"));
                    in.setString(3, set.getString("DATE"));
                    in.setString(4, set.getString("REASON"));
                    in.setString(5, String.valueOf(new Date().getTime()));
                    in.setInt(6, Integer.parseInt(set.getString("STAFF")));
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public static BanEntry getBan(String uuid)
    {
        BanEntry ban = null;

        try (Connection connection = BanServer.bans.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT BANID, REASON, EXPIRE, STAFF FROM ban WHERE UUID=?"))
        {
            statement.setString(1, uuid.replace("-", ""));
            ResultSet set = statement.executeQuery();
            if (set.next())
            {
                ban = new BanEntry();
                ban.id = set.getString("BANID");
                ban.reason = set.getString("REASON");
                ban.expire = set.getString("EXPIRE");
                ban.hasStaff = set.getInt("STAFF") == 1;
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
             PreparedStatement statement = connection.prepareStatement("SELECT BANID, REASON, STAFF, UNBANDATE FROM ban WHERE UUID=?"))
        {
            statement.setString(1, uuid.replace("-", ""));
            ResultSet set = statement.executeQuery();
            while (set.next())
            {
                final BanEntry ban = new BanEntry();
                ban.id = set.getString("BANID");
                ban.reason = set.getString("REASON");
                ban.unbannedDate = set.getString("UNBANDATE");
                ban.hasStaff = set.getInt("STAFF") == 1;
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
