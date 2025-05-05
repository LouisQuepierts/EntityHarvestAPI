package net.quepierts.entityharvest.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.ModelBakery;
import org.jetbrains.annotations.NotNull;

public class DestroyTextureVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private final float width;
    private final float height;

    public static VertexConsumer getVertexConsumer(MultiBufferSource buffer, float width, float height, int destroyType) {
        VertexConsumer consumer = buffer.getBuffer(ModelBakery.DESTROY_TYPES.get(destroyType));
        return new DestroyTextureVertexConsumer(consumer, width, height);
    }

    public DestroyTextureVertexConsumer(VertexConsumer delegate, float width, float height) {
        this.delegate = delegate;
        this.width = width;
        this.height = height;
    }

    @Override
    public @NotNull VertexConsumer addVertex(float x, float y, float z) {
        this.delegate.addVertex(x, y, z);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setColor(int red, int green, int blue, int alpha) {
        this.delegate.setColor(-1);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setUv(float u, float v) {
        this.delegate.setUv(u * this.width, v * this.height);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setUv1(int u, int v) {
        this.delegate.setUv1(u, v);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setUv2(int u, int v) {
        this.delegate.setUv2(u, v);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
        this.delegate.setNormal(normalX, normalY, normalZ);
        return this;
    }
}
