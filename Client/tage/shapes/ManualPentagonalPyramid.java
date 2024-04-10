package tage.shapes;
import tage.*;
import tage.shapes.*;
import tage.shapes.ManualObject;

/**
 * A PentagonalPyramid has 24 vertices, has the base as a pentagon, and 5 triangle that meet at the top
 * @author Anish Chouhan
 */
public class ManualPentagonalPyramid extends ManualObject {
        private float[] vertices = new float[]
                {
                        -0.6f,-1f,1f,  0.6f,-1f,1f,  0f,1f,0f, //side1
                        0.6f,-1f,1f,  1f,-1f,-0.1f,  0f,1f,0f, //sid2
                        1f,-1f,-0.1f,  0f,-1f,-1f,  0f,1f,0f, //side3
                        0f,-1f,-1f,  -1f,-1f,-0.1f,  0f,1f,0f, //side4
                        -1f,-1f,-0.1f,  -0.6f,-1f,1f,  0f,1f,0f,  //side5
                        0f,-1f,-1f, -1f,-1f,-0.1f, -0.6f,-1f,1f, //bottom1
                        0f,-1f,-1f, -0.6f,-1f,1f, 0.6f,-1f,1f, //botton2
                        0f,-1f,-1f, 0.6f,-1f,1f, 1f,-1f,-0.1f, //bottom3
                };
        private float[] texcoords = new float[]
                {
                        0f,0f, 1f,0f, 0.5f,1f,
                        0f,0f, 1f,0f, 0.5f,1f,
                        0f,0f, 1f,0f, 0.5f,1f,
                        0f,0f, 1f,0f, 0.5f,1f,
                        0f,0f, 1f,0f, 0.5f,1f,
                        0f,0f, 1f,0f, 0.5f,1f,
                        0f,0f, 1f,0f, 0.5f,1f,
                        0f,0f, 1f,0f, 0.5f,1f,
                };

        private float[] normals = new float[]
                {
                        0f,1f,1f, 0f,1f,1f, 0f,1f,1f,
                        0.6f,1f,0.5f, 0.6f,1f,0.5f, 0.6f,1f,0.5f,
                        0.5f,1f,-0.5f, 0.5f,1f,-0.5f, 0.5f,1f,-0.5f,
                        -0.5f,1f,-0.5f, -0.5f,1f,-0.5f, -0.5f,1f,-0.5f,
                        -0.6f,1f,0.5f, -0.6f,1f,0.5f, -0.6f,1f,0.5f,
                        0f,-1f,0f, 0f,-1f,0f, 0f,-1f,0f,
                        0f,-1f,0f, 0f,-1f,0f, 0f,-1f,0f,
                        0f,-1f,0f, 0f,-1f,0f, 0f,-1f,0f,
                };

        /** Creates a 24-vertex pentagonal pyramid with texture coordinates if (0,0) (1, 0), (0.5, 1) . */
        public ManualPentagonalPyramid()
        {
                super();
                setNumVertices(24);
                setVertices(vertices);
                setTexCoords(texcoords);
                setNormals(normals);

                setMatAmb(Utils.goldAmbient());
                setMatDif(Utils.goldDiffuse());
                setMatSpe(Utils.goldSpecular());
                setMatShi(Utils.goldShininess());


        }
}
