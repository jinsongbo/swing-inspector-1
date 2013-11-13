package com.swingInspector.runtime;

import com.swingInspector.utils.Listener;
import com.swingInspector.utils.Listeners;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * author: alex
 * date  : 11/10/13
 */
public class BorderControl {
	private MouseAdapter currentBorderListener;
	private ComponentHighlightConfiguration currentBorderConfig;
	private final Listener<JComponent> BORDER_LISTENER = new Listener<JComponent>() {
		@Override
		public void onEvent(JComponent update) {
			if (currentBorderListener != null)
				update.addMouseListener(currentBorderListener);
		}
	};

	private final Listeners<JComponent> listeners = new Listeners<JComponent>();

	public void enableBorder(ComponentHighlightConfiguration configuration) {
		if (currentBorderConfig != null && currentBorderConfig.equals(configuration)) {
			return;
		}

		if (currentBorderConfig != null) {
			disableBorder();//remove all old borders
		}

		Components components = SwingInspectorConsole.components;
		currentBorderListener = new BorderMouseListeners(this, configuration, components);
		currentBorderConfig = configuration;
		Set<JComponent> set = components.componentsSet();
		for (JComponent cc : set) {
			cc.addMouseListener(currentBorderListener);
		}
		components.addListener(BORDER_LISTENER);
	}

	public void disableBorder() {
		Components components = SwingInspectorConsole.components;
		for (JComponent component : components.componentsSet()) {
			component.removeMouseListener(currentBorderListener);
			restoreOriginalBorder(components, component);
		}
		components.removeListener(BORDER_LISTENER);
	}

	public void addListener(Listener<JComponent> listener) {
		listeners.addListener(listener);
	}

	public void removeListener(Listener<JComponent> listener) {
		listeners.removeListener(listener);
	}

	private static void restoreOriginalBorder(Components components, JComponent c) {
		Components.ComponentInformationHolder data = components.getData(c);
		Border b = data.getData("border");
		Boolean hasBorder = data.getData("has_border");
		if (hasBorder != null && hasBorder) {
			c.setBorder(b);
			data.addData("has_border", null);
		}
		Container parent = c.getParent();
		if (parent instanceof JComponent) {
			restoreOriginalBorder(components, (JComponent) parent);
		}
	}

	private static void setupCustomBorder(Components components,
										ComponentHighlightConfiguration configuration,
										JComponent c)
	{
		Border newBorder = BorderFactory.createLineBorder(configuration.getBorderColor());
		Border currentBorder = c.getBorder();

		Components.ComponentInformationHolder data = components.getData(c);
		Boolean definedBefore = data.getData("has_border");

		if (definedBefore == null || !definedBefore) {
			data.addData("border", currentBorder);
			data.addData("has_border", true);
			c.setBorder(newBorder);
			if (configuration.getType() == ComponentHighlightConfiguration.BorderType.WHOLE_TREE) {
				Container parent = c.getParent();
				if (parent instanceof JComponent) {
					setupCustomBorder(components, configuration, (JComponent) parent);
				}
			}
		}
	}

	private static class BorderMouseListeners extends MouseAdapter {
		private final BorderControl borderControl;
		private final ComponentHighlightConfiguration configuration;
		private final Components components;

		public BorderMouseListeners(BorderControl borderControl,
									ComponentHighlightConfiguration configuration,
									Components components)
		{
			this.borderControl = borderControl;
			this.configuration = configuration;
			this.components = components;
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof JComponent) {
				JComponent c = (JComponent) component;
				setupCustomBorder(components, configuration, c);
				borderControl.listeners.pushEvent(c);
			}
		}

		@Override
		public void mouseExited(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof JComponent) {
				JComponent c = (JComponent) component;
				restoreOriginalBorder(components, c);
			}
		}
	}
}
