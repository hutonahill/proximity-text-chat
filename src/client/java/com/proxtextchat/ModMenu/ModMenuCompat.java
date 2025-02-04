package com.proxtextchat.ModMenu;

import com.proxtextchat.ProxChatBaseMod;
import com.proxtextchat.ProxChatBaseModClient;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;

public class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ClothConfigCompat().getConfigScreen(parent);
        //return ProxChatBaseModClient::getConfigScreen;
    }

}
