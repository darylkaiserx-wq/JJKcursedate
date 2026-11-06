package dev.cursedfate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public final class Progress {
    public static CompoundTag data(Player p){
        var root=p.getPersistentData();
        if(!root.contains(Player.PERSISTED_NBT_TAG))root.put(Player.PERSISTED_NBT_TAG,new CompoundTag());
        var persisted=root.getCompound(Player.PERSISTED_NBT_TAG);
        if(!persisted.contains(CursedFate.ID))persisted.put(CursedFate.ID,new CompoundTag());
        return persisted.getCompound(CursedFate.ID);
    }
    public static int mastery(int uses){return Math.min(10,1+(int)Math.sqrt(Math.max(0,uses)/8.0));}
    public static int level(int xp){return Math.min(60,1+(int)Math.sqrt(Math.max(0,xp)/20.0));}
    public static int level(Player p){return level(data(p).getInt("xp"));}
    public static boolean heavenly(Player p){String path=data(p).getString("path");return path.equals("maki")||path.equals("toji");}
    public static int maxEnergy(Player p){if(heavenly(p))return 0;return 100+level(p)*4+data(p).getInt("rank")*20;}
    public static String rank(int rank){return new String[]{"Grade 4","Grade 3","Grade 2","Grade 1","Special Grade"}[Math.max(0,Math.min(4,rank))];}
    public static String rank(Player p){return rank(data(p).getInt("rank"));}
    public static int requiredLevel(int rank){return new int[]{1,8,18,32,48}[Math.max(0,Math.min(4,rank))];}
    public static boolean pay(Player p,int amount){var d=data(p);float ce=d.getFloat("energy");if(ce<amount)return false;d.putFloat("energy",ce-amount);return true;}
    public static void award(Player p,int xp){var d=data(p);d.putInt("xp",Math.min(1000000,d.getInt("xp")+Math.max(0,xp)));}
    public static Catalog.Character character(Player p){return Catalog.get(data(p).getString("path"));}
}
