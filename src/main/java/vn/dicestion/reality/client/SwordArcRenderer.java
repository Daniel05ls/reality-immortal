package vn.dicestion.reality.client;
import java.util.*;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import vn.dicestion.reality.*;
import org.joml.Matrix4f;

@EventBusSubscriber(modid=RealityImmortal.ID,value=Dist.CLIENT)
public final class SwordArcRenderer {
    private record Wave(SwordArc arc,long start,Object world) {}
    private static final List<Wave> WAVES=new ArrayList<>();
    public static void add(SwordArc arc) {
        var world=Minecraft.getInstance().level;
        if(world==null) return;
        if(WAVES.size()>=32) WAVES.remove(0);
        WAVES.add(new Wave(arc,world.getGameTime(),world));
    }
    @SubscribeEvent
    public static void draw(RenderLevelStageEvent event) {
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        var mc=Minecraft.getInstance();
        if(mc.level==null){WAVES.clear();return;}
        double now=mc.level.getGameTime()+event.getPartialTick().getGameTimeDeltaPartialTick(false);
        WAVES.removeIf(w->w.world!=mc.level || now-w.start>w.arc.life());
        if(WAVES.isEmpty()) return;
        var buffers=mc.renderBuffers().bufferSource();
        VertexConsumer vertices=buffers.getBuffer(RenderType.lightning());
        Vec3 camera=event.getCamera().getPosition();
        Matrix4f matrix=event.getModelViewMatrix();
        for(Wave w:WAVES) {
            SwordArc a=w.arc;float age=(float)(now-w.start);float progress=Math.min(1,age/a.life());
            Vec3 forward=new Vec3(a.dx(),a.dy(),a.dz()).normalize();
            Vec3 right=forward.cross(new Vec3(0,1,0));
            if(right.lengthSqr()<0.001) right=new Vec3(1,0,0);
            right=right.normalize();Vec3 up=right.cross(forward).normalize();
            double tilt=a.speed()==0 ? -0.9+1.7*progress : -0.55;
            Vec3 u=right.scale(Math.cos(tilt)).add(up.scale(Math.sin(tilt)));
            Vec3 v=up.scale(Math.cos(tilt)).subtract(right.scale(Math.sin(tilt)));
            Vec3 center=new Vec3(a.x(),a.y(),a.z()).add(forward.scale(a.speed()*age)).subtract(camera);
            float fade=(float)Math.sin(Math.PI*Math.min(0.99,Math.max(0.01,progress)));
            for(int trail=2;trail>=0;trail--) {
                Vec3 origin=center.subtract(forward.scale(trail*0.28*a.scale())).subtract(v.scale(trail*0.10*a.scale()));
                float alpha=fade*(trail==0?1F:0.15F/trail);
                ribbon(vertices,matrix,origin,u,v,3F*a.scale(),0.5F*a.scale(),alpha*0.12F,1F,0.52F,0.015F);
                ribbon(vertices,matrix,origin,u,v,3F*a.scale(),0.23F*a.scale(),alpha*0.7F,1F,0.78F,0.12F);
                ribbon(vertices,matrix,origin,u,v,3F*a.scale(),0.055F*a.scale(),alpha,1F,0.99F,0.83F);
            }
        }
        buffers.endBatch(RenderType.lightning());
    }
    private static void ribbon(VertexConsumer out,Matrix4f m,Vec3 c,Vec3 u,Vec3 v,
                               float length,float width,float alpha,float r,float g,float b) {
        for(int i=0;i<48;i++){
            double t=i/48.0,t2=(i+1)/48.0;
            double x=(2*t-1)*length,x2=(2*t2-1)*length;
            double curve=0.42*length*(1-Math.pow(2*t-1,2));
            double curve2=0.42*length*(1-Math.pow(2*t2-1,2));
            double w=width*Math.pow(Math.sin(t*Math.PI),0.7),w2=width*Math.pow(Math.sin(t2*Math.PI),0.7);
            vertex(out,m,c.add(u.scale(x)).add(v.scale(curve+w)),r,g,b,alpha);
            vertex(out,m,c.add(u.scale(x2)).add(v.scale(curve2+w2)),r,g,b,alpha);
            vertex(out,m,c.add(u.scale(x2)).add(v.scale(curve2-w2)),r,g,b,alpha);
            vertex(out,m,c.add(u.scale(x)).add(v.scale(curve-w)),r,g,b,alpha);
        }
    }
    private static void vertex(VertexConsumer out,Matrix4f matrix,Vec3 p,float r,float g,float b,float a){
        out.addVertex(matrix,(float)p.x,(float)p.y,(float)p.z).setColor(r,g,b,a);
    }
}
