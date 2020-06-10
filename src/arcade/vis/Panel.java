package arcade.vis;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import javax.swing.JFrame;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display3d.Display3D;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal3d.Portrayal3D;

/** 
 * Abstract class for visualization panels, consisting of a JFrame generated
 * from a Display object. Drawings are attached to the Display object.
 * 
 * @author  Jessica S. Yu <jessicayu@u.northwestern.edu>
 * @version 2.3.0
 * @since   2.2
 */

public abstract class Panel {
	JFrame frame;

	// PROPERTIES.
	public JFrame getFrame() { return frame; }
	
	// ABSTRACT METHODS.
	abstract void attach(Drawer drawer, String name, Rectangle2D.Double bounds);
	abstract void reset();
	
	// METHOD: setup. Setups up title and location of frame.
	public void setup(String title, int x, int y) {
		frame.setVisible(true);
		frame.setTitle(title);
		frame.setLocation(new Point(x, y));
	}
	
	// METHOD: remove. Removes the JFrame.
	public void remove() {
		if (frame != null) { frame.dispose(); }
		frame = null;
	}
	
	// METHOD: register. Registers the frame to the controller.
	public void register(Controller controller) {
		controller.registerFrame(frame);
	}
	
	// CLASS: Panel2D. Nested Display2D class.
	public static class Panel2D extends Panel {
		final Display2D display;
		
		// CONSTRUCTOR.
		public Panel2D(String title, int x, int y, int w, int h, Visualization vis) {
			display = new Display2D(w, h, vis);
			display.setBackdrop(Color.black);
			frame = display.createFrame();
			setup(title, x, y);
		}
		
		// METHOD: attach. Attaches 2D portrayal with given bounds.
		public void attach(Drawer drawer, String name, Rectangle2D.Double bounds) {
			FieldPortrayal2D port = (FieldPortrayal2D)(drawer.getPortrayal());
			if (bounds == null) { display.attach(port, name); }
			else { display.attach(port, name, bounds); }
		}
		
		// METHOD: reset. Resets 2D display.
		public void reset() {
			display.reset();
			display.repaint();
		}
	}
	
	// CLASS: Panel3D. Nested Display3D class.
	public static class Panel3D extends Panel {
		final Display3D display;
		
		// CONSTRUCTOR.
		public Panel3D(String title, int x, int y, int w, int h, Visualization vis) {
			display = new Display3D(w, h, vis);
			frame = display.createFrame();
			setup(title, x, y);
		}
		
		// METHOD: attach. Attaches 3D portrayal.
		public void attach(Drawer drawer, String name, Rectangle2D.Double bounds) {
			Portrayal3D port = (Portrayal3D)(drawer.getPortrayal());
			display.attach(port, name);
		}
		
		// METHOD: reset. Resets 3D display.
		public void reset() {
			display.createSceneGraph();
			display.reset();
		}
		
		// METHOD: decorate.
		public void decorate(Portrayal3D port, String name) { display.attach(port, name); }
	}
}