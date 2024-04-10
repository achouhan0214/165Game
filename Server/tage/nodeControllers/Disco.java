package tage.nodeControllers;
import tage.*;
import org.joml.*;
import java.lang.Math;
/**
* A Disco is a node controller that, when enabled, causes any object
* it is change its Ambient lighting attraibutes to different collors.
* @author Anish Chouhan
*/
public class Disco extends NodeController{
        private Engine engine;

        private float cycleTime = 2000.0f;
        private float totalTime = 0.0f;

        /** Creates a Dico controller with a cycle time*/
        public Disco(Engine e, float cycleTime)
        {
                super();
                this.cycleTime = cycleTime;
                engine = e;
        }
        
         /** This is called automatically by the RenderSystem (via SceneGraph) once per frame
	      *   during display().  It is for engine use and should not be called by the application.
         *   Chanages the ambient light to a random color changing at certain cycles. 
	      */
        public void apply(GameObject go)
        {
                float elapsedTime = super.getElapsedTime();
                totalTime += elapsedTime/1000.0f;
                if (totalTime > cycleTime) {
                        float r = (float) Math.random() * 150;
                        float g = (float) Math.random() * 150;
                        float b = (float) Math.random() * 150;
                        float[] rgb = {r, g, b};
                        ObjShape tempShape = go.getShape();
                        tempShape.setMatAmb(rgb);
                        go.setShape(tempShape);
                        totalTime = 0;
                }
        }
}
