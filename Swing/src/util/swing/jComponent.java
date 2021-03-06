/*
 * There is no read the source of this class.  It is not a requirement of the assignment, and is very boring.
 *
 * The TL;DR summary is that it is a JContainer wrapper that allow jQuery like creation and manipulation of
 * Swing JContainer based objects using method chaining and the Builder pattern [Gamma95].
 *
 * example usage:
 *    JPanel parent = new JPanel();
 *    JPanel child  = new JPanel();
 *    JPanel panel  = jComponent.builder(new JPanel())
 *          .layout(new AspectLayout(new Dimension(93, 23)).setFill(true))
 *          .opaque(false)
 *          .add(jComponent.builder(child)
 *                .opaque(false)
 *                .boxLayout(BoxLayout.LINE_AXIS)
 *                .get())
 *          .appendTo(parent);
 *
 * This class was not generated by a 3rd party tool, it was written **by hand**, by **me** (Christopher Anderson)
 * using `vim` and a number of regular expression. The process is detailed herein:
 *
 *  1. The contents of the classes Component, Container, and JComponent were concatenated into a `vim` session
 *  2. The names of the setters were isolated by executing this regex:
 *       :%s/^    public void set\w\+(.*)/XXX&
 *       :!grep XXX
 *  3. The leading junk was removed, then field definitions produced by the regex:
 *       :'<,'>s/^public void set\(\w\)\(\w\+\)(\(\w\+\) \(\w\+\)) {/public \3 \l\1\2;
 *  4. After copying that output into the class, it was undone, and another regex was applied to obtain methods:
 *       :'<,'>s/^public void set\(\w\)\(\w\+\)(\(\w\+\) \(\w\+\)) {/
 *                if (\l\1\2 !== null) { component.set\1\2(\l\1\2); } this.\l\1\2 = \l\1\2;
 *  5. These definitions were copied into the class.
 *  6. Native types were replaced with their boxed counterparts to allow `null` values
 *  7. A manual `.text()` method and field were added, conditional on the `component` being an instance of
 *     AbstractButton
 *  8. The IDE was used to reformat the code and ultimately to remove most unused methods
 *
 *  The entire process took around 3 hours, which while certainly unnecessary, was a very short amount of time
 *  considering the comprehensive implementation, and was also very education in the matter of practical applications
 *  of the Builder pattern [Gamma95].
 *
 *  The reason it has not been removed before grading, is that it's usage has imposed excellent grouping of
 *  swing setXXX methods, and is no longer easily removable.  While the following source code may not be pleasant
 *  to read (so don't), the improvement in the source code of AppView is marked (no pun intended).
 */
package util.swing;

import util.swing.containers.ImagePanel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "RedundantSuppression"})
public class jComponent {

   private final JComponent component;

   jComponent(JComponent component,
              Border border,
              Color background,
              Color foreground,
              Dimension maximumSize,
              Dimension minimumSize,
              Dimension preferredSize,
              Dimension size,
              Font font,
              Point location,
              Rectangle bounds,
              String name,
              String toolTipText,
              String text,
              Boolean enabled,
              Boolean opaque,
              Boolean visible,
              Float alignmentX,
              Float alignmentY,
              Integer mnemonic,
              KeyStroke accelerator,
              List<ActionListener> actions,
              List<ItemListener> items
   ) {
      //@formatter:off
      if (alignmentX    != null) component.setAlignmentX(alignmentX);
      if (alignmentY    != null) component.setAlignmentY(alignmentY);
      if (background    != null) component.setBackground(background);
      if (border        != null) component.setBorder(border);
      if (bounds        != null) component.setBounds(bounds);
      if (enabled       != null) component.setEnabled(enabled);
      if (font          != null) component.setFont(font);
      if (foreground    != null) component.setForeground(foreground);
      if (location      != null) component.setLocation(location);
      if (maximumSize   != null) component.setMaximumSize(maximumSize);
      if (minimumSize   != null) component.setMinimumSize(minimumSize);
      if (name          != null) component.setName(name);
      if (opaque        != null) component.setOpaque(opaque);
      if (preferredSize != null) component.setPreferredSize(preferredSize);
      if (size          != null) component.setSize(size);
      if (toolTipText   != null) component.setToolTipText(toolTipText);
      if (visible       != null) component.setVisible(visible);
      if (text          != null) {
         if      (component instanceof AbstractButton) ((AbstractButton) (component)).setText(text);
         else if (component instanceof JLabel)         ((JLabel)         (component)).setText(text);
         else if (component instanceof JTextComponent) ((JTextComponent) (component)).setText(text);
      }
      if (mnemonic      != null) {
         if      (component instanceof AbstractButton) ((AbstractButton) (component)).setMnemonic(mnemonic);
      }
      if (accelerator   != null) {
         if      (component instanceof JMenuItem     ) ((JMenuItem)      (component)).setAccelerator(accelerator);
      }
      //@formatter:on

      if (component instanceof AbstractButton) {
         AbstractButton abstractButton = (AbstractButton) component;
         for (ActionListener l : actions)
            abstractButton.addActionListener(l);
         for (ItemListener l : items)
            abstractButton.addItemListener(l);
      }

      this.component = component;

   }

