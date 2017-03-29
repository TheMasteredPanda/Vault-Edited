package net.milkbowl.vault.economy.plugins;

import net.dungeons.DEconomy;
import net.dungeons.bank.BankEntry;
import net.dungeons.bank.BankManager;
import net.dungeons.config.MainConfig;
import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by TheMasteredPanda on 22/03/2017.
 */

public class Economy_DEconomy extends AbstractEconomy
{
    private static final Logger logger = Logger.getLogger("Minecraft");
    private DEconomy instance;
    private BankManager bankManager;
    private MainConfig mainConfig;
    private EconomyResponse bankManagerNotLoadedResponse = new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "BankManager isn't properly loaded.");
    private Plugin plugin;
    private final String name = "DEconomy";

    public Economy_DEconomy(Plugin plugin)
    {
        System.out.println("Invoked Economy_DEconomy");
        this.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(new Economy_DEconomy.EconomyServerListener(this), plugin);

        if (this.instance == null) {
            Plugin dEconomy = plugin.getServer().getPluginManager().getPlugin(this.name);

            if (dEconomy != null && dEconomy.isEnabled()) {
                this.instance = (DEconomy) dEconomy;
                logger.info(String.format("[%s][Economy] %s hooked.", plugin.getDescription().getName(), name));
            }
        }
    }

    public class EconomyServerListener implements Listener
    {
        Economy_DEconomy economy = null;

        public EconomyServerListener(Economy_DEconomy economy) {
            this.economy = economy;
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPluginEnable(PluginEnableEvent event) {
            if (economy.instance == null) {
                Plugin cex = event.getPlugin();

                if (cex.getDescription().getName().equals(economy.name)) {
                    economy.instance = (DEconomy) cex;
                    economy.bankManager = economy.instance.getBankManager();
                    economy.mainConfig = economy.instance.getConfigManager().getMainConfig();
                    logger.info(String.format("[%s][Economy] %s hooked.", plugin.getDescription().getName(), economy.name));
                }
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPluginDisable(PluginDisableEvent event) {
            if (economy.instance != null) {
                if (event.getPlugin().getDescription().getName().equals(economy.name)) {
                    economy.instance = null;
                    logger.info(String.format("[%s][Economy] %s unhooked.", plugin.getDescription().getName(), economy.name));
                }
            }
        }
    }

    @Override
    public boolean isEnabled()
    {
        return instance != null;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public boolean hasBankSupport()
    {
        return this.mainConfig.getBankSupport();
    }

    @Override
    public int fractionalDigits()
    {
        return 1;
    }

    @Override
    public String format(double v)
    {
        return v + "xp";
    }

    @Override
    public String currencyNamePlural()
    {
        return "xp";
    }

    @Override
    public String currencyNameSingular()
    {
        return "xp";
    }

    @Override
    public boolean hasAccount(String s)
    {
        return true;
    }

    @Override
    public boolean hasAccount(String s, String s1)
    {
        return true;
    }

    @Override
    public double getBalance(String s)
    {
        return Bukkit.getPlayer(s).getLevel();
    }

    @Override
    public double getBalance(String s, String s1)
    {
        return Bukkit.getPlayer(s).getLevel();
    }

    @Override
    public boolean has(String s, double v)
    {
        return Bukkit.getPlayer(s).getLevel() >= v;
    }

    @Override
    public boolean has(String s, String s1, double v)
    {
        return Bukkit.getPlayer(s).getExp() >= v;
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, double v)
    {
        Player player = Bukkit.getPlayer(s);

        if (player.getLevel() < v) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player " + s + " does not have enough money to withdraw " + this.format(v) + ".");
        }

        player.setLevel((int) (player.getLevel() - v));
        return new EconomyResponse(v, player.getLevel(), EconomyResponse.ResponseType.SUCCESS, "Successfully withdrew " + this.format(v) + ".");
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, String s1, double v)
    {
        Player player = Bukkit.getPlayer(s);

        if (player.getLevel() < v) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player " + s + " does not have enough money to withdraw " + this.format(v) + ".");
        }

        player.setLevel((int) (player.getLevel() - v));
        return new EconomyResponse(v, player.getLevel(), EconomyResponse.ResponseType.SUCCESS, "Successfully withdrew " + this.format(v) + ".");
    }

    @Override
    public EconomyResponse depositPlayer(String s, double v)
    {
        Player player = Bukkit.getPlayer(s);
        player.setLevel((int) (player.getLevel() + v));
        return new EconomyResponse(v, player.getLevel(), EconomyResponse.ResponseType.SUCCESS, "Successfully deposited " + this.format(v) + ".");
    }

    @Override
    public EconomyResponse depositPlayer(String s, String s1, double v)
    {
        Player player = Bukkit.getPlayer(s);
        player.setLevel((int) (player.getLevel() + v));
        return new EconomyResponse(v, player.getLevel(), EconomyResponse.ResponseType.SUCCESS, "Successfully deposited " + this.format(v) + ".");
    }

    @Override
    public EconomyResponse createBank(String s, String s1)
    {
        if (this.bankManager == null) {
            System.out.println("BankManager is null.");
            return this.bankManagerNotLoadedResponse;
        }
        Player player = Bukkit.getPlayer(s1);

        List<BankEntry> bankEntries = this.bankManager.getBanksOwnedBy(player.getUniqueId());

        if (bankEntries != null) {
            for (BankEntry entry : bankEntries) {
                System.out.println("In for loop.");

                if (entry.getName().endsWith(s1)) {
                    System.out.println("Entry:" + entry.getName() + " Requested Name: " + s);
                    return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player already owns an account named " + s + ".");
                }
            }
        }

        this.bankManager.createBank(player.getUniqueId(), s);
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.SUCCESS, "Successfully made bank " + s + ".");
    }

    @Override
    public EconomyResponse deleteBank(String s)
    {
        if (!this.bankManager.getAllBankNames(false).contains(s)) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Bank account not found.");
        }

        this.bankManager.removeBank(s);
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.SUCCESS, "Successfully deleted bank " + s + ".");
    }

    @Override
    public EconomyResponse bankBalance(String s)
    {
        if (!this.bankManager.getAllBankNames(false).contains(s)) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Bank account not found.");
        }

        return new EconomyResponse(0, this.bankManager.getBalance(s), EconomyResponse.ResponseType.SUCCESS, "Successfully retrieved bank balance for bank " + s + ".");
    }

    @Override
    public EconomyResponse bankHas(String s, double v)
    {
        if (!this.bankManager.getAllBankNames(false).contains(s)) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Bank account not found.");
        }

        if (this.bankBalance(s).balance < v) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Bank doesn't have enough money.");
        }

        return new EconomyResponse(v, this.bankManager.getBalance(s), EconomyResponse.ResponseType.SUCCESS, "Bank has more money than " + this.format(v) + ".");
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v)
    {
        if (!this.bankManager.getAllBankNames(false).contains(s)) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Bank account not found.");
        }

        this.bankManager.setBalance(s, this.bankManager.getBalance(s) - v);
        return new EconomyResponse(v, this.bankManager.getBalance(s), EconomyResponse.ResponseType.SUCCESS, "Successfully withdrew " + this.format(v) + ".");
    }

    @Override
    public EconomyResponse bankDeposit(String s, double v)
    {
        if (!this.bankManager.getAllBankNames(false).contains(s)) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Bank account not found.");
        }

        this.bankManager.setBalance(s, this.bankManager.getBalance(s) + v);
        return new EconomyResponse(v, this.bankManager.getBalance(s), EconomyResponse.ResponseType.SUCCESS, "Successfully deposited " + this.format(v) + ".");
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s1)
    {
        if (!this.bankManager.getAllBankNames(false).contains(s)) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Bank account not found.");
        }

        BankEntry entry = this.bankManager.getBankEntry(s);
        this.instance.debug("isBankOwner(" + s + ", " + s1 + ") : BankEntry is: " + entry);

        if (!entry.getOwner().equals(Bukkit.getPlayer(s1).getUniqueId())) {
            this.instance.debug("isBankOwner(" + s + ", " + s1 + ") : Player is not the owner of bank.");
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player " + s1 + " is not the owner of bank " + s + ".");
        }

        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.SUCCESS, "Player " + s1 + " is the owner of bank " + s + ".");
    }

    @Override
    public EconomyResponse isBankMember(String s, String s1)
    {
        if (!this.bankManager.getAllBankNames(false).contains(s)) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Bank account not found.");
        }

        BankEntry entry = this.bankManager.getBankEntry(s);

        if (!entry.getMembers().contains(Bukkit.getPlayer(s1).getUniqueId()) && this.isBankOwner(s, Bukkit.getPlayer(entry.getOwner())).type == EconomyResponse.ResponseType.FAILURE) {
            this.instance.debug("isBankMember(" + s + ", " + s1 + ") : Player is not the a member of bank.");
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player " + s1 + " is not a member of bank " + s + ".");
        }

        return new EconomyResponse(0,0, EconomyResponse.ResponseType.SUCCESS, "Player " + s1 + " is a member of bank " + s + ".");
    }

    @Override
    public List<String> getBanks()
    {
        return this.bankManager.getAllBankNames(false);
    }

    @Override
    public boolean createPlayerAccount(String s)
    {
        return true;
    }

    @Override
    public boolean createPlayerAccount(String s, String s1)
    {
        return true;
    }
}
