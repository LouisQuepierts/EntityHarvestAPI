package net.quepierts.entityharvest.ai;

import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.quepierts.entityharvest.data.EntityHarvestAttachments;
import net.quepierts.entityharvest.data.HarvestProgressAttachment;

public class MissingHeadPanicGoal extends PanicGoal {
    public MissingHeadPanicGoal(AbstractSkeleton mob, double speedModifier) {
        super(mob, speedModifier);
    }

    @Override
    protected boolean shouldPanic() {
        if (!this.mob.hasData(EntityHarvestAttachments.HARVEST_PROGRESS)) {
            return false;
        }
        HarvestProgressAttachment attachment = this.mob.getData(EntityHarvestAttachments.HARVEST_PROGRESS);
        return attachment.isDestroyed();
    }
}
