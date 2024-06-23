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
                "setup.area-does-not-exist", "&cThere is no area called &f{NAME}&c."
        ));
    }

}
