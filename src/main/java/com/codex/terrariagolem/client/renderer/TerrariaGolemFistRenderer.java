package com.codex.terrariagolem.client.renderer;

import com.codex.terrariagolem.TerrariaGolemMod;
import com.codex.terrariagolem.client.model.TerrariaGolemFistModel;
import com.codex.terrariagolem.world.entity.TerrariaGolemEntity;
import com.codex.terrariagolem.world.entity.TerrariaGolemFistEntity;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class TerrariaGolemFistRenderer extends MobRenderer<TerrariaGolemFistEntity, TerrariaGolemFistModel<TerrariaGolemFistEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(TerrariaGolemMod.MODID, "textures/entity/terraria_golem.png");

    public TerrariaGolemFistRenderer(EntityRendererProvider.Context context) {
        super(context, new TerrariaGolemFistModel<>(context.bakeLayer(TerrariaGolemFistModel.LAYER_LOCATION)), 0.85F);
    }

    @Override
    protected void scale(TerrariaGolemFistEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.45F, 1.45F, 1.45F);
    }

    @Override
    public boolean shouldRender(TerrariaGolemFistEntity entity, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
        if (super.shouldRender(entity, frustum, cameraX, cameraY, cameraZ)) {
            return true;
        }

        Entity owner = entity.getOwnerEntity();
        if (!(owner instanceof TerrariaGolemEntity body) || entity.getExtension() <= 0.04F) {
            return false;
        }

        Vec3 fist = entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
        Vec3 anchor = body.getFistChainAnchor(entity.isLeftFist());
        AABB chainBounds = new AABB(
                Math.min(fist.x, anchor.x),
                Math.min(fist.y, anchor.y),
                Math.min(fist.z, anchor.z),
                Math.max(fist.x, anchor.x),
                Math.max(fist.y, anchor.y),
                Math.max(fist.z, anchor.z))
                .inflate(2.0D);
        return frustum.isVisible(chainBounds);
    }

    @Override
    public void render(TerrariaGolemFistEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Entity owner = entity.getOwnerEntity();
        if (owner instanceof TerrariaGolemEntity body) {
            if (entity.getExtension() <= 0.04F) {
                renderIdleWithBodyView(entity, body, partialTick, poseStack, buffer, packedLight);
                return;
            }
            super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
            renderChainToBody(entity, body, partialTick, poseStack, buffer);
            return;
        }
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private void renderIdleWithBodyView(TerrariaGolemFistEntity entity, TerrariaGolemEntity body, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Vec3 entityPos = entity.getPosition(partialTick);
        Vec3 bodyAnchor = body.getFistAnchor(entity.isLeftFist(), partialTick);
        Vec3 offset = bodyAnchor.subtract(entityPos);
        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y, offset.z);
        super.render(entity, body.getPartYaw(partialTick), partialTick, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    private void renderChainToBody(TerrariaGolemFistEntity fist, TerrariaGolemEntity body, float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        Vec3 fistPos = fist.getPosition(partialTick);
        Vec3 anchor = body.getFistChainAnchor(fist.isLeftFist(), partialTick);
        Vec3 start = fistSurfaceTowardBody(fist, anchor.subtract(fistPos));
        Vec3 end = anchor.subtract(fistPos);
        Vec3 delta = end.subtract(start);
        double length = delta.length();
        if (length < 0.25D) {
            return;
        }

        Vec3 forward = delta.scale(1.0D / length);
        Vec3 side = forward.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (side.lengthSqr() < 0.001D) {
            side = forward.cross(new Vec3(1.0D, 0.0D, 0.0D));
        }
        side = side.normalize();
        Vec3 up = side.cross(forward).normalize();

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.lines());
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();

        drawLine(vertexConsumer, matrix, normal, start, end, 37, 39, 37);
        drawLine(vertexConsumer, matrix, normal, start.add(side.scale(0.035D)), end.add(side.scale(0.035D)), 78, 82, 76);
        drawLine(vertexConsumer, matrix, normal, start.add(side.scale(-0.035D)), end.add(side.scale(-0.035D)), 25, 27, 25);

        int links = Mth.clamp((int) (length / 0.42D), 4, 32);
        for (int i = 1; i < links; i++) {
            double t = i / (double) links;
            Vec3 center = start.add(delta.scale(t));
            Vec3 cross = (i & 1) == 0 ? side : up;
            double half = 0.14D;
            drawLine(vertexConsumer, matrix, normal, center.add(cross.scale(-half)), center.add(cross.scale(half)), 126, 132, 118);
        }
    }

    private Vec3 fistSurfaceTowardBody(TerrariaGolemFistEntity fist, Vec3 anchorLocal) {
        Vec3 center = new Vec3(0.0D, fist.getBbHeight() * 0.52D, 0.0D);
        Vec3 direction = anchorLocal.subtract(center);
        if (direction.lengthSqr() < 0.001D) {
            return center;
        }

        direction = direction.normalize();
        double halfX = fist.getBbWidth() * 0.48D;
        double halfY = fist.getBbHeight() * 0.42D;
        double halfZ = fist.getBbWidth() * 0.48D;
        double distance = Double.POSITIVE_INFINITY;
        if (Math.abs(direction.x) > 0.001D) {
            distance = Math.min(distance, halfX / Math.abs(direction.x));
        }
        if (Math.abs(direction.y) > 0.001D) {
            distance = Math.min(distance, halfY / Math.abs(direction.y));
        }
        if (Math.abs(direction.z) > 0.001D) {
            distance = Math.min(distance, halfZ / Math.abs(direction.z));
        }
        if (!Double.isFinite(distance)) {
            distance = halfX;
        }
        return center.add(direction.scale(distance));
    }

    private void drawLine(VertexConsumer vertexConsumer, Matrix4f matrix, Matrix3f normal, Vec3 start, Vec3 end, int red, int green, int blue) {
        Vec3 direction = end.subtract(start).normalize();
        vertexConsumer.vertex(matrix, (float) start.x, (float) start.y, (float) start.z)
                .color(red, green, blue, 255)
                .normal(normal, (float) direction.x, (float) direction.y, (float) direction.z)
                .endVertex();
        vertexConsumer.vertex(matrix, (float) end.x, (float) end.y, (float) end.z)
                .color(red, green, blue, 255)
                .normal(normal, (float) direction.x, (float) direction.y, (float) direction.z)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(TerrariaGolemFistEntity entity) {
        return TEXTURE;
    }
}
