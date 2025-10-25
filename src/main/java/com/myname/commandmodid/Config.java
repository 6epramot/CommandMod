package com.myname.commandmodid;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

// я хз нах этот класс мне нужен но пусть будет
public class Config {

    public static String greeting = "Hello World";

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        greeting = configuration.getString("greeting", Configuration.CATEGORY_GENERAL, greeting, "How shall I greet?");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
