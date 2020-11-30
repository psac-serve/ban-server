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

    public static void pardon(String player, String pardonnedBy, String pardonReason)
    {
        try (Connection ban = BanServer.bans.getConnection();
             PreparedStatement banLp = ban.prepareStatement("SELECT BANNEDBY, UUID, BANID, DATE, REASON, STAFF, EXPIRE FROM ban WHERE UUID=?");
             Connection log = BanServer.log.getConnection())
        {
            banLp.setString(1, player);
            ResultSet set = banLp.executeQuery();
            if (!set.next())
                return;
            SQLModifier.insert(log, "log",
                    set.getString("UUID"),
                    set.getString("BANID"),
                    set.getString("DATE"),
                    set.getString("REASON"),
                    set.getString("EXPIRE"),
                    new Date().getTime(),
                    set.getString("STAFF"),
                    set.getString("BANNEDBY"),
                    pardonnedBy,
                    pardonReason
            );
            SQLModifier.delete(ban, "ban", new HashMap<String, String>()
            {{
                put("UUID", player);
            }});
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
             PreparedStatement statement = connection.prepareStatement("SELECT UUID, BANNEDBY, BANID, REASON, EXPIRE, STAFF, DATE FROM ban WHERE UUID=?"))
        {
            statement.setString(1, uuid.replace("-", ""));
            ResultSet set = statement.executeQuery();
            if (set.next())
            {
                ban = getEntryFromResultSet(false, set);
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
             PreparedStatement statement = connection.prepareStatement("SELECT UUID, UNBANREASON, BANNEDBY, UNBANNEDBY, BANID, REASON, STAFF, UNBANDATE, DATE, EXPIRE FROM log WHERE UUID=?"))
        {
            statement.setString(1, uuid.replace("-", ""));
            ResultSet set = statement.executeQuery();
            while (set.next())
                bans.add(getEntryFromResultSet(true, set));

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

    public static BanEntry getEntryFromResultSet(final boolean ex, final ResultSet set)
    {
        final BanEntry ban = new BanEntry();

        try
        {
            ban.id = set.getString("BANID");
            ban.reason = set.getString("REASON");
            ban.bannedDate = set.getLong("DATE");
            ban.unBanned = ex;
            ban.expire = set.getString("EXPIRE").equals("_PERM") ? null: set.getLong("EXPIRE");
            ban.bannedBy = set.getString("BANNEDBY");
            ban.uuid = set.getString("UUID");
            if (ex)
            {
                final String unban = set.getString("UNBANDATE");
                ban.unbannedDate = unban == null || unban.equals("") ? null: Long.parseLong(unban);
                ban.hasStaff = set.getInt("STAFF") == 1;
                ban.unBannedBy = set.getString("UNBANNEDBY");
                ban.unBanReason = set.getString("UNBANREASON");

            }
        }
        catch (Exception ignored) { }
        return ban;
    }

    public static LinkedList<BanEntry> getBansFromID(String banid)
    {
        final LinkedList<BanEntry> bans = new LinkedList<>();
        try (final Connection connection = BanServer.log.getConnection();
             final PreparedStatement statement = connection.prepareStatement("SELECT UUID, UNBANREASON, BANNEDBY, UNBANNEDBY, BANID, REASON, STAFF, UNBANDATE, DATE, EXPIRE FROM log WHERE BANID=?");
             final Connection cs = BanServer.bans.getConnection();
             PreparedStatement bs = cs.prepareStatement("SELECT UUID, BANNEDBY, BANID, REASON, EXPIRE, STAFF, DATE FROM ban WHERE BANID=?"))
        {
            statement.setString(1, banid);
            bs.setString(1, banid);
            final ResultSet set = statement.executeQuery();
            if (set.next())
                bans.add(getEntryFromResultSet(true, set));
            final ResultSet bss = bs.executeQuery();
            if (bss.next())
                bans.add(getEntryFromResultSet(false, bss));

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return bans;

    }

    public static LinkedList<BanEntry> getBansDate(Long before, Long after)
    {
        final LinkedList<BanEntry> bans = new LinkedList<>();
        try (final Connection connection = BanServer.log.getConnection();
             final PreparedStatement statement = connection.prepareStatement("SELECT UUID, UNBANREASON, BANNEDBY, UNBANNEDBY, BANID, REASON, STAFF, UNBANDATE, DATE, EXPIRE FROM log WHERE DATE BETWEEN ?" + (before != null ? " AND ?": ""));
             final Connection cs = BanServer.bans.getConnection();
             PreparedStatement bs = cs.prepareStatement("SELECT UUID, BANNEDBY, BANID, REASON, EXPIRE, STAFF, DATE FROM ban WHERE DATE BETWEEN ?" + (before != null ? " AND ?": "")))
        {
            if (before != null)
                statement.setLong(2, before);
            else
                statement.setLong(2, 0);
            if (after != null)
                statement.setLong(1, after);
            else
                statement.setLong(1, 0);

            if (before != null)
                bs.setLong(2, before);
            else
                bs.setLong(2, 0);
            if (after != null)
                bs.setLong(1, after);
            else
                bs.setLong(1, 0);
            final ResultSet set = statement.executeQuery();
            while (set.next())
                bans.add(getEntryFromResultSet(true, set));
            final ResultSet bss = bs.executeQuery();
            while (bss.next())
                bans.add(getEntryFromResultSet(false, bss));

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return bans;

    }
}
