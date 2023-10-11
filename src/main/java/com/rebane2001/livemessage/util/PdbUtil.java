package com.rebane2001.livemessage.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rebane2001.livemessage.Livemessage;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PdbUtil {

    private static final Map<String, int[]> decorations = Map.ofEntries(
            Map.entry("catgirl_chief", new int[]{16029848}), // F49898
            Map.entry("cogs", new int[]{12802333}), // C3591D
            Map.entry("cookie", new int[]{14327683}), // DA9F83
            Map.entry("daytime", new int[]{16755763}), // FFAC33
            Map.entry("df_kanin", new int[]{14716019}), // E08C73
            Map.entry("df_plume", new int[]{12245429}), // BAD9B5
            Map.entry("donator_aurora", new int[]{1636506, 12731374}), // 18F89A -> C243EE
            Map.entry("donator_blossom", new int[]{16034746}), // F4ABBA
            Map.entry("donator_ribbon", new int[]{14495300}), // DD2E44
            Map.entry("donator_star", new int[]{16634468}), // FDD264
            Map.entry("donator_strawberry", new int[]{12458289}), // BE1931
            Map.entry("donator_warmth", new int[]{16634468, 15422291}), // FDD264 -> EB5353
            Map.entry("nighttime", new int[]{2372469}), // 66757F ! 243375 (overridden so it doesn't look like offline)
            Map.entry("pride_bi", new int[]{14185132, 14185132, 9797037, 6990317, 6990317}), // D872AC -> D872AC -> 957DAD -> 6AA9ED -> 6AA9ED
            Map.entry("pride_lesbian", new int[]{15431258, 16493428, 16777215, 15898060, 10707080}), // EB765A -> FBAB74 -> FFF -> F295CC -> A36088
            Map.entry("pride_pan", new int[]{16745137, 16249995, 16249995, 9163257}), // FF82B1 -> F7F48B -> F7F48B -> 8BD1F9
            Map.entry("pride_trans", new int[]{6804714, 16561843, 16777215, 16561843, 6804714}), // 67D4EA -> FCB6B3 -> FFF -> FCB6B3 -> 67D4EA
            Map.entry("pride", new int[]{16022652, 16761448, 16249995, 10608275, 7381463, 9797037}) // F47C7C -> FFC268 -> F7F48B -> A1DE93 -> 70A1D7 -> 957DAD
    );
    private static final Map<String, String> pronouns = Map.ofEntries(
            Map.entry("he", "he/him"),
            Map.entry("it", "it/its"),
            Map.entry("she", "she/her"),
            Map.entry("they", "they/them"),
            Map.entry("any", "any pronouns"),
            Map.entry("ask", "ask for pronouns"),
            Map.entry("avoid", "avoid pronouns"),
            Map.entry("other", "other pronouns")
    );

    public static class PdbData {
        public String pronoun;
        public String decoration;
        public boolean fresh = false;
    }

    public static void fetchData(PdbData pdbData, UUID uuid) {
        CompletableFuture
                .runAsync(() -> {
                    try {
                        Livemessage.LOGGER.warn("Looking up pronouns!");
                        JsonElement jsonElement = LivemessageUtil.getResources(new URL("https://pronoundb.org/api/v2/lookup?platform=minecraft&ids=" + uuid.toString()), "GET");
                        pdbData.decoration = null;
                        pdbData.pronoun = null;
                        pdbData.fresh = true;
                        if (jsonElement.getAsJsonObject().isEmpty()) return;
                        Collection<JsonElement> values = jsonElement.getAsJsonObject().asMap().values();
                        if (values.isEmpty()) return;
                        JsonObject pronounData = values.stream().findFirst().get().getAsJsonObject();
                        pdbData.decoration = pronounData.get("decoration").isJsonNull() ? null : pronounData.get("decoration").getAsString();
                        pdbData.pronoun = pronounData.get("sets").getAsJsonObject().get("en").getAsJsonArray().get(0).getAsString();
                    } catch (com.google.gson.JsonSyntaxException | java.net.UnknownHostException e) {
                        Livemessage.LOGGER.warn("Couldn't retrieve pronouns due to a network error");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    public static String getPronounText(String pronoun) {
        return pronouns.getOrDefault(pronoun, "unknown pronouns");
    }

    public static int[] getDecorationColors(String decoration) {
        return decorations.getOrDefault(decoration, new int[]{0});
    }
}
