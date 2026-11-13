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
            // Five trailing volumes twist behind the real beveled blade.
            for(int trail=5;trail>=1;trail--) {
                double twist=tilt-trail*0.035;
                Vec3 tu=right.scale(Math.cos(twist)).add(up.scale(Math.sin(twist)));
                Vec3 tv=up.scale(Math.cos(twist)).subtract(right.scale(Math.sin(twist)));
                Vec3 origin=center.subtract(forward.scale(trail*0.22*a.scale()))
                    .subtract(v.scale(trail*0.06*a.scale()));
                volume(vertices,matrix,origin,tu,tv,forward,3F*a.scale(),
                    0.30F*a.scale(),0.11F*a.scale(),fade*0.13F/trail,1F,0.58F,0.04F,48,4);
            }
            // Broad gold aura, solid gold body, and a raised ivory cutting ridge.
            volume(vertices,matrix,center,u,v,forward,3F*a.scale(),
                0.52F*a.scale(),0.24F*a.scale(),fade*0.14F,1F,0.53F,0.01F,96,8);
            volume(vertices,matrix,center,u,v,forward,3F*a.scale(),
                0.25F*a.scale(),0.10F*a.scale(),fade*0.90F,1F,0.80F,0.20F,96,8);
            Vec3 ridge=center.add(v.scale(0.13*a.scale())).add(forward.scale(0.065*a.scale()));
            volume(vertices,matrix,ridge,u,v,forward,3F*a.scale(),
                0.065F*a.scale(),0.035F*a.scale(),fade,1F,0.99F,0.87F,96,8);
            // A smaller, offset crescent follows the main blade in three dimensions.
            volume(vertices,matrix,center.subtract(v.scale(0.8*a.scale())).subtract(forward.scale(0.3*a.scale())),
                u,v,forward,2.3F*a.scale(),0.12F*a.scale(),0.06F*a.scale(),
                fade*0.6F,1F,0.85F,0.35F,64,8);
            for(int spark=0;spark<12;spark++) {
                double t=(spark/12.0+age*0.018)%1.0;
                double x=(2*t-1)*3*a.scale();
                double y=1.26*a.scale()*(1-Math.pow(2*t-1,2));
                double depth=Math.sin(age*0.35+spark*2.4)*0.45*a.scale();
                Vec3 point=center.add(u.scale(x)).add(v.scale(y+0.3*Math.sin(spark)))
                    .add(forward.scale(depth));
                crystal(vertices,matrix,point,u,v,forward,0.035F*a.scale(),fade*0.85F);
            }
        }
        buffers.endBatch(RenderType.lightning());
    }
    /** An elliptical eight-facet section swept along a curved, tapering blade.
     *  Unlike a billboard ribbon, it has front, back and bevel faces from every view. */
    private static void volume(VertexConsumer out,Matrix4f m,Vec3 c,Vec3 u,Vec3 v,Vec3 normal,
                               float length,float width,float depth,float alpha,float r,float g,float b,
                               int segments,int sides) {
        for(int i=0;i<segments;i++){
            double t=i/(double)segments,t2=(i+1)/(double)segments;
            Vec3 a=arc(c,u,v,length,t),a2=arc(c,u,v,length,t2);
            double taper=Math.pow(Math.max(0.0001,Math.sin(t*Math.PI)),0.8);
            double taper2=Math.pow(Math.max(0.0001,Math.sin(t2*Math.PI)),0.8);
            for(int j=0;j<sides;j++){
                double angle=j*Math.PI*2/sides,angle2=(j+1)*Math.PI*2/sides;
                Vec3 p=section(a,v,normal,width,depth,taper,angle);
                Vec3 q=section(a2,v,normal,width,depth,taper2,angle);
                Vec3 rr=section(a2,v,normal,width,depth,taper2,angle2);
                Vec3 ss=section(a,v,normal,width,depth,taper,angle2);
                float shade=(float)(0.72+0.28*Math.max(0,Math.sin(angle+0.5)));
                vertex(out,m,p,r*shade,g*shade,b*shade,alpha);
                vertex(out,m,q,r*shade,g*shade,b*shade,alpha);
                vertex(out,m,rr,r*shade,g*shade,b*shade,alpha);
                vertex(out,m,ss,r*shade,g*shade,b*shade,alpha);
            }
        }
    }
    private static Vec3 arc(Vec3 c,Vec3 u,Vec3 v,float length,double t){
        double x=(2*t-1)*length;
        double y=0.42*length*(1-Math.pow(2*t-1,2));
        return c.add(u.scale(x)).add(v.scale(y));
    }
    private static Vec3 section(Vec3 c,Vec3 v,Vec3 normal,float width,float depth,double taper,double angle){
        return c.add(v.scale(width*taper*Math.cos(angle)))
            .add(normal.scale(depth*taper*Math.sin(angle)));
    }
    private static void crystal(VertexConsumer out,Matrix4f m,Vec3 c,Vec3 u,Vec3 v,Vec3 n,float size,float alpha) {
        Vec3 top=c.add(v.scale(size*2.5)),bottom=c.subtract(v.scale(size*2.5));
        Vec3[] ring={c.add(u.scale(size)),c.add(n.scale(size)),c.subtract(u.scale(size)),c.subtract(n.scale(size))};
        for(int i=0;i<4;i++){
            Vec3 a=ring[i],b=ring[(i+1)%4];
            vertex(out,m,top,1,1,0.85F,alpha);vertex(out,m,a,1,0.85F,0.3F,alpha);
            vertex(out,m,bottom,1,0.65F,0.08F,alpha);vertex(out,m,b,1,0.85F,0.3F,alpha);
        }
    }
    private static void vertex(VertexConsumer out,Matrix4f matrix,Vec3 p,float r,float g,float b,float a){
        out.addVertex(matrix,(float)p.x,(float)p.y,(float)p.z).setColor(r,g,b,a);
    }
}
