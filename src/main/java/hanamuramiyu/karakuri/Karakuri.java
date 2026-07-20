package hanamuramiyu.karakuri;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Karakuri implements ModInitializer {
    public static final String MOD_ID = "karakuri";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Karakuri initialized");
    }
}