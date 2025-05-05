package net.quepierts.entityharvest.data;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.quepierts.entityharvest.EntityHarvest;

public class EntityHarvestAttachments {
    public static final DeferredRegister<AttachmentType<?>> REGISTER = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES, EntityHarvest.MODID
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<HarvestProgressAttachment>> HARVEST_PROGRESS = REGISTER.register(
            "harvest_progress",
            () -> AttachmentType.builder(HarvestProgressAttachment::new)
                    .serialize(HarvestProgressAttachment.CODEC)
                    .build()
    );
}
