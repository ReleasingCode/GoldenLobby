package com.releasingcode.goldenlobby.modulos.scoreboard.manager;


import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContentsAnimatable {
    private final List<ContentsAnimatableLine> texts;

    public ContentsAnimatable(FileConfiguration configuration) {
        texts = new ArrayList<>();
        for (String key : configuration.getConfigurationSection("contents-animatable").getKeys(false)) {
            int tick = configuration.getInt("contents-animatable." + key + ".tick", 0);
            String contents = configuration.getString("contents-animatable." + key + ".contents", "");
            texts.add(new ContentsAnimatableLine(key, contents, tick));
        }
    }

    public List<ContentsAnimatableLine> getTexts() {
        return texts;
    }

    public static class ContentsAnimatableLine {
        private final String key;
        private final int tick;
        private final List<String> text;

        public ContentsAnimatableLine(String key, String text, int tick) {
            this.key = key;
            this.tick = tick;
            this.text = new ArrayList<>(Arrays.asList(text.split("\\n")));
        }

        public String getKey() {
            return key;
        }

        public int getTick() {
            return tick;
        }

        public List<String> getText() {
            return text;
        }
    }
}