   public static jComponentBuilder builder(JComponent component) {
      return internalBuilder().component(component);
   }

   public static JMenu menu(String text, int mnemonic) {
      return jComponent.builder(new JMenu())
            .text(text)
            .mnemonic(mnemonic)
            .get();
   }

   public static jComponentBuilder menuBuilder(String text, int mnemonic) {
      return jComponent.builder(new JMenu())
            .text(text)
            .mnemonic(mnemonic);
   }

   public static JMenuItem menuItem(String text, int mnemonic) {
      return menuItem(text, mnemonic, text);
   }

   public static JMenuItem menuItem(String text, int mnemonic, String actionCommand) {
      JMenuItem mi = new JMenuItem(text, mnemonic);
      mi.setActionCommand(actionCommand);
      return mi;
   }

   public static jComponentBuilder menuItemBuilder(String text, int mnemonic) {
      return menuItemBuilder(text, mnemonic, text);
   }

   public static jComponentBuilder menuItemBuilder(String text, int mnemonic, String actionCommand) {
      return jComponent.builder(menuItem(text, mnemonic, text));
   }


   private static jComponentBuilder internalBuilder() {
      return new jComponentBuilder();
   }

   /**
    * {@code build()} component and return instance
    *
    * @param classType return type {@code JComponent.class}
    * @param <E>       return type
    *
    * @return built container instance, cannot chain past this call
    */
   public <E extends JComponent> E getAs(Class<E> classType) {
      // Class<? extends JComponent> classType = exampleClass.getClass();
      return classType.cast(component);
   }

   /**
    * {@code build()} component and return instance
    *
    * @param <E> return type
    *
    * @return built container instance, cannot chain past this call
    */
   @SuppressWarnings("unchecked")
   public <E> E get() {
      //noinspection unchecked
      return (E) component;
   }

   /**
    * {@code build()} component and add it to {@code container}
    *
    * @param container container to add ourselves to
    * @param <E>       return type
    *
    * @return built container instance, cannot chain past this call
    */
   public <E> E appendTo(Container container) {
      container.add(component);
      return get();
   }

   /**
    * Adds component to component being built
    *
    * <p><b>Note: is added immediately, and not saved for this or future <pre>build()</pre>s</b>
    *
    * @param comp component to add
    *
    * @return this (for continued chaining)
    */
   public jComponent add(Component comp) {
      if (component instanceof Container) {
         component.add(comp);
      }
      return this;
   }

   /**
    * Adds component to component being built
    *
    * <p><b>Note: is added immediately, and not saved for this or future <pre>build()</pre>s</b>
    *
    * @param comp        component to add
    * @param constraints optional constraints
    *
    * @return this (for continued chaining)
    */
   public jComponent add(Component comp, Object constraints) {
      if (component instanceof Container) {
         component.add(comp, constraints);
      }
      return this;
   }

