package com.proxtextchat.ModMenu;

import com.proxtextchat.ProxChatBaseModClient;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * This class adds compatability with the ModMenu mod.
 */
public class ModMenuCompat implements ModMenuApi {
    // TODO: need to make sure this dependency is optional.
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ProxChatBaseModClient::getConfigScreen;
    }

}
