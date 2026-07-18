package games.cubi.locatables.api;

import java.util.UUID;

/**
 * The mutable locatable branch. Block implementations use
 * {@link MutableBlockLocatable}; arithmetic on those implementations is
 * quantized through their block setters.
 */
public non-sealed interface MutableLocatable extends Locatable, MutableSpatial {
    @Override
    MutableLocatable setX(double x);

    @Override
    MutableLocatable setY(double y);

    @Override
    MutableLocatable setZ(double z);

    /**
     * Default compatibility implementation using component setters. Implementations
     * should override this when they support a bulk or atomic update.
     *
     * @param x the new x coordinate
     * @param y the new y coordinate
     * @param z the new z coordinate
     * @return this mutable locatable
     */
    @Override
    default MutableLocatable setPosition(double x, double y, double z) {
        setX(x);
        setY(y);
        setZ(z);
        return this;
    }

    MutableLocatable setWorld(UUID world);

    default MutableLocatable setX(int x) {
        return setX((double) x);
    }

    default MutableLocatable setY(int y) {
        return setY((double) y);
    }

    default MutableLocatable setZ(int z) {
        return setZ((double) z);
    }

    @Override
    default MutableLocatable setPosition(Spatial spatial) {
        double x = spatial.x();
        double y = spatial.y();
        double z = spatial.z();
        return setPosition(x, y, z);
    }

    default MutableLocatable setLocation(Locatable locatable) {
        UUID world = locatable.world();
        double x = locatable.x();
        double y = locatable.y();
        double z = locatable.z();
        return setLocation(world, x, y, z);
    }

    default MutableLocatable setLocation(UUID world, double x, double y, double z) {
        setWorld(world);
        return setPosition(x, y, z);
    }

    default MutableLocatable set(Locatable locatable) {
        return setLocation(locatable);
    }

    default MutableLocatable set(double x, double y, double z) {
        return setPosition(x, y, z);
    }

    default MutableLocatable set(int x, int y, int z) {
        return setPosition(x, y, z);
    }

    default MutableLocatable set(double x, double y, double z, UUID world) {
        return setLocation(world, x, y, z);
    }

    default MutableLocatable normalize() {
        double x = x();
        double y = y();
        double z = z();
        double length = Math.sqrt(x * x + y * y + z * z);
        if (length == 0.0) {
            throw new ArithmeticException("Cannot normalize a zero-length spatial");
        }
        double factor = 1.0 / length;
        return setPosition(x * factor, y * factor, z * factor);
    }

    default MutableLocatable add(Locatable locatable) {
        return add((Spatial) locatable);
    }

    default MutableLocatable add(Spatial spatial) {
        double x = x();
        double y = y();
        double z = z();
        double otherX = spatial.x();
        double otherY = spatial.y();
        double otherZ = spatial.z();
        return setPosition(x + otherX, y + otherY, z + otherZ);
    }

    default MutableLocatable add(double x, double y, double z) {
        double currentX = x();
        double currentY = y();
        double currentZ = z();
        return setPosition(currentX + x, currentY + y, currentZ + z);
    }

    default MutableLocatable subtract(Locatable locatable) {
        return subtract((Spatial) locatable);
    }

    default MutableLocatable subtract(Spatial spatial) {
        double x = x();
        double y = y();
        double z = z();
        double otherX = spatial.x();
        double otherY = spatial.y();
        double otherZ = spatial.z();
        return setPosition(x - otherX, y - otherY, z - otherZ);
    }

    default MutableLocatable scalarMultiply(double factor) {
        double x = x();
        double y = y();
        double z = z();
        return setPosition(x * factor, y * factor, z * factor);
    }
}
