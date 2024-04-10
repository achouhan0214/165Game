package tage.nodeControllers;
import tage.*;
import org.joml.*;

/**
* A StretchController is a node controller that, when enabled, causes any object
* it is attached to to strrtch and compress.
* @author Scott Gordon
*/
public class StretchController extends NodeController{
        private float scaleRate = .0003f;
        private float cycleTime = 2000.0f;
        private float totalTime = 0.0f;
        private float direction = 1.0f;
        private Matrix4f curScale, newScale;
        private Engine engine;
        
        /** Creates a stretch controller with a specified cycle time*/
        public StretchController(Engine e, float ctime)
        {
                super();
                cycleTime = ctime;
                engine = e;
                newScale = new Matrix4f();
        }
        /** This is called automatically by the RenderSystem (via SceneGraph) once per frame
	     *   during display().  It is for engine use and should not be called by the application.
        *   Applies stretch and compress over a certain time cycle.
	     */
        public void apply(GameObject go)
        {
                float elapsedTime = super.getElapsedTime();
                totalTime += elapsedTime/1000.0f;
                if (totalTime > cycleTime)
                {
                        direction = -direction;
                        totalTime = 0.0f;
                }
                curScale = go.getLocalScale();
                float scaleAmt = 1.0f + direction * scaleRate * elapsedTime;
                newScale.scaling(curScale.m00()*scaleAmt, curScale.m11(), curScale.m22());
                go.setLocalScale(newScale);
        }
}
