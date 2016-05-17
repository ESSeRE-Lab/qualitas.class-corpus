package org.argouml.ui;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Utility class for easy alignment of components in a GridBagLayout.
 * 
 * @author drahmann
 */
public final class GridBagUtils {
    /**
     * Constant representing text alignment.
     */
    public static final int CENTER = 2;

    /**
     * Constant representing text alignment.
     */
    public static final int LEFT = 0;

    /**
     * Constant representing text alignment.
     */
    public static final int RIGHT = 1;

    private static Insets buttonInsets;

    private static Insets captionInsets;

    private static Insets inputFieldInsets;

    private static int rowDistance;

    static {
        rowDistance = 2;
        buttonInsets = new Insets(rowDistance, 5, rowDistance, 5);
        captionInsets = new Insets(rowDistance + 2, 5, rowDistance, 5);
        inputFieldInsets = new Insets(rowDistance, 0, rowDistance, 0);
    }

    /**
     * Returns GridBagConstraints suitable for a button.
     * 
     * @param x
     *            The x-coordinate of the grid bag where the button should be
     *            placed.
     * @param y
     *            The y-coordinate of the grid bag where the button should be
     *            placed.
     * @return Suitable GridBagConstraints.
     */
    public static GridBagConstraints buttonConstraints(int x, int y) {
        return buttonConstraints(x, y, 1, 1);
    }

    /**
     * Returns GridBagConstraints suitable for a button larger than 1x1
     * GridBags.
     * 
     * @param x
     *            The x-coordinate of the grid bag where the button should be
     *            placed.
     * @param y
     *            The y-coordinate of the grid bag where the button should be
     *            placed.
     * @param width
     *            The width of the button.
     * @param height
     *            The height of the button.
     * @return Suitable GridBagConstraints.
     */
    public static GridBagConstraints buttonConstraints(int x, int y, int width,
            int height) {
        GridBagConstraints gbc = createConstraints(x, y, width, height);
        // gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = buttonInsets;
        return gbc;
    }

    /**
     * Returns GridBagConstraints suitable for a caption (label). The text will
     * be alignemt right.
     * 
     * @param x
     *            The x-coordinate of the grid bag where the label should be
     *            placed.
     * @param y
     *            The y-coordinate of the grid bag where the label should be
     *            placed.
     * @return Suitable GridBagConstraints.
     */
    public static GridBagConstraints captionConstraints(int x, int y) {
        return captionConstraints(x, y, 1, 1, RIGHT);
    }

    /**
     * Returns GridBagConstraints suitable for a caption. The text will be
     * aligned according to the given value.
     * 
     * @param x
     *            The x-coordinate of the grid bag where the caption should be
     *            placed.
     * @param y
     *            The y-coordinate of the grid bag where the caption should be
     *            placed.
     * @param alignment
     *            The alignment of the text.
     * @return Suitable GridBagConstraints.
     */
    public static GridBagConstraints captionConstraints(int x, int y,
            int alignment) {
        return captionConstraints(x, y, 1, 1, alignment);
    }

    /**
     * Returns GridBagConstraints suitable for a caption. The text will be
     * aligned right.
     * 
     * @param x
     *            The x-coordinate of the grid bag where the caption should be
     *            placed.
     * @param y
     *            The y-coordinate of the grid bag where the caption should be
     *            placed.
     * @param width
     *            The width of the text.
     * @param height
     *            The height of the text.
     * @return Suitable GridBagConstraints.
     */
    public static GridBagConstraints captionConstraints(int x, int y,
            int width, int height) {
        return captionConstraints(x, y, width, height, RIGHT);
    }

