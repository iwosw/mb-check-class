package com.talhanation.bannermod.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;

import java.util.Locale;
import java.util.Set;

public final class BannerModNpcNamePool {
    private static final String[] GIVEN_NAMES = {
            "Aldric", "Alwin", "Ansel", "Arnulf", "Baldric", "Bennet", "Berengar", "Beric", "Brandt", "Cedric",
            "Corwin", "Darian", "Edric", "Edwin", "Eldric", "Emeric", "Falk", "Garrick", "Geoff", "Geralt",
            "Godric", "Hadrian", "Halric", "Harwin", "Hector", "Ivo", "Jareth", "Kael", "Lambert", "Leoric",
            "Lucan", "Marek", "Matthias", "Niall", "Odo", "Osric", "Percival", "Quentin", "Roderic", "Roland",
            "Rowan", "Soren", "Stellan", "Theobald", "Tristan", "Ulric", "Valen", "Warin", "Wilhelm", "Yorick",
            "Adela", "Aelina", "Alis", "Ameline", "Beatrice", "Brienne", "Cateline", "Cecily", "Clarice", "Daria",
            "Eira", "Elaine", "Elise", "Fenna", "Gwen", "Helena", "Isolde", "Joan", "Kaela", "Liora",
            "Maren", "Matilda", "Meriel", "Nerys", "Odette", "Petra", "Rosalind", "Sabine", "Seren", "Talia"
    };

    private static final String[] FAMILY_NAMES = {
            "Ashdown", "Barrow", "Blackmere", "Briar", "Brookfield", "Claymoor", "Coldbrook", "Crowell", "Dale", "Dunmere",
            "Eastmarch", "Faircroft", "Fenwood", "Frostford", "Glen", "Goldmere", "Greybank", "Greyfen", "Grimshaw", "Hartwell",
            "Highfield", "Holloway", "Ironbrook", "Kestrel", "Kingsley", "Larkspur", "Lowmere", "Marrow", "Mournhill", "Northmere",
            "Oakridge", "Pineholt", "Quarry", "Rainford", "Ravencrest", "Redfen", "Rook", "Rowntree", "Stonehill", "Storme",
            "Thornwall", "Umber", "Vale", "Westfall", "Whitlock", "Willowmere", "Wolfe", "Yarrow", "Amberfield", "Brightwell",
            "Cinder", "Driftwood", "Eagleford", "Foxhall", "Greenholt", "Hawthorne", "Ivory", "Juniper", "Locke", "Mallow",
            "Norwood", "Orchard", "Pryce", "Roth", "Sable", "Tanner", "Underhill", "Voss", "Wren", "Yewcroft"
    };

    private static final Set<String> PLACEHOLDER_NAMES = Set.of(
            "recruit", "shieldman", "nomad", "horseman", "bowman", "crossbowman", "scout", "messenger", "commander", "captain",
            "noble villager", "farmer", "builder", "miner", "lumberman", "merchant", "fisherman", "animal farmer", "citizen"
    );

    private BannerModNpcNamePool() {
    }

    public static void ensureNamed(Mob mob) {
        if (mob == null) {
            return;
        }
        String current = mob.getCustomName() == null ? "" : mob.getCustomName().getString().trim();
        if (!current.isBlank() && !PLACEHOLDER_NAMES.contains(current.toLowerCase(Locale.ROOT))) {
            return;
        }
        mob.setCustomName(Component.literal(generate(mob)));
    }

    public static String generate(Mob mob) {
        int firstIndex = mob.getRandom().nextInt(GIVEN_NAMES.length);
        int lastIndex = mob.getRandom().nextInt(FAMILY_NAMES.length);
        return GIVEN_NAMES[firstIndex] + " " + FAMILY_NAMES[lastIndex];
    }
}
