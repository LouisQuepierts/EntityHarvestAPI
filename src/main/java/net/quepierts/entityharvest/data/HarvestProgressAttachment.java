package net.quepierts.entityharvest.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;

public class HarvestProgressAttachment {
    public static final Codec<HarvestProgressAttachment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("progress").forGetter(HarvestProgressAttachment::getProgress),
            Codec.BOOL.fieldOf("destroyed").forGetter(HarvestProgressAttachment::isDestroyed)
    ).apply(instance, (progress, destroyed) -> {
        HarvestProgressAttachment attachment = new HarvestProgressAttachment();
        attachment.setProgress(progress);
        attachment.setDestroyed(destroyed);
        return attachment;
    }));

    private float progress;
    private int destroyTick;
    private int tick;
    private boolean destroyed;

    public float getProgress() {
        return progress;
    }

    public int getDestroyType() {
        return Math.clamp((int) (this.progress * 10), 0, 9);
    }

    public void setProgress(float progress) {
        this.progress = Mth.clamp(progress, 0.0f, 1.0f);
        this.destroyTick++;
        this.tick = 0;

        if (progress >= 1.0f) {
            this.destroyed = true;
        }
    }

    public boolean isDestroyed() {
        return this.destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    public void tick() {
        if (this.tick == 20) {
            this.destroyTick = 0;
            this.progress = Math.max(0, this.progress - 0.2f);
            this.tick = 0;
        }

        if (this.progress != 0) {
            this.tick ++;
        }
    }

    public int getDestroyTick() {
        return this.destroyTick;
    }
}
