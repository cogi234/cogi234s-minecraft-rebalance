package cogi234.rebalance.util;

import blue.endless.jankson.Jankson;
import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.util.Observable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Cogi234sRebalanceConfig extends ConfigWrapper<cogi234.rebalance.util.ConfigModel> {

    public final Keys keys = new Keys();

    private final Option<java.lang.Boolean> repairsAccumulateCost = this.optionForKey(this.keys.repairsAccumulateCost);
    private final Option<java.lang.Integer> materialCountToFullyRepair = this.optionForKey(this.keys.materialCountToFullyRepair);
    private final Option<java.lang.Boolean> enchantsCanLevelUp = this.optionForKey(this.keys.enchantsCanLevelUp);
    private final Option<java.lang.Boolean> sleepCanSkipNight = this.optionForKey(this.keys.sleepCanSkipNight);
    private final Option<java.lang.Boolean> bedCanSetSpawn = this.optionForKey(this.keys.bedCanSetSpawn);
    private final Option<java.lang.Integer> minecartMaxSpeed = this.optionForKey(this.keys.minecartMaxSpeed);

    private Cogi234sRebalanceConfig() {
        super(cogi234.rebalance.util.ConfigModel.class);
    }

    private Cogi234sRebalanceConfig(Consumer<Jankson.Builder> janksonBuilder) {
        super(cogi234.rebalance.util.ConfigModel.class, janksonBuilder);
    }

    public static Cogi234sRebalanceConfig createAndLoad() {
        var wrapper = new Cogi234sRebalanceConfig();
        wrapper.load();
        return wrapper;
    }

    public static Cogi234sRebalanceConfig createAndLoad(Consumer<Jankson.Builder> janksonBuilder) {
        var wrapper = new Cogi234sRebalanceConfig(janksonBuilder);
        wrapper.load();
        return wrapper;
    }

    public boolean repairsAccumulateCost() {
        return repairsAccumulateCost.value();
    }

    public void repairsAccumulateCost(boolean value) {
        repairsAccumulateCost.set(value);
    }

    public int materialCountToFullyRepair() {
        return materialCountToFullyRepair.value();
    }

    public void materialCountToFullyRepair(int value) {
        materialCountToFullyRepair.set(value);
    }

    public boolean enchantsCanLevelUp() {
        return enchantsCanLevelUp.value();
    }

    public void enchantsCanLevelUp(boolean value) {
        enchantsCanLevelUp.set(value);
    }

    public boolean sleepCanSkipNight() {
        return sleepCanSkipNight.value();
    }

    public void sleepCanSkipNight(boolean value) {
        sleepCanSkipNight.set(value);
    }

    public boolean bedCanSetSpawn() {
        return bedCanSetSpawn.value();
    }

    public void bedCanSetSpawn(boolean value) {
        bedCanSetSpawn.set(value);
    }

    public int minecartMaxSpeed() {
        return minecartMaxSpeed.value();
    }

    public void minecartMaxSpeed(int value) {
        minecartMaxSpeed.set(value);
    }


    public static class Keys {
        public final Option.Key repairsAccumulateCost = new Option.Key("repairsAccumulateCost");
        public final Option.Key materialCountToFullyRepair = new Option.Key("materialCountToFullyRepair");
        public final Option.Key enchantsCanLevelUp = new Option.Key("enchantsCanLevelUp");
        public final Option.Key sleepCanSkipNight = new Option.Key("sleepCanSkipNight");
        public final Option.Key bedCanSetSpawn = new Option.Key("bedCanSetSpawn");
        public final Option.Key minecartMaxSpeed = new Option.Key("minecartMaxSpeed");
    }
}

