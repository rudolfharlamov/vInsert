package org.vinsert.api.wrappers;

import org.vinsert.api.util.Utilities;
import org.vinsert.api.wrappers.interaction.SceneNode;

import java.awt.*;
import java.util.Arrays;

/**
 * Date: 09/04/13
 * Time: 09:45
 *
 * @author A_C/Cov
 */
public final class Area {

    private int pn;
    private int[] px;
    private int[] py;
    private int minPlane = -1;
    private int maxPlane = -1;

    private static final int MINIMUM_LENGTH = 4;

    public Area() {
        px = new int[MINIMUM_LENGTH];
        py = new int[MINIMUM_LENGTH];
    }

    public Area(Tile... bounds) {
        this();
        if (bounds.length == 2) {
            Tile t1 = bounds[0];
            Tile t2 = bounds[1];
            bounds = new Tile[]{new Tile(Math.min(t1.getX(), t2.getX()), Math.min(t1.getY(), t2.getY()), t1.getZ()),
                    new Tile(Math.max(t1.getX(), t2.getX()), Math.min(t1.getY(), t2.getY()), t1.getZ()),
                    new Tile(Math.max(t1.getX(), t2.getX()), Math.max(t1.getY(), t2.getY()), t2.getZ()),
                    new Tile(Math.min(t1.getX(), t2.getX()), Math.max(t1.getY(), t2.getY()), t2.getZ())};
        }
        for (final Tile tile : bounds) {
            addTile(tile.getX(), tile.getY(), tile.getZ());
        }
    }

    /**
     * Adds a tiles x, y and z to the polygon area.
     *
     * @param x - the x co-ordinate to add.
     * @param y - the y co-ordinate to add.
     * @param z - the plane
     */
    public void addTile(int x, int y, int z) {
        if (pn >= px.length || pn >= py.length) {
            int newLength = pn * 2;
            if (newLength < MINIMUM_LENGTH) {
                newLength = MINIMUM_LENGTH;
            } else if ((newLength & (newLength - 1)) != 0) {
                newLength = Integer.highestOneBit(newLength);
            }


            px = Arrays.copyOf(px, newLength);
            py = Arrays.copyOf(py, newLength);
        }
        px[pn] = x;
        py[pn] = y;
        pn++;
        if (minPlane == -1) {
            minPlane = z;
        }

        if (maxPlane == -1) {
            maxPlane = z;
        }

        minPlane = Math.min(minPlane, z);
        maxPlane = Math.max(maxPlane, z);

    }

    /**
     * Gets the bounding box of the Area.
     *
     * @return Bounding Box as a Rectangle.
     */
    public Rectangle getBounds() {
        int boundsMinX = Integer.MAX_VALUE;
        int boundsMinY = Integer.MAX_VALUE;
        int boundsMaxX = Integer.MIN_VALUE;
        int boundsMaxY = Integer.MIN_VALUE;

        for (int i = 0; i < pn; i++) {
            int x = px[i];
            boundsMinX = Math.min(boundsMinX, x);
            boundsMaxX = Math.max(boundsMaxX, x);

            int y = py[i];
            boundsMinY = Math.min(boundsMinY, y);
            boundsMaxY = Math.max(boundsMaxY, y);
        }
        return new Rectangle(boundsMinX, boundsMinY, boundsMaxX - boundsMinX, boundsMaxY - boundsMinY);
    }

    /**
     * Gets the center tile in the area
     *
     * @return center Tile in the area
     */
    public Tile getCenter() {
        Rectangle bounds = getBounds();
        return new Tile((int) Math.round(bounds.getCenterX()), (int) Math.round(bounds.getCenterY()), Math.round((minPlane + maxPlane) / 2));
    }

    /**
     * Gets a random Tile in the Area
     *
     * @return random Tile from the Area
     */
    public Tile getRandom() {
        Rectangle bounds = getBounds();
        int tries = 0;
        while (tries++ < 100) {
            Tile tempTile = new Tile(
                    Utilities.random(bounds.x, bounds.x + bounds.width),
                    Utilities.random(bounds.y, bounds.y + bounds.height),
                    Utilities.random(minPlane, maxPlane)
            );
            if (contains(tempTile)) {
                return tempTile;
            }
        }
        return null;
    }

    /**
     * Checks if the area contains a SceneNode
     *
     * @param node - the SceneNode to look for
     * @return <t>true if the node is inside the area</t> otherwise false
     */
    public boolean contains(SceneNode node) {
        return contains(node.getX(), node.getY(), node.getZ());
    }

    /**
     * Checks if the area contains a Tile
     *
     * @param tile - the Tile to look for
     * @return <t>true if the tile is inside the area</t> otherwise false
     */
    public boolean contains(Tile tile) {
        return contains(tile.getX(), tile.getY(), tile.getZ());
    }

    /**
     * Checks if the area contains an x/y
     *
     * @param x - the x co-ordinate
     * @param y - the y co-ordinate
     * @param z -  the plane
     * @return <t>true if the x and y co-ordinates are inside the area</t> otherwise false
     */
    public boolean contains(int x, int y, int z) {
        return contains((double) x, (double) y, (double) z);
    }

    private boolean contains(double x, double y, double z) {
        if (pn <= 2 || !getBounds().contains(x, y)) {
            return false;
        }
        int hits = 0;

        int lastX = px[pn - 1];
        int lastY = py[pn - 1];
        int curX, curY;

        // Walk the edges of the polygon
        for (int i = 0; i < pn; lastX = curX, lastY = curY, i++) {
            curX = px[i];
            curY = py[i];

            if (curY == lastY) {
                continue;
            }

            int leftX;
            if (curX < lastX) {
                if (x >= lastX) {
                    continue;
                }
                leftX = curX;
            } else {
                if (x >= curX) {
                    continue;
                }
                leftX = lastX;
            }

            double test1, test2;
            if (curY < lastY) {
                if (y < curY || y >= lastY) {
                    continue;
                }
                if (x < leftX) {
                    hits++;
                    continue;
                }
                test1 = x - curX;
                test2 = y - curY;
            } else {
                if (y < lastY || y >= curY) {
                    continue;
                }
                if (x < leftX) {
                    hits++;
                    continue;
                }
                test1 = x - lastX;
                test2 = y - lastY;
            }

            if (test1 < (test2 / (lastY - curY) * (lastX - curX))) {
                hits++;
            }
        }

        return ((hits & 1) != 0) && z >= minPlane && z <= maxPlane;
    }

}