   public static class jComponentBuilder {
      private final List<ActionListener> actions = new ArrayList<>();
      private final List<ItemListener> items = new ArrayList<>();
      private JComponent component;
      private Border border = null;
      private Color background = null;
      private Color foreground = null;
      private Dimension maximumSize = null;
      private Dimension minimumSize = null;
      private Dimension preferredSize = null;
      private Dimension size = null;
      private Font font = null;
      private Point location = null;
      private Rectangle bounds = null;
      private String name = null;
      private String toolTipText = null;
      private String text = null;
      private Boolean enabled = null;
      private Boolean focusable = null;
      private Boolean opaque = null;
      private Boolean visible = null;
      private Float alignmentX = null;
      private Float alignmentY = null;
      private Integer mnemonic = null;
      private KeyStroke accelerator;

      jComponentBuilder() {
      }

      public jComponent.jComponentBuilder component(JComponent component) {
         this.component = component;
         return this;
      }

      public jComponent.jComponentBuilder border(Border border) {
         this.border = border;
         return this;
      }

      public jComponent.jComponentBuilder background(Color background) {
         this.background = background;
         return this;
      }

      public jComponent.jComponentBuilder foreground(Color foreground) {
         this.foreground = foreground;
         return this;
      }

      public jComponent.jComponentBuilder maximumSize(int width, int height) {
         return maximumSize(new Dimension(width, height));
      }

      public jComponent.jComponentBuilder minimumSize(int width, int height) {
         return minimumSize(new Dimension(width, height));
      }

      public jComponent.jComponentBuilder preferredSize(int width, int height) {
         return preferredSize(new Dimension(width, height));
      }

      public jComponent.jComponentBuilder size(int width, int height) {
         return size(new Dimension(width, height));
      }

      public jComponent.jComponentBuilder maximumSize(Dimension maximumSize) {
         this.maximumSize = maximumSize;
         return this;
      }

      public jComponent.jComponentBuilder minimumSize(Dimension minimumSize) {
         this.minimumSize = minimumSize;
         return this;
      }

      public jComponent.jComponentBuilder preferredSize(Dimension preferredSize) {
         this.preferredSize = preferredSize;
         return this;
      }

      public jComponent.jComponentBuilder size(Dimension size) {
         this.size = size;
         return this;
      }

      public jComponent.jComponentBuilder bounds(int x, int y, int width, int height) {
         return bounds(new Rectangle(x, y, width, height));
      }

      public jComponent.jComponentBuilder font(Font font) {
         this.font = font;
         return this;
      }

      public jComponent.jComponentBuilder location(Point location) {
         this.location = location;
         return this;
      }

      public jComponent.jComponentBuilder bounds(Rectangle bounds) {
         this.bounds = bounds;
         return this;
      }

      public jComponent.jComponentBuilder name(String name) {
         this.name = name;
         return this;
      }

      public jComponent.jComponentBuilder toolTipText(String toolTipText) {
         this.toolTipText = toolTipText;
         return this;
      }

      public jComponent.jComponentBuilder text(String text) {
         this.text = text;
         return this;
      }

      public jComponent.jComponentBuilder enabled(Boolean enabled) {
         this.enabled = enabled;
         return this;
      }

      public jComponent.jComponentBuilder focusable(Boolean focusable) {
         this.focusable = focusable;
         return this;
      }

      public jComponent.jComponentBuilder opaque(Boolean opaque) {
         this.opaque = opaque;
         return this;
      }

      public jComponent.jComponentBuilder visible(Boolean visible) {
         this.visible = visible;
         return this;
      }

      public jComponent.jComponentBuilder alignmentX(Float alignmentX) {
         this.alignmentX = alignmentX;
         return this;
      }

      public jComponent.jComponentBuilder alignmentY(Float alignmentY) {
         this.alignmentY = alignmentY;
         return this;
      }

      public jComponent.jComponentBuilder layout(LayoutManager layout) {
         this.component.setLayout(layout);
         return this;
      }

      public jComponent.jComponentBuilder boxLayout(int axis) {
         this.component.setLayout(new BoxLayout(component, axis));
         return this;
      }

      public jComponent.jComponentBuilder mnemonic(int mnemonic) {
         this.mnemonic = mnemonic;
         return this;
      }

      public jComponent.jComponentBuilder accelerator(KeyStroke keyStroke) {
         this.accelerator = keyStroke;
         return this;
      }

