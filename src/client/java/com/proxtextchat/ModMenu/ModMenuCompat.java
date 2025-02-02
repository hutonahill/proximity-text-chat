package com.proxtextchat.ModMenu;

import com.proxtextchat.ProxChatBaseMod;
import com.proxtextchat.ProxChatBaseModClient;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ProxChatBaseModClient::getConfigScreen;
    }
}