    /**
     * Returns GridBagConstraints suitable for a caption. The text will be
     * aligned according to the given value.
     * 
     * @param x
     *            The x-coordinate of the grid bag where the caption should be
     *            placed.
     * @param y
     *            The y-coordinate of the grid bag where the caption should be
     *            placed.
     * @param width
     *            The width of the text.
     * @param height
     *            The height of the text.
     * @param alignment
     *            The alignment of the text.
     * @return Suitable GridBagConstraints.
     */
    public static GridBagConstraints captionConstraints(int x, int y,
            int width, int height, int alignment) {
        GridBagConstraints gbc = createConstraints(x, y, width, height);
        if (alignment == LEFT) {
            gbc.anchor = GridBagConstraints.NORTHWEST;
        } else if (alignment == RIGHT) {
            gbc.anchor = GridBagConstraints.NORTHEAST;
        } else if (alignment == CENTER) {
            gbc.anchor = GridBagConstraints.CENTER;
        }
        gbc.insets = captionInsets;
        return gbc;
    }

    /**
     * Returns {@link GridBagConstraints} suitable for client alignment in the
     * given GridBag.
     * 
     * @param x
     *            The x-coordinate of the grid bag.
     * @param y
     *            The y-coordinate of the grid bag.
     * @return Suitable {@link GridBagConstraints}.
     */
    public static GridBagConstraints clientAlignConstraints(int x, int y) {
        return clientAlignConstraints(x, y, 1, 1);
    }

    /**
     * Returns {@link GridBagConstraints} suitable for client alignment in the
     * given GridBag with the given width and height.
     * 
     * @param x
     *            The x-coordinate of the GridBag.
     * @param y
     *            The y-coordinate of the GridBag.
     * @param width
     *            The width.
     * @param height
     *            The height.
     * @return Suitable {@link GridBagConstraints}.
     */
    public static GridBagConstraints clientAlignConstraints(int x, int y,
            int width, int height) {
        GridBagConstraints gbc = createConstraints(x, y, width, height);
        gbc.fill = GridBagConstraints.BOTH;
        return gbc;
    }

    /**
     * Returns GridBagConstraints for placing a component at (x, y) with the
     * dimension (width x height).
     * 
     * @param x
     *            The x-coordinate of the GridBag.
     * @param y
     *            The y-coordinate of the GridBag.
     * @param width
     *            The width.
     * @param height
     *            The height.
     * @return The {@link GridBagConstraints}
     */
    public static GridBagConstraints createConstraints(int x, int y, int width,
            int height) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        return gbc;
    }

    /**
     * Returns GridBagConstraints for placing a {@link JTextArea} at (x, y).
     * 
     * @param x
     *            The x-coordinate of the GridBag.
     * @param y
     *            The y-coordinate of the GridBag.
     * @return The {@link GridBagConstraints}
     */
    public static Object textAreaConstraints(int x, int y) {
        return textAreaConstraints(x, y, 1, 1);
    }

    /**
     * Returns GridBagConstraints for placing a {@link JTextArea} at (x, y) with
     * the dimension (width x height).
     * 
     * @param x
     *            The x-coordinate of the GridBag.
     * @param y
     *            The y-coordinate of the GridBag.
     * @param width
     *            The width.
     * @param height
     *            The height.
     * @return The {@link GridBagConstraints}
     */
    public static Object textAreaConstraints(int x, int y, int width, int height) {
        GridBagConstraints gbc = createConstraints(x, y, width, height);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = inputFieldInsets;
        return gbc;
    }

    /**
     * Returns GridBagConstraints for placing a {@link JTextField} at (x, y).
     * 
     * @param x
     *            The x-coordinate of the GridBag.
     * @param y
     *            The y-coordinate of the GridBag.
     * @return The {@link GridBagConstraints}
     */
    public static GridBagConstraints textFieldConstraints(int x, int y) {
        return textFieldConstraints(x, y, 1, 1);
    }

    /**
     * Returns GridBagConstraints for placing a {@link JTextField} at (x, y)
     * with the dimension (width x height).
     * 
     * @param x
     *            The x-coordinate of the GridBag.
     * @param y
     *            The y-coordinate of the GridBag.
     * @param width
     *            The width.
     * @param height
     *            The height.
     * @return The {@link GridBagConstraints}
     */
    public static GridBagConstraints textFieldConstraints(int x, int y,
            int width, int height) {
        GridBagConstraints gbc = createConstraints(x, y, width, height);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = inputFieldInsets;
        return gbc;
    }

    private GridBagUtils() {
    }
}
