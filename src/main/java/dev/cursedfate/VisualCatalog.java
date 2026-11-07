package dev.cursedfate;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Shared, immutable art directions. No gameplay values are read from this catalog. */
public final class VisualCatalog {
    public record Profile(String gesture,List<String> moves,String domain,float tempo,float guard,float lean,float spread,int lead,String color) {
        public int tint(){return Integer.parseInt(color,16);}
    }
    public static final Map<String,Profile> ALL;
    static {
        try(var stream=Objects.requireNonNull(VisualCatalog.class.getResourceAsStream("/data/jjkcursedate/visuals.json"));
            var reader=new InputStreamReader(stream,StandardCharsets.UTF_8)){
            Map<String,Profile> data=new Gson().fromJson(reader,new TypeToken<LinkedHashMap<String,Profile>>(){}.getType());
            if(!data.keySet().equals(Catalog.BY_ID.keySet()))throw new IllegalStateException("Incomplete animation directions");
            for(var p:data.values())if(p.moves().size()!=3||p.tempo()<=0||Math.abs(p.lead())!=1)throw new IllegalStateException("Invalid animation profile");
            ALL=Collections.unmodifiableMap(data);
        }catch(Exception e){throw new ExceptionInInitializerError(e);}
    }
    public static Profile get(String path){return ALL.getOrDefault(path,ALL.get("yuji"));}
    public static String path(net.minecraft.world.entity.LivingEntity actor){
        return actor instanceof Sorcerer s?s.character().id():actor instanceof net.minecraft.world.entity.player.Player p?Progress.data(p).getString("path"):"";
    }
}
