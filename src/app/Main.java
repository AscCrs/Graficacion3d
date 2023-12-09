package app;

/**
 *
 * @author crist
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.GLProfile;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_SMOOTH;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

import java.awt.image.BufferedImage;

@SuppressWarnings("serial")
public class Main extends GLJPanel implements GLEventListener, KeyListener {
    // Define constantes para el contenedor principal

    private static final String TITLE = "Árbol de navidad"; // Título de la ventana
    private static final int CANVAS_WIDTH = 1500; // Ancho del área de dibujo
    private static final int CANVAS_HEIGHT = 844; // Alto del área de dibujo
    private static final int FPS = 60; // Frecuencia de actualización de la animación (cuadros por segundo)
    private static final float factInc = 5.0f; // Factor de incremento de la animación

    // Variables de la cámara
    float fovy = 45.0f; // Campo de visión
    int eje = 0; // Eje de rotación de la cámara (0: X, 1: Y, 2: Z)
    float rotX = 0.0f; // Rotación de la cámara sobre el eje X
    float rotY = 0.0f; // Rotación de la cámara sobre el eje Y
    float rotZ = 0.0f; // Rotación de la cámara sobre el eje Z

    float posCamX = 0.0f; // Posición X de la cámara
    float posCamY = 0.0f; // Posición Y de la cámara
    float posCamZ = 0.0f; // Posición Z de la cámara

    float distanciaCam = 25.0f; // Distancia de la cámara al objeto

    // Colores de fondo
    float rojo = 0.1f;
    float verde = 0.2f;
    float azul = 0.4f;

    // Posición de la luz
    float lightX = 1f; // Posición X de la luz
    float lightY = 1f; // Posición Y de la luz
    float lightZ = 1f; // Posición Z de la luz
    float dLight = 0.05f; // Factor de atenuación de la luz

    // Parámetros de la luz
    final float ambient[] = {0.1f, 0.1f, 0.1f, 0.1f}; // Luz ambiente
    final float position[] = {lightX, lightY, lightZ, 1.0f}; // Posición de la luz
    final float[] colorWhite = {1.0f, 1.0f, 1.0f, 1.0f}; // Color de la luz

    float intensidadLuz = 0.0f; // Intensidad de la luz

    // Texturas
    Texture textura1;
    Texture textura2;
    Texture textura3;
    Texture textura4;
    Texture textura5;

    // Bibliotecas utilizadas
    private GLU glu; // Biblioteca para utilidades de OpenGL
    private GLUT glut; // Biblioteca para la interfaz de usuario de OpenGL

    public static void main(String[] args) {
        // Run the GUI codes in the event-dispatching thread for thread safety
        SwingUtilities.invokeLater(() -> {
            // Create the OpenGL rendering canvas
            GLJPanel canvas = new Main();
            canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));

            // Create a animator that drives canvas' display() at the specified FPS.
            final FPSAnimator animator = new FPSAnimator(canvas, FPS, true);

            // Create the top-level container
            final JFrame frame = new JFrame(); // Swing's JFrame or AWT's Frame

            BorderLayout fl = new BorderLayout();
            frame.setLayout(fl);

            frame.getContentPane().add(canvas, BorderLayout.CENTER);

            frame.addKeyListener((KeyListener) canvas);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    // Use a dedicate thread to run the stop() to ensure that the
                    // animator stops before program exits.
                    new Thread() {
                        @Override
                        public void run() {
                            if (animator.isStarted()) {
                                animator.stop();
                            }
                            System.exit(0);
                        }
                    }.start();
                }
            });

            frame.setTitle(TITLE);
            frame.pack();
            frame.setVisible(true);
            animator.start(); // start the animation loop
        });
    }

    Texture cargarTextura(String imageFile) {
        Texture text1 = null;
        try {
            BufferedImage buffImage = ImageIO.read(new File(imageFile));
            text1 = AWTTextureIO.newTexture(GLProfile.getDefault(), buffImage, false);
        } catch (IOException ioe) {
        }
        return text1;
    }

    /**
     * Constructor to setup the GUI for this Component
     */
    public Main() {
        this.addGLEventListener(this);
        this.addKeyListener(this);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        glu = new GLU();
        glut = new GLUT();
        gl.glClearColor(rojo, verde, azul, 1.0f);
        gl.glClearDepth(1.0f);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LEQUAL);
        gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

        float[] whiteMaterial = {1.0f, 1.0f, 1.0f};
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, whiteMaterial, 0);
        gl.glShadeModel(GL_SMOOTH);

        // Configuración de la luz
        float[] ambientLight = {intensidadLuz, intensidadLuz, intensidadLuz, 0.0f};
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, ambientLight, 0);
        float[] diffuseLight = {intensidadLuz, intensidadLuz, intensidadLuz, 0f};
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, diffuseLight, 0);
        float[] specularLight = {1f, 1f, 1f, 0f};
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, specularLight, 0);

        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);

        // Lamparas
        float[] emissionLight = {1.0f, 1.0f, 1.0f, 1.0f};
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_EMISSION, emissionLight, 0);
        float[] emissionLightColor = {1.0f, 1.0f, 1.0f, 1.0f};
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_EMISSION, emissionLightColor, 0);

        gl.glShadeModel(GL_SMOOTH); // blends colors nicely, and smoothes out lighting
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, this.ambient, 0);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, colorWhite, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, colorWhite, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, position, 0);

        this.initPosition(gl);

        this.textura1 = this.cargarTextura("textures/nieve.jpg");
        this.textura2 = this.cargarTextura("textures/tronco.jpg");
        this.textura3 = this.cargarTextura("textures/hojas.jpg");
        this.textura4 = this.cargarTextura("textures/papel.jpg");
        this.textura5 = this.cargarTextura("textures/tierra.jpg");

        gl.glEnable(GL2.GL_TEXTURE_2D);
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_ONE, GL2.GL_ONE_MINUS_SRC_ALPHA);
    }

    // Move light
    public void moveLightX(boolean positivDirection) {
        lightX += positivDirection ? dLight : -dLight;
    }

    public void moveLightY(boolean positivDirection) {
        lightY += positivDirection ? dLight : -dLight;
    }

    public void moveLightZ(boolean positivDirection) {
        lightZ += positivDirection ? dLight : -dLight;
    }

    public void initPosition(GL2 gl) {
        float posLight1[] = {lightX, lightY, lightZ, 1.0f}; // La posicion de la luz se va movimendo aqui chido
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, posLight1, 0);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2(); // get the OpenGL 2 graphics context

        if (height == 0) {
            height = 1; // prevent divide by zero
        }
        float aspect = (float) width / height;

        // Set the view port (display area) to cover the entire window
        gl.glViewport(0, 0, width, height);

        // Setup perspective projection, with aspect ratio matches viewport
        gl.glMatrixMode(GL_PROJECTION); // choose projection matrix
        gl.glLoadIdentity(); // reset projection matrix
        glu.gluPerspective(fovy, aspect, 0.1, 50.0); // fovy, aspect, zNear, zFar

        // Enable the model-view transform
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity(); // reset
    }

    public void animate(GL2 gl) {
        float posLight0[] = {lightX, lightY, lightZ, 1.f};
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, posLight0, 0);
    }

    @Override
    public void display(GLAutoDrawable drawable) {

        GL2 gl = drawable.getGL().getGL2(); // get the OpenGL 2 graphics context
        // get the OpenGL 2 graphics context
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        // gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos, 0);
        // clear color and depth buffers

        gl.glLoadIdentity(); // reset the model-view matrix
        gl.glEnable(GL.GL_LINE_SMOOTH);
        gl.glEnable(GL.GL_BLEND);

        gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_DONT_CARE);

        glu.gluLookAt(0.0, 0.0, distanciaCam, this.posCamX, this.posCamY, this.posCamZ, 0.0, 1.0, 0.0);

        if (rotX < 0) {
            rotX = 360 - factInc;
        }
        if (rotY < 0) {
            rotY = 360 - factInc;
        }
        if (rotZ < 0) {
            rotZ = 360 - factInc;
        }

        if (rotX >= 360) {
            rotX = 0;
        }
        if (rotY >= 360) {
            rotY = 0;
        }
        if (rotZ >= 360) {
            rotZ = 0;
        }

        gl.glRotatef(rotX, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(rotY, 0.0f, 1.0f, 0.0f);
        gl.glRotatef(rotZ, 0.0f, 0.0f, 1.0f);

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        float no_mat[] = {0.0f, 0.0f, 0.0f, 1.0f};
        float mat_ambient[] = {1.0f, 1.0f, 1.0f, 1.0f};
        float mat_ambient_color[] = {1.0f, 1.0f, 1.0f, 1.0f};
        float mat_diffuse[] = {1.0f, 1.0f, 1.0f, 1.0f};
        float mat_specular[] = {1.0f, 1.0f, 1.0f, 1.0f};
        float no_shininess[] = {0.0f};
        float low_shininess[] = {5.0f};
        float high_shininess[] = {100.0f};
        float mat_emission[] = {0.5f, 0.5f, 0.5f, 0.0f};

        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT, no_mat, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE, mat_diffuse, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, no_mat, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SHININESS, high_shininess, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_EMISSION, mat_emission, 0);

        // Asociar la textura con el canvas
        this.textura1.bind(gl);
        this.textura1.enable(gl);

        // glut.glutSolidTeapot(1);
        this.drawFloor(gl);
        // this.drawCubeUVWmapped(gl);

        this.textura1.disable(gl);

        // Asociar la textura con el canvas
        this.textura5.bind(gl);
        this.textura5.enable(gl);

        this.drawDirt(gl);

        this.textura5.disable(gl);

        // Configurar material y luz para el objeto que emite luz
        float[] glowMaterial = {1.0f, 1.0f, 1.0f, 1.0f}; // Ajusta emissionR, emissionG, emissionB según tu necesidad
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_EMISSION, glowMaterial, 0);
        this.textura4.bind(gl);
        this.textura4.enable(gl);
        this.animate(gl);
        this.textura4.disable(gl);

        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT, no_mat, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE, mat_diffuse, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, no_mat, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SHININESS, high_shininess, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_EMISSION, mat_emission, 0);

        // Después de restablecer la emisión a su estado original
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT, mat_ambient, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE, mat_diffuse, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, mat_specular, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SHININESS, high_shininess, 0);

        this.textura2.bind(gl);
        this.textura2.enable(gl);
        this.drawLog(gl);
        this.textura2.disable(gl);

        gl.glPushMatrix();
        gl.glTranslatef(0.0f, 5.5f, 0.0f);
        gl.glScalef(2.5f, 3.5f, 2.5f);
        this.textura3.bind(gl);
        this.textura3.enable(gl);
        this.drawPyramid(gl);
        this.textura3.disable(gl);
        gl.glPopMatrix();

        gl.glTranslatef(0.0f, -1.0f, 0.0f);
        gl.glScalef(0.8f, 0.8f, 0.8f);
        gl.glPushMatrix();
        gl.glTranslatef(-1.5f, 0.0f, 1.0f);
        this.textura4.bind(gl);
        this.textura4.enable(gl);
        this.drawRegalos(gl);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(-9f, 0.0f, -5f);
        this.drawRegalos(gl);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(-3.34f, 0.0f, 8.89f);
        this.drawRegalos(gl);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(13.12f, 0.0f, 6.45f);
        this.drawRegalos(gl);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(-6.67f, 0.0f, -2.1f);
        this.drawRegalos(gl);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(8f, 0.0f, -8.56f);
        this.drawRegalos(gl);
        gl.glPopMatrix();

        this.textura4.disable(gl);

        gl.glFlush();

        this.rotY += 0.5f;
    }

    void drawFloor(GL2 gl) {
        gl.glPushMatrix();
        gl.glScalef(15.0f, 0.3f, 15.0f);
        gl.glTranslatef(0f, -7.0f, 0f);

        gl.glBegin(GL2.GL_QUADS);
        // Front Face

        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f); // Bottom Left
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f); // Top Left
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f); // Top Right
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f); // Bottom Right

        // Back Face
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f); // Bottom Right
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f); // Top Right
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f); // Top Left
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f); // Bottom Left

        // Top Face
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f); // Top Left
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f); // Bottom Left
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f); // Bottom Right
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f); // Top Right

        // Bottom Face
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f); // Top Right
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f); // Top Left
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f); // Bottom Left
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f); // Bottom Right
        // Right face
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f); // Bottom Right
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f); // Top Right
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f); // Top Left
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f); // Bottom Left
        // Left Face
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f); // Bottom Left
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f); // Bottom Right
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f); // Top Right
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f); // Top Left

        gl.glEnd();

        gl.glPopMatrix();

    }

    void drawDirt(GL2 gl) {
        gl.glPushMatrix();
        gl.glScalef(15.0f, 0.8f, 15.0f);
        gl.glTranslatef(0f, -4.0f, 0f);

        // Configura el modo de repetición de la textura
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);

        gl.glBegin(GL2.GL_QUADS);

        // Front Face
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f); // Bottom Left
        gl.glTexCoord2f(0.0f, 2.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f); // Top Left
        gl.glTexCoord2f(2.0f, 2.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f); // Top Right
        gl.glTexCoord2f(2.0f, 0.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f); // Bottom Right

        // Back Face
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f); // Bottom Right
        gl.glTexCoord2f(0.0f, 2.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f); // Top Right
        gl.glTexCoord2f(2.0f, 2.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f); // Top Left
        gl.glTexCoord2f(2.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f); // Bottom Left

        // Top Face
        gl.glTexCoord2f(0.0f, 2.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f); // Top Left
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f); // Bottom Left
        gl.glTexCoord2f(2.0f, 0.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f); // Bottom Right
        gl.glTexCoord2f(2.0f, 2.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f); // Top Right

        // Bottom Face
        gl.glTexCoord2f(2.0f, 2.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f); // Top Right
        gl.glTexCoord2f(0.0f, 2.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f); // Top Left
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f); // Bottom Left
        gl.glTexCoord2f(2.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f); // Bottom Right

        // Right Face
        gl.glTexCoord2f(2.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f); // Bottom Right
        gl.glTexCoord2f(2.0f, 2.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f); // Top Right
        gl.glTexCoord2f(0.0f, 2.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f); // Top Left
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f); // Bottom Left

        // Left Face
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f); // Bottom Left
        gl.glTexCoord2f(2.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f); // Bottom Right
        gl.glTexCoord2f(2.0f, 2.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f); // Top Right
        gl.glTexCoord2f(0.0f, 2.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f); // Top Left

        gl.glEnd();

        gl.glPopMatrix();

    }

    public static void drawLog(GL2 gl) {

        gl.glPushMatrix();

        gl.glScalef(0.6f, 2.5f, 0.6f);
        gl.glTranslatef(0.0f, 0.3f, 0.0f);
        gl.glBegin(GL2.GL_QUADS);
        // Front Face

        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f); // Bottom Left
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f); // Top Left
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f); // Top Right
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f); // Bottom Right

        // Back Face
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f); // Bottom Right
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f); // Top Right
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f); // Top Left
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f); // Bottom Left

        // Top Face
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f); // Top Left
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f); // Bottom Left
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f); // Bottom Right
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f); // Top Right

        // Bottom Face
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f); // Top Right
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f); // Top Left
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f); // Bottom Left
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f); // Bottom Right
        // Right face
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f); // Bottom Right
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f); // Top Right
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f); // Top Left
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f); // Bottom Left
        // Left Face
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f); // Bottom Left
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f); // Bottom Right
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f); // Top Right
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f); // Top Left

        gl.glEnd();

        gl.glPopMatrix();

    }

    public static void drawPyramid(GL2 gl) {
        gl.glBegin(GL2.GL_TRIANGLES);

        // Front Face
        gl.glTexCoord2f(0.5f, 0.0f);
        gl.glVertex3f(0.0f, 1.0f, 0.0f); // Top
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f); // Bottom Left
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f); // Bottom Right

        // Right Face
        gl.glTexCoord2f(0.5f, 0.0f);
        gl.glVertex3f(0.0f, 1.0f, 0.0f); // Top
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f); // Bottom Left
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f); // Bottom Right

        // Back Face
        gl.glTexCoord2f(0.5f, 0.0f);
        gl.glVertex3f(0.0f, 1.0f, 0.0f); // Top
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f); // Bottom Left
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f); // Bottom Right

        // Left Face
        gl.glTexCoord2f(0.5f, 0.0f);
        gl.glVertex3f(0.0f, 1.0f, 0.0f); // Top
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f); // Bottom Left
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f); // Bottom Right

        gl.glEnd();
    }

    public static void drawRegalos(GL2 gl) {

        gl.glBegin(GL2.GL_QUADS);
        // Front Face

        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f); // Bottom Left
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f); // Top Left
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f); // Top Right
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f); // Bottom Right

        // Back Face
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f); // Bottom Right
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f); // Top Right
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f); // Top Left
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f); // Bottom Left

        // Top Face
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f); // Top Left
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f); // Bottom Left
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f); // Bottom Right
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f); // Top Right

        // Bottom Face
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f); // Top Right
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f); // Top Left
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f); // Bottom Left
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f); // Bottom Right
        // Right face
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f); // Bottom Right
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f); // Top Right
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f); // Top Left
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f); // Bottom Left
        // Left Face
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f); // Bottom Left
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f); // Bottom Right
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f); // Top Right
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f); // Top Left

        gl.glEnd();
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change
        // body of generated methods, choose Tools | Templates.
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Obtener el código de la tecla presionada
        int keyCode = e.getKeyCode();

        // Control de la luz
        switch (keyCode) {
            case KeyEvent.VK_K -> // Mover luz hacia abajo
                this.moveLightY(false);
            case KeyEvent.VK_I -> // Mover luz hacia arriba
                this.moveLightY(true);
            case KeyEvent.VK_L -> // Mover luz a la derecha
                this.moveLightX(true);
            case KeyEvent.VK_J -> // Mover luz a la izquierda
                this.moveLightX(false);
            case KeyEvent.VK_PAGE_UP -> // Alejar la luz
                this.moveLightZ(false);
            case KeyEvent.VK_PAGE_DOWN -> // Acercar la luz
                this.moveLightZ(true);
        }

        // Control de la cámara
        switch (keyCode) {
            case KeyEvent.VK_UP -> // Mover la cámara hacia arriba
                posCamY += 0.5f;
            case KeyEvent.VK_DOWN -> // Mover la cámara hacia abajo
                posCamY -= 0.5f;
            case KeyEvent.VK_LEFT -> // Mover la cámara hacia la izquierda
                posCamX -= 0.5f;
            case KeyEvent.VK_RIGHT -> // Mover la cámara hacia la derecha
                posCamX += 0.5f;
            case KeyEvent.VK_F1 -> // Aumentar el campo de visión
                fovy += factInc;
            case KeyEvent.VK_F2 -> // Disminuir el campo de visión
                fovy -= factInc;
        }

        // Control de la rotación
        switch (keyCode) {
            case KeyEvent.VK_3 -> {
                // Aumentar la rotación según el eje seleccionado
                switch (eje) {
                    case 1 -> rotX += factInc;
                    case 2 -> rotY += factInc;
                    case 3 -> rotZ += factInc;
                }
            }
            case KeyEvent.VK_4 -> {
                // Disminuir la rotación según el eje seleccionado
                switch (eje) {
                    case 1 -> rotX -= factInc;
                    case 2 -> rotY -= factInc;
                    case 3 -> rotZ -= factInc;
                }
            }
        }

        // Cambiar el eje de rotación
        switch (keyCode) {
            case KeyEvent.VK_X -> eje = 1; // Rotar sobre X
            case KeyEvent.VK_Y -> eje = 2; // Rotar sobre Y
            case KeyEvent.VK_Z -> eje = 3; // Rotar sobre Z
        }

        // Control de la distancia a la escena
        switch (keyCode) {
            case KeyEvent.VK_S -> // Acercar la cámara
                posCamZ -= 0.5f;
            case KeyEvent.VK_W -> // Alejar la cámara
                posCamZ += 0.5f;
        }
    }


    @Override
    public void keyReleased(KeyEvent e) {
    }
}