      /**
       * Adds component to component being built
       *
       * <p><b>Note: is added immediately, and not saved for this or future <pre>build()</pre>s</b>
       *
       * @param comp component to add
       *
       * @return this (for continued chaining)
       */
      public jComponent.jComponentBuilder add(Component comp) {
         return add(comp, null, -1);
      }

//      public jComponent.jComponentBuilder add(jComponent comp) {
//         return add(comp.getAs(Component.class));
//      }

      public jComponent.jComponentBuilder add(jComponentBuilder b) {
         add(b.getAs(JComponent.class));
         return this;
      }

      public jComponent.jComponentBuilder addBuilder(JComponent component) {
         return add(builder(component));
      }

      public jComponent.jComponentBuilder add(Component comp, int index) {
         return add(comp, null, index);
      }

      public jComponent.jComponentBuilder add(Component comp, Object constraints) {
         return add(comp, constraints, -1);
      }

      public jComponent.jComponentBuilder add(Component comp, Object constraints, int index) {
         if (this.component instanceof JMenuItem) {
            JMenuItem jMenuItem = (JMenuItem) this.component;
            jMenuItem.add(comp);
            return this;
         }
         this.component.add(comp, constraints, index);
         return this;
      }

      public jComponent.jComponentBuilder addSeparator() {
         if (component instanceof JMenu) {
            JMenu jMenu = (JMenu) component;
            jMenu.addSeparator();
         }
         return this;
      }

      public jComponent.jComponentBuilder image(URL resourceUrl) {
         if (component instanceof ImagePanel || component instanceof AbstractButton) {
            ((ImagePanel) component).setImage(resourceUrl);
         }
         return this;
      }


      public jComponent.jComponentBuilder action(ActionListener actionListener) {
         this.actions.add(actionListener);
         return this;
      }

      public jComponent.jComponentBuilder item(ItemListener itemListener) {
         this.items.add(itemListener);
         return this;
      }


      /**
       * {@code build()} component and return instance
       *
       * @param <E> return type
       *
       * @return built container instance, cannot chain past this call
       */
      public <E extends Component> E get() {
         //noinspection unchecked
         return build().get();
      }

      /**
       * {@code build()} component and return instance
       *
       * @param classType return type {@code JComponent.class}
       * @param <E>       return type
       *
       * @return built container instance, cannot chain past this call
       */
      public <E extends JComponent> E getAs(Class<E> classType) {
         return build().getAs(classType);
      }

      /**
       * {@code build()} component and add it to {@code container}
       *
       * @param container container to add ourselves to
       * @param <E>       return type
       *
       * @return built container instance, cannot chain past this call
       */
      public <E> E appendTo(Container container) {
         //noinspection unchecked
         return build().appendTo(container);
      }

      /**
       * {@code build()} component (still need to {@code get()} the component)
       *
       * @return built container wrapper (jComponent)
       */
      public jComponent build() {
         return new jComponent(component,
               border,
               background,
               foreground,
               maximumSize,
               minimumSize,
               preferredSize,
               size,
               font,
               location,
               bounds,
               name,
               toolTipText,
               text,
               enabled,
               opaque,
               visible,
               alignmentX,
               alignmentY,
               mnemonic,
               accelerator,
               actions,
               items
         );
      }

      public String toString() {
         return "jComponent.jComponentBuilder(component=" + this.component +
               ", border=" + this.border +
               ", background=" + this.background +
               ", foreground=" + this.foreground +
               ", maximumSize=" + this.maximumSize +
               ", minimumSize=" + this.minimumSize +
               ", preferredSize=" + this.preferredSize +
               ", size=" + this.size +
               ", font=" + this.font +
               ", location=" + this.location +
               ", bounds=" + this.bounds +
               ", name=" + this.name +
               ", toolTipText=" + this.toolTipText +
               ", text=" + this.text +
               ", enabled=" + this.enabled +
               ", focusable=" + this.focusable +
               ", opaque=" + this.opaque +
               ", visible=" + this.visible +
               ", alignmentX=" + this.alignmentX +
               ", alignmentY=" + this.alignmentY +
               ")";
      }

   }
}