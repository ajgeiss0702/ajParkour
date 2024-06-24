package us.ajg0702.parkour.loaders;

import us.ajg0702.parkour.ParkourPlugin;
import us.ajg0702.utils.common.Messages;

import java.util.HashMap;

public class MessageLoader {

    public static Messages loadMessages(ParkourPlugin plugin) {
        return new Messages(plugin.getDataFolder(), plugin.getLogger(), Messages.makeDefaults(
                "setup.create.usage", "&cUsage: &f/{LABEL} setup create <name>",
                "setup.create.success", "&aSuccessfully created in-progress area {NAME}. &7Do &f/{LABEL} setup {NAME}&7 for next steps.",
                "setup.create.fail.already-exists", "&cAn area called &f{NAME}&c already exists!",
                "setup.area-does-not-exist", "&cThere is no area called &f{NAME}&c.",
                "setup.progress.header", "&6Parkour area &e{NAME}&6 setup progress\n&eYellow = Unset (required) &7Gray = Unset (optional) &aGreen = Set",
                "setup.progress.pos1.unset", "&epos1&7 - /ajp setup {NAME} pos1",
                "setup.progress.pos1.set", "&apos1",
                "setup.progress.pos2.unset", "&epos2&7 - /ajp setup {NAME} pos2",
                "setup.progress.pos2.set", "&apos2",
                "setup.progress.difficulty.unset", "&7difficulty&7 - /ajp setup {NAME} difficulty <difficulty>",
                "setup.progress.difficulty.set", "&adifficulty&7 - {DIFFICULTY}",
                "setup.progress.fallpos.unset", "&7fallpos - /ajp setup {NAME} fallpos",
                "setup.progress.fallpos.set", "&afallpos",
                "setup.difficulty.usage", "&cUsage: &f/{LABEL} setup {AREA} difficulty <difficulty>",
                "setup.difficulty.invalid-difficulty", "&cUnable to find a difficulty called \"&f{DIFFICULTY}&c\".",
                "setup.set.pos1", "&aSet the first position for &f{NAME}&a at your feet!",
                "setup.set.pos2", "&aSet the second position for &f{NAME}&a at your feet!",
                "setup.set.difficulty", "&aSet the difficulty for &f{NAME}&a to &f{DIFFICULTY}&a!",
                "setup.set.fallpos", "&aSet the fall position for &f{NAME}&a to where you are standing!",
                "setup.save.missing-required", "&f{NAME}&c is missing some required settings! &7See &f/{LABEL} setup {NAME} &7to see what is missing.",
                "setup.save.success", "&aSuccessfully saved and enabled &f{NAME}&a!",
                "setup.save.error.unknown", "&cSomething went wrong when saving &f{NAME}&c. &7There might be more info in the console.",
                "ingame", "&cYou must be in-game to run this command!"
        ));
    }

}
