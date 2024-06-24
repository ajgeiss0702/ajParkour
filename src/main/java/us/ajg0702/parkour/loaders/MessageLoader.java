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
                "ingame", "&cYou must be in-game to run this command!"
        ));
    }

}
