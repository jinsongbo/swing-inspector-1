package com.swingInspector.runtime;

import javax.swing.*;
import java.awt.*;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * author: alex
 * date  : 11/10/13
 */
public class SwingHelper {
	private static final Object PLACE_HOLDER = new Object();

	/**
	 * In case that we have been connected as agent AFTER the application was started up, 
	 * we should grab all frames and collect all available components.
	 *
	 * This method should be called only once, but it's safe to call it many times as you need
	 * (will not make any sense after correct agent initialization)
	 */
	public static void collectAllComponents(Components components) {
		Frame[] frames = Frame.getFrames();
		Map<Component, Object> visited = new IdentityHashMap<Component, Object>();
		for (Frame frame : frames) {
			collectComponentsFromContainer(components, visited, frame);
		}
	}

	private static void collectComponentsFromContainer(Components components,
													Map<Component, Object> visited,
													Container c)
	{
		if (visited.put(c, PLACE_HOLDER) != null)
			return;

		if (c instanceof JComponent) {
			components.registerSoft((JComponent) c);
		}

		Component[] cc = c.getComponents();
		for (Component component : cc) {
			if (component instanceof JComponent)
				components.registerSoft((JComponent) component);
			if (component instanceof Container)
				collectComponentsFromContainer(components, visited, c);
		}
	}

	public static void paintBorder(JComponent c, Graphics graphics, Color color) {
		int x = c.getWidth() - 1;
		int y = c.getHeight() - 1;
		if (x > 0 && y > 0) {
			Graphics g = graphics.create();
			g.setColor(color);
			g.drawLine(0, 0, x, 0);
			g.drawLine(x, 0, x, y);
			g.drawLine(0, 0, 0, y);
			g.drawLine(0, y, x, y);
			g.dispose();
		}
	}
}
