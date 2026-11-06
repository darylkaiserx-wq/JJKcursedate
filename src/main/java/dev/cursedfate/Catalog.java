package dev.cursedfate;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class Catalog {
    public record Move(String name,String kind,int cost,int cooldown,int level,float power) {}
    public record Character(String id,String name,String role,String color,List<Move> moves,String domain) {
        public int tint(){return Integer.parseInt(color,16);}
        public boolean hostile(){return role.equals("boss")||role.equals("enemy");}
        public boolean civilian(){return role.equals("civilian");}
    }
    public static final List<Character> ALL;
    public static final Map<String,Character> BY_ID;
    static {
        try(var stream=Objects.requireNonNull(Catalog.class.getResourceAsStream("/data/jjkcursedate/roster.json"));
            var reader=new InputStreamReader(stream,StandardCharsets.UTF_8)) {
            ALL=List.of(new Gson().fromJson(reader,Character[].class));
            var map=new LinkedHashMap<String,Character>();
            for(var c:ALL) {
                if(map.put(c.id(),c)!=null || c.moves().size()!=3)throw new IllegalStateException("Invalid roster "+c.id());
            }
            BY_ID=Collections.unmodifiableMap(map);
        }catch(Exception e){throw new ExceptionInInitializerError(e);}
    }
    public static Character get(String id){return BY_ID.get(id);}
}
