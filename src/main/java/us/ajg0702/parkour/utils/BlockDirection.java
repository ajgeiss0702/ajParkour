package us.ajg0702.parkour.utils;

public enum BlockDirection {
    /**
     * -Z
     */
    NORTH,
    /**
     * +X -Z
     */
    NORTH_EAST,
    /**
     * +X
     */
    EAST,
    /**
     * +X +Z
     */
    SOUTH_EAST,
    /**
     * +Z
     */
    SOUTH,
    /**
     * -X +Z
     */
    SOUTH_WEST,
    /**
     * -X
     */
    WEST,
    /**
     * -X -Z
     */
    NORTH_WEST;


    public BlockDirection getRight() {
        switch(this) {
            case NORTH:
                return NORTH_EAST;
            case NORTH_EAST:
                return EAST;
            case EAST:
                return SOUTH_EAST;
            case SOUTH_EAST:
                return SOUTH;
            case SOUTH:
                return SOUTH_WEST;
            case SOUTH_WEST:
                return WEST;
            case WEST:
                return NORTH_WEST;
            case NORTH_WEST:
                return NORTH;
        }
        throw new IllegalStateException(this.toString());
    }

    public BlockDirection getLeft() {
        switch(this) {
            case NORTH:
                return NORTH_WEST;
            case NORTH_EAST:
                return NORTH;
            case EAST:
                return NORTH_EAST;
            case SOUTH_EAST:
                return EAST;
            case SOUTH:
                return SOUTH_EAST;
            case SOUTH_WEST:
                return SOUTH;
            case WEST:
                return SOUTH_WEST;
            case NORTH_WEST:
                return WEST;
        }
        throw new IllegalStateException(this.toString());
    }

    public BlockDirection getLeft90() {
        switch(this) {
            case NORTH_WEST:
            case NORTH:
                return WEST;
            case NORTH_EAST:
            case EAST:
                return NORTH;
            case SOUTH_EAST:
            case SOUTH:
                return EAST;
            case SOUTH_WEST:
            case WEST:
                return SOUTH;
        }
        throw new IllegalStateException(this.toString());
    }

    public BlockDirection getRight90() {
        switch(this) {
            case NORTH:
            case NORTH_EAST:
                return EAST;
            case EAST:
            case SOUTH_EAST:
                return SOUTH;
            case SOUTH:
            case SOUTH_WEST:
                return WEST;
            case WEST:
            case NORTH_WEST:
                return NORTH;
        }
        throw new IllegalStateException(this.toString());
    }


    public float getYaw() {
        // this is probably wrong. sorry future me
        switch(this) {
            case NORTH:
                return 180;
            case NORTH_EAST:
                return 225;
            case EAST:
                return 270;
            case SOUTH_EAST:
                return 315;
            case SOUTH:
                return 0;
            case SOUTH_WEST:
                return 45;
            case WEST:
                return 90;
            case NORTH_WEST:
                return 135;
        }
        throw new IllegalStateException(this.toString());
    }

    public BlockDirection get180() {
        switch(this) {
            case NORTH:
                return SOUTH;
            case NORTH_EAST:
                return SOUTH_WEST;
            case EAST:
                return WEST;
            case SOUTH_EAST:
                return NORTH_WEST;
            case SOUTH:
                return NORTH;
            case SOUTH_WEST:
                return NORTH_EAST;
            case WEST:
                return EAST;
            case NORTH_WEST:
                return SOUTH_EAST;
        }
        throw new IllegalStateException(this.toString());
    }

    public static BlockDirection randomDirection(boolean only90) {
        BlockDirection[] possibilities = values();
        BlockDirection selected = possibilities[(int) Math.floor(Math.random() * possibilities.length)];

        // Let the right90 logic figure out which one to use if its not already 90deg
        if(only90) return selected.getRight90();

        return selected;
    }
}